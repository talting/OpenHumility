package cn.hanabi.utils.auth.j4socket.server;

import cn.hanabi.utils.auth.j4socket.AbstractWebSocket;
import cn.hanabi.utils.auth.j4socket.SocketChannelIOHelper;
import cn.hanabi.utils.auth.j4socket.WebSocket;
import cn.hanabi.utils.auth.j4socket.WebSocketFactory;
import cn.hanabi.utils.auth.j4socket.WebSocketImpl;
import cn.hanabi.utils.auth.j4socket.WebSocketServerFactory;
import cn.hanabi.utils.auth.j4socket.WrappedByteChannel;
import cn.hanabi.utils.auth.j4socket.drafts.Draft;
import cn.hanabi.utils.auth.j4socket.exceptions.WebsocketNotConnectedException;
import cn.hanabi.utils.auth.j4socket.exceptions.WrappedIOException;
import cn.hanabi.utils.auth.j4socket.framing.Framedata;
import cn.hanabi.utils.auth.j4socket.handshake.ClientHandshake;
import cn.hanabi.utils.auth.j4socket.handshake.Handshakedata;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class WebSocketServer extends AbstractWebSocket implements Runnable {
   private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
   private final Collection connections;
   private final InetSocketAddress address;
   private ServerSocketChannel server;
   private Selector selector;
   private List drafts;
   private Thread selectorthread;
   private final AtomicBoolean isclosed;
   protected List<WebSocketWorker> decoders;
   private List iqueue;
   private BlockingQueue buffers;
   private int queueinvokes;
   private final AtomicInteger queuesize;
   private WebSocketServerFactory wsf;
   private int maxPendingConnections;

   public WebSocketServer() {
      this(new InetSocketAddress(80), AVAILABLE_PROCESSORS, (List)null);
   }

   public WebSocketServer(InetSocketAddress address) {
      this(address, AVAILABLE_PROCESSORS, (List)null);
   }

   public WebSocketServer(InetSocketAddress address, int decodercount) {
      this(address, decodercount, (List)null);
   }

   public WebSocketServer(InetSocketAddress address, List drafts) {
      this(address, AVAILABLE_PROCESSORS, drafts);
   }

   public WebSocketServer(InetSocketAddress address, int decodercount, List drafts) {
      this(address, decodercount, drafts, new HashSet());
   }

   public WebSocketServer(InetSocketAddress address, int decodercount, List drafts, Collection connectionscontainer) {
      super();
      this.isclosed = new AtomicBoolean(false);
      this.queueinvokes = 0;
      this.queuesize = new AtomicInteger(0);
      this.wsf = new DefaultWebSocketServerFactory();
      this.maxPendingConnections = -1;
      if (address != null && decodercount >= 1 && connectionscontainer != null) {
         if (drafts == null) {
            this.drafts = Collections.emptyList();
         } else {
            this.drafts = drafts;
         }

         this.address = address;
         this.connections = connectionscontainer;
         this.setTcpNoDelay(false);
         this.setReuseAddr(false);
         this.iqueue = new LinkedList();
         this.decoders = new ArrayList(decodercount);
         this.buffers = new LinkedBlockingQueue();

         for(int i = 0; i < decodercount; ++i) {
            WebSocketServer.WebSocketWorker ex = new WebSocketServer.WebSocketWorker();
            this.decoders.add(ex);
         }

      } else {
         throw new IllegalArgumentException("address and connectionscontainer must not be null and you need at least 1 decoder");
      }
   }

   public void start() {
      if (this.selectorthread != null) {
         throw new IllegalStateException(this.getClass().getName() + " can only be started once.");
      } else {
         (new Thread(this)).start();
      }
   }

   public void stop(int timeout) throws InterruptedException {
      if (this.isclosed.compareAndSet(false, true)) {
         List<WebSocket> socketsToClose;
         synchronized(this.connections) {
            socketsToClose = new ArrayList(this.connections);
         }

         for(WebSocket ws : socketsToClose) {
            ws.close(1001);
         }

         this.wsf.close();
         synchronized(this) {
            if (this.selectorthread != null && this.selector != null) {
               this.selector.wakeup();
               this.selectorthread.join((long)timeout);
            }

         }
      }
   }

   public void stop() throws InterruptedException {
      this.stop(0);
   }

   public Collection getConnections() {
      synchronized(this.connections) {
         return Collections.unmodifiableCollection(new ArrayList(this.connections));
      }
   }

   public InetSocketAddress getAddress() {
      return this.address;
   }

   public int getPort() {
      int port = this.getAddress().getPort();
      if (port == 0 && this.server != null) {
         port = this.server.socket().getLocalPort();
      }

      return port;
   }

   public List getDraft() {
      return Collections.unmodifiableList(this.drafts);
   }

   public void setMaxPendingConnections(int numberOfConnections) {
      this.maxPendingConnections = numberOfConnections;
   }

   public int getMaxPendingConnections() {
      return this.maxPendingConnections;
   }

   public void run() {
      if (this.doEnsureSingleThread()) {
         if (this.doSetupSelectorAndServerThread()) {
            try {
               int shutdownCount = 5;
               int selectTimeout = 0;

               while(!this.selectorthread.isInterrupted() && shutdownCount != 0) {
                  SelectionKey key = null;

                  try {
                     if (this.isclosed.get()) {
                        selectTimeout = 5;
                     }

                     int keyCount = this.selector.select((long)selectTimeout);
                     if (keyCount == 0 && this.isclosed.get()) {
                        --shutdownCount;
                     }

                     Set keys = this.selector.selectedKeys();
                     Iterator i = keys.iterator();

                     while(i.hasNext()) {
                        key = (SelectionKey)i.next();
                        if (key.isValid()) {
                           if (key.isAcceptable()) {
                              this.doAccept(key, i);
                           } else if ((!key.isReadable() || this.doRead(key, i)) && key.isWritable()) {
                              this.doWrite(key);
                           }
                        }
                     }

                     this.doAdditionalRead();
                  } catch (CancelledKeyException var15) {
                     ;
                  } catch (ClosedByInterruptException var16) {
                     return;
                  } catch (WrappedIOException var17) {
                     this.handleIOException(key, var17.getConnection(), var17.getIOException());
                  } catch (IOException var18) {
                     this.handleIOException(key, (WebSocket)null, var18);
                  } catch (InterruptedException var19) {
                     Thread.currentThread().interrupt();
                  }
               }

            } catch (RuntimeException var20) {
               this.handleFatal((WebSocket)null, var20);
            } finally {
               this.doServerShutdown();
            }
         }
      }
   }

   private void doAdditionalRead() throws InterruptedException, IOException {
      while(!this.iqueue.isEmpty()) {
         WebSocketImpl conn = (WebSocketImpl)this.iqueue.remove(0);
         WrappedByteChannel c = (WrappedByteChannel)conn.getChannel();
         ByteBuffer buf = this.takeBuffer();

         try {
            if (SocketChannelIOHelper.readMore(buf, conn, c)) {
               this.iqueue.add(conn);
            }

            if (buf.hasRemaining()) {
               conn.inQueue.put(buf);
               this.queue(conn);
            } else {
               this.pushBuffer(buf);
            }
         } catch (IOException var5) {
            this.pushBuffer(buf);
            throw var5;
         }
      }

   }

   private void doAccept(SelectionKey key, Iterator i) throws IOException, InterruptedException {
      if (!this.onConnect(key)) {
         key.cancel();
      } else {
         SocketChannel channel = this.server.accept();
         if (channel != null) {
            channel.configureBlocking(false);
            Socket socket = channel.socket();
            socket.setTcpNoDelay(this.isTcpNoDelay());
            socket.setKeepAlive(true);
            WebSocketImpl w = this.wsf.createWebSocket(this, this.drafts);
            w.setSelectionKey(channel.register(this.selector, 1, w));

            try {
               w.setChannel(this.wsf.wrapChannel(channel, w.getSelectionKey()));
               i.remove();
               this.allocateBuffers(w);
            } catch (IOException var7) {
               if (w.getSelectionKey() != null) {
                  w.getSelectionKey().cancel();
               }

               this.handleIOException(w.getSelectionKey(), (WebSocket)null, var7);
            }

         }
      }
   }

   private boolean doRead(SelectionKey key, Iterator i) throws InterruptedException, WrappedIOException {
      WebSocketImpl conn = (WebSocketImpl)key.attachment();
      ByteBuffer buf = this.takeBuffer();
      if (conn.getChannel() == null) {
         key.cancel();
         this.handleIOException(key, conn, new IOException());
         return false;
      } else {
         try {
            if (SocketChannelIOHelper.read(buf, conn, conn.getChannel())) {
               if (buf.hasRemaining()) {
                  conn.inQueue.put(buf);
                  this.queue(conn);
                  i.remove();
                  if (conn.getChannel() instanceof WrappedByteChannel && ((WrappedByteChannel)conn.getChannel()).isNeedRead()) {
                     this.iqueue.add(conn);
                  }
               } else {
                  this.pushBuffer(buf);
               }
            } else {
               this.pushBuffer(buf);
            }

            return true;
         } catch (IOException var6) {
            this.pushBuffer(buf);
            throw new WrappedIOException(conn, var6);
         }
      }
   }

   private void doWrite(SelectionKey key) throws WrappedIOException {
      WebSocketImpl conn = (WebSocketImpl)key.attachment();

      try {
         if (SocketChannelIOHelper.batch(conn, conn.getChannel()) && key.isValid()) {
            key.interestOps(1);
         }

      } catch (IOException var4) {
         throw new WrappedIOException(conn, var4);
      }
   }

   private boolean doSetupSelectorAndServerThread() {
      this.selectorthread.setName("WebSocketSelector-" + this.selectorthread.getId());

      try {
         this.server = ServerSocketChannel.open();
         this.server.configureBlocking(false);
         ServerSocket socket = this.server.socket();
         socket.setReceiveBufferSize(16384);
         socket.setReuseAddress(this.isReuseAddr());
         socket.bind(this.address, this.getMaxPendingConnections());
         this.selector = Selector.open();
         this.server.register(this.selector, this.server.validOps());
         this.startConnectionLostTimer();

         for(WebSocketServer.WebSocketWorker ex : this.decoders) {
            ex.start();
         }

         this.onStart();
         return true;
      } catch (IOException var4) {
         this.handleFatal((WebSocket)null, var4);
         return false;
      }
   }

   private boolean doEnsureSingleThread() {
      synchronized(this) {
         if (this.selectorthread != null) {
            throw new IllegalStateException(this.getClass().getName() + " can only be started once.");
         } else {
            this.selectorthread = Thread.currentThread();
            return !this.isclosed.get();
         }
      }
   }

   private void doServerShutdown() {
      this.stopConnectionLostTimer();
      if (this.decoders != null) {
         for(WebSocketServer.WebSocketWorker w : this.decoders) {
            w.interrupt();
         }
      }

      if (this.selector != null) {
         try {
            this.selector.close();
         } catch (IOException var4) {
            this.onError((WebSocket)null, var4);
         }
      }

      if (this.server != null) {
         try {
            this.server.close();
         } catch (IOException var3) {
            this.onError((WebSocket)null, var3);
         }
      }

   }

   protected void allocateBuffers(WebSocket c) throws InterruptedException {
      if (this.queuesize.get() < 2 * this.decoders.size() + 1) {
         this.queuesize.incrementAndGet();
         this.buffers.put(this.createBuffer());
      }
   }

   protected void releaseBuffers(WebSocket c) throws InterruptedException {
   }

   public ByteBuffer createBuffer() {
      return ByteBuffer.allocate(16384);
   }

   protected void queue(WebSocketImpl ws) throws InterruptedException {
      if (ws.getWorkerThread() == null) {
         ws.setWorkerThread((WebSocketServer.WebSocketWorker)this.decoders.get(this.queueinvokes % this.decoders.size()));
         ++this.queueinvokes;
      }

      ws.getWorkerThread().put(ws);
   }

   private ByteBuffer takeBuffer() throws InterruptedException {
      return (ByteBuffer)this.buffers.take();
   }

   private void pushBuffer(ByteBuffer buf) throws InterruptedException {
      if (this.buffers.size() <= this.queuesize.intValue()) {
         this.buffers.put(buf);
      }
   }

   private void handleIOException(SelectionKey key, WebSocket conn, IOException ex) {
      if (key != null) {
         key.cancel();
      }

      if (conn != null) {
         conn.closeConnection(1006, ex.getMessage());
      } else if (key != null) {
         SelectableChannel channel = key.channel();
         if (channel != null && channel.isOpen()) {
            try {
               channel.close();
            } catch (IOException var6) {
               ;
            }
         }
      }

   }

   private void handleFatal(WebSocket conn, Exception e) {
      this.onError(conn, e);
      if (this.decoders != null) {
         for(WebSocketServer.WebSocketWorker w : this.decoders) {
            w.interrupt();
         }
      }

      if (this.selectorthread != null) {
         this.selectorthread.interrupt();
      }

      try {
         this.stop();
      } catch (InterruptedException var5) {
         Thread.currentThread().interrupt();
         this.onError((WebSocket)null, var5);
      }

   }

   public final void onWebsocketMessage(WebSocket conn, String message) {
      this.onMessage(conn, message);
   }

   public final void onWebsocketMessage(WebSocket conn, ByteBuffer blob) {
      this.onMessage(conn, blob);
   }

   public final void onWebsocketOpen(WebSocket conn, Handshakedata handshake) {
      if (this.addConnection(conn)) {
         this.onOpen(conn, (ClientHandshake)handshake);
      }

   }

   public final void onWebsocketClose(WebSocket conn, int code, String reason, boolean remote) {
      this.selector.wakeup();

      try {
         if (this.removeConnection(conn)) {
            this.onClose(conn, code, reason, remote);
         }
      } finally {
         try {
            this.releaseBuffers(conn);
         } catch (InterruptedException var11) {
            Thread.currentThread().interrupt();
         }

      }

   }

   protected boolean removeConnection(WebSocket ws) {
      boolean removed = false;
      synchronized(this.connections) {
         if (this.connections.contains(ws)) {
            removed = this.connections.remove(ws);
         }
      }

      if (this.isclosed.get() && this.connections.isEmpty()) {
         this.selectorthread.interrupt();
      }

      return removed;
   }

   protected boolean addConnection(WebSocket ws) {
      if (!this.isclosed.get()) {
         synchronized(this.connections) {
            return this.connections.add(ws);
         }
      } else {
         ws.close(1001);
         return true;
      }
   }

   public final void onWebsocketError(WebSocket conn, Exception ex) {
      this.onError(conn, ex);
   }

   public final void onWriteDemand(WebSocket w) {
      WebSocketImpl conn = (WebSocketImpl)w;

      try {
         conn.getSelectionKey().interestOps(5);
      } catch (CancelledKeyException var4) {
         conn.outQueue.clear();
      }

      this.selector.wakeup();
   }

   public void onWebsocketCloseInitiated(WebSocket conn, int code, String reason) {
      this.onCloseInitiated(conn, code, reason);
   }

   public void onWebsocketClosing(WebSocket conn, int code, String reason, boolean remote) {
      this.onClosing(conn, code, reason, remote);
   }

   public void onCloseInitiated(WebSocket conn, int code, String reason) {
   }

   public void onClosing(WebSocket conn, int code, String reason, boolean remote) {
   }

   public final void setWebSocketFactory(WebSocketServerFactory wsf) {
      if (this.wsf != null) {
         this.wsf.close();
      }

      this.wsf = wsf;
   }

   public final WebSocketFactory getWebSocketFactory() {
      return this.wsf;
   }

   protected boolean onConnect(SelectionKey key) {
      return true;
   }

   private Socket getSocket(WebSocket conn) {
      WebSocketImpl impl = (WebSocketImpl)conn;
      return ((SocketChannel)impl.getSelectionKey().channel()).socket();
   }

   public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
      return (InetSocketAddress)this.getSocket(conn).getLocalSocketAddress();
   }

   public InetSocketAddress getRemoteSocketAddress(WebSocket conn) {
      return (InetSocketAddress)this.getSocket(conn).getRemoteSocketAddress();
   }

   public abstract void onOpen(WebSocket var1, ClientHandshake var2);

   public abstract void onClose(WebSocket var1, int var2, String var3, boolean var4);

   public abstract void onMessage(WebSocket var1, String var2);

   public abstract void onError(WebSocket var1, Exception var2);

   public abstract void onStart();

   public void onMessage(WebSocket conn, ByteBuffer message) {
   }

   public void broadcast(String text) {
      this.broadcast(text, this.connections);
   }

   public void broadcast(byte[] data) {
      this.broadcast(data, this.connections);
   }

   public void broadcast(ByteBuffer data) {
      this.broadcast(data, this.connections);
   }

   public void broadcast(byte[] data, Collection clients) {
      if (data != null && clients != null) {
         this.broadcast(ByteBuffer.wrap(data), clients);
      } else {
         throw new IllegalArgumentException();
      }
   }

   public void broadcast(ByteBuffer data, Collection clients) {
      if (data != null && clients != null) {
         this.doBroadcast(data, clients);
      } else {
         throw new IllegalArgumentException();
      }
   }

   public void broadcast(String text, Collection clients) {
      if (text != null && clients != null) {
         this.doBroadcast(text, clients);
      } else {
         throw new IllegalArgumentException();
      }
   }

   private void doBroadcast(Object data, Collection clients) {
      String strData = null;
      if (data instanceof String) {
         strData = (String)data;
      }

      ByteBuffer byteData = null;
      if (data instanceof ByteBuffer) {
         byteData = (ByteBuffer)data;
      }

      if (strData != null || byteData != null) {
         Map draftFrames = new HashMap();
         List<WebSocket> clientCopy;
         synchronized(clients) {
            clientCopy = new ArrayList(clients);
         }

         for(WebSocket client : clientCopy) {
            if (client != null) {
               Draft draft = client.getDraft();
               this.fillFrames(draft, draftFrames, strData, byteData);

               try {
                  client.sendFrame((Collection)draftFrames.get(draft));
               } catch (WebsocketNotConnectedException var11) {
                  ;
               }
            }
         }

      }
   }

   private void fillFrames(Draft draft, Map draftFrames, String strData, ByteBuffer byteData) {
      if (!draftFrames.containsKey(draft)) {
         List frames = null;
         if (strData != null) {
            frames = draft.createFrames(strData, false);
         }

         if (byteData != null) {
            frames = draft.createFrames(byteData, false);
         }

         if (frames != null) {
            draftFrames.put(draft, frames);
         }
      }

   }

   public class WebSocketWorker extends Thread {
      private BlockingQueue iqueue = new LinkedBlockingQueue();

      public WebSocketWorker() {
         super();
         this.setName("WebSocketWorker-" + this.getId());
         this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
            }
         });
      }

      public void put(WebSocketImpl ws) throws InterruptedException {
         this.iqueue.put(ws);
      }

      public void run() {
         WebSocketImpl ws = null;

         try {
            while(true) {
               ws = (WebSocketImpl)this.iqueue.take();
               ByteBuffer buf = (ByteBuffer)ws.inQueue.poll();

               assert buf != null;

               this.doDecode(ws, buf);
               ws = null;
            }
         } catch (InterruptedException var3) {
            Thread.currentThread().interrupt();
         } catch (RuntimeException var4) {
            WebSocketServer.this.handleFatal(ws, var4);
         }

      }

      private void doDecode(WebSocketImpl ws, ByteBuffer buf) throws InterruptedException {
         try {
            ws.decode(buf);
         } catch (Exception var7) {
            ;
         } finally {
            WebSocketServer.this.pushBuffer(buf);
         }

      }
   }
}
