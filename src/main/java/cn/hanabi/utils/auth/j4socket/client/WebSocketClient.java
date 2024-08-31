package cn.hanabi.utils.auth.j4socket.client;

import cn.hanabi.utils.auth.j4socket.AbstractWebSocket;
import cn.hanabi.utils.auth.j4socket.WebSocket;
import cn.hanabi.utils.auth.j4socket.WebSocketImpl;
import cn.hanabi.utils.auth.j4socket.drafts.Draft;
import cn.hanabi.utils.auth.j4socket.drafts.Draft_6455;
import cn.hanabi.utils.auth.j4socket.enums.Opcode;
import cn.hanabi.utils.auth.j4socket.enums.ReadyState;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidHandshakeException;
import cn.hanabi.utils.auth.j4socket.framing.Framedata;
import cn.hanabi.utils.auth.j4socket.handshake.HandshakeImpl1Client;
import cn.hanabi.utils.auth.j4socket.handshake.Handshakedata;
import cn.hanabi.utils.auth.j4socket.handshake.ServerHandshake;
import cn.hanabi.utils.auth.j4socket.protocols.IProtocol;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public abstract class WebSocketClient extends AbstractWebSocket implements Runnable, WebSocket {
   protected URI uri;
   private WebSocketImpl engine;
   private Socket socket;
   private SocketFactory socketFactory;
   private OutputStream ostream;
   private Proxy proxy;
   private Thread writeThread;
   private Thread connectReadThread;
   private Draft draft;
   private Map<String, String> headers;
   private CountDownLatch connectLatch;
   private CountDownLatch closeLatch;
   private int connectTimeout;
   private DnsResolver dnsResolver;

   public WebSocketClient(URI serverUri) {
      this(serverUri, new Draft_6455());
   }

   public WebSocketClient(URI serverUri, Draft protocolDraft) {
      this(serverUri, protocolDraft, (Map)null, 0);
   }

   public WebSocketClient(URI serverUri, Map httpHeaders) {
      this(serverUri, new Draft_6455(), httpHeaders);
   }

   public WebSocketClient(URI serverUri, Draft protocolDraft, Map httpHeaders) {
      this(serverUri, protocolDraft, httpHeaders, 0);
   }

   public WebSocketClient(URI serverUri, Draft protocolDraft, Map httpHeaders, int connectTimeout) {
      super();
      this.uri = null;
      this.engine = null;
      this.socket = null;
      this.socketFactory = null;
      this.proxy = Proxy.NO_PROXY;
      this.connectLatch = new CountDownLatch(1);
      this.closeLatch = new CountDownLatch(1);
      this.connectTimeout = 0;
      this.dnsResolver = null;
      if (serverUri == null) {
         throw new IllegalArgumentException();
      } else if (protocolDraft == null) {
         throw new IllegalArgumentException("null as draft is permitted for `WebSocketServer` only!");
      } else {
         this.uri = serverUri;
         this.draft = protocolDraft;
         this.dnsResolver = new DnsResolver() {
            public InetAddress resolve(URI uri) throws UnknownHostException {
               return InetAddress.getByName(uri.getHost());
            }
         };
         if (httpHeaders != null) {
            this.headers = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            this.headers.putAll(httpHeaders);
         }

         this.connectTimeout = connectTimeout;
         this.setTcpNoDelay(false);
         this.setReuseAddr(false);
         this.engine = new WebSocketImpl(this, protocolDraft);
      }
   }

   public URI getURI() {
      return this.uri;
   }

   public Draft getDraft() {
      return this.draft;
   }

   public Socket getSocket() {
      return this.socket;
   }

   public void addHeader(String key, String value) {
      if (this.headers == null) {
         this.headers = new TreeMap(String.CASE_INSENSITIVE_ORDER);
      }

      this.headers.put(key, value);
   }

   public String removeHeader(String key) {
      return this.headers == null ? null : (String)this.headers.remove(key);
   }

   public void clearHeaders() {
      this.headers = null;
   }

   public void setDnsResolver(DnsResolver dnsResolver) {
      this.dnsResolver = dnsResolver;
   }

   public void reconnect() {
      this.reset();
      this.connect();
   }

   public boolean reconnectBlocking() throws InterruptedException {
      this.reset();
      return this.connectBlocking();
   }

   private void reset() {
      Thread current = Thread.currentThread();
      if (current != this.writeThread && current != this.connectReadThread) {
         try {
            this.closeBlocking();
            if (this.writeThread != null) {
               this.writeThread.interrupt();
               this.writeThread = null;
            }

            if (this.connectReadThread != null) {
               this.connectReadThread.interrupt();
               this.connectReadThread = null;
            }

            this.draft.reset();
            if (this.socket != null) {
               this.socket.close();
               this.socket = null;
            }
         } catch (Exception var3) {
            this.onError(var3);
            this.engine.closeConnection(1006, var3.getMessage());
            return;
         }

         this.connectLatch = new CountDownLatch(1);
         this.closeLatch = new CountDownLatch(1);
         this.engine = new WebSocketImpl(this, this.draft);
      } else {
         throw new IllegalStateException("You cannot initialize a reconnect out of the websocket thread. Use reconnect in another thread to ensure a successful cleanup.");
      }
   }

   public void connect() {
      if (this.connectReadThread != null) {
         throw new IllegalStateException("WebSocketClient objects are not reuseable");
      } else {
         this.connectReadThread = new Thread(this);
         this.connectReadThread.setName("WebSocketConnectReadThread-" + this.connectReadThread.getId());
         this.connectReadThread.start();
      }
   }

   public boolean connectBlocking() throws InterruptedException {
      this.connect();
      this.connectLatch.await();
      return this.engine.isOpen();
   }

   public boolean connectBlocking(long timeout, TimeUnit timeUnit) throws InterruptedException {
      this.connect();
      return this.connectLatch.await(timeout, timeUnit) && this.engine.isOpen();
   }

   public void close() {
      if (this.writeThread != null) {
         this.engine.close(1000);
      }

   }

   public void closeBlocking() throws InterruptedException {
      this.close();
      this.closeLatch.await();
   }

   public void send(String text) {
      this.engine.send(text);
   }

   public void send(byte[] data) {
      this.engine.send(data);
   }

   public Object getAttachment() {
      return this.engine.getAttachment();
   }

   public void setAttachment(Object attachment) {
      this.engine.setAttachment(attachment);
   }

   protected Collection getConnections() {
      return Collections.singletonList(this.engine);
   }

   public void sendPing() {
      this.engine.sendPing();
   }

   public void run() {
      InputStream istream;
      try {
         boolean upgradeSocketToSSLSocket = this.prepareSocket();
         this.socket.setTcpNoDelay(this.isTcpNoDelay());
         this.socket.setReuseAddress(this.isReuseAddr());
         if (!this.socket.isConnected()) {
            InetSocketAddress addr = new InetSocketAddress(this.dnsResolver.resolve(this.uri), this.getPort());
            this.socket.connect(addr, this.connectTimeout);
         }

         if (upgradeSocketToSSLSocket && "wss".equals(this.uri.getScheme())) {
            this.upgradeSocketToSSL();
         }

         if (this.socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket)this.socket;
            SSLParameters sslParameters = sslSocket.getSSLParameters();
            this.onSetSSLParameters(sslParameters);
            sslSocket.setSSLParameters(sslParameters);
         }

         istream = this.socket.getInputStream();
         this.ostream = this.socket.getOutputStream();
         this.sendHandshake();
      } catch (Exception var7) {
         this.onWebsocketError(this.engine, var7);
         this.engine.closeConnection(-1, var7.getMessage());
         return;
      } catch (InternalError var8) {
         if (var8.getCause() instanceof InvocationTargetException && var8.getCause().getCause() instanceof IOException) {
            IOException cause = (IOException)var8.getCause().getCause();
            this.onWebsocketError(this.engine, cause);
            this.engine.closeConnection(-1, cause.getMessage());
            return;
         }

         throw var8;
      }

      this.writeThread = new Thread(new WebSocketClient.WebsocketWriteThread(this));
      this.writeThread.start();
      byte[] rawbuffer = new byte[16384];

      try {
         int readBytes;
         while(!this.isClosing() && !this.isClosed() && (readBytes = istream.read(rawbuffer)) != -1) {
            this.engine.decode(ByteBuffer.wrap(rawbuffer, 0, readBytes));
         }

         this.engine.eot();
      } catch (IOException var5) {
         this.handleIOException(var5);
      } catch (RuntimeException var6) {
         this.onError(var6);
         this.engine.closeConnection(1006, var6.getMessage());
      }

      this.connectReadThread = null;
   }

   private void upgradeSocketToSSL() throws NoSuchAlgorithmException, KeyManagementException, IOException {
      SSLSocketFactory factory;
      if (this.socketFactory instanceof SSLSocketFactory) {
         factory = (SSLSocketFactory)this.socketFactory;
      } else {
         SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
         sslContext.init((KeyManager[])null, (TrustManager[])null, (SecureRandom)null);
         factory = sslContext.getSocketFactory();
      }

      this.socket = factory.createSocket(this.socket, this.uri.getHost(), this.getPort(), true);
   }

   private boolean prepareSocket() throws IOException {
      boolean upgradeSocketToSSLSocket = false;
      if (this.proxy != Proxy.NO_PROXY) {
         this.socket = new Socket(this.proxy);
         upgradeSocketToSSLSocket = true;
      } else if (this.socketFactory != null) {
         this.socket = this.socketFactory.createSocket();
      } else if (this.socket == null) {
         this.socket = new Socket(this.proxy);
         upgradeSocketToSSLSocket = true;
      } else if (this.socket.isClosed()) {
         throw new IOException();
      }

      return upgradeSocketToSSLSocket;
   }

   protected void onSetSSLParameters(SSLParameters sslParameters) {
      sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
   }

   private int getPort() {
      int port = this.uri.getPort();
      String scheme = this.uri.getScheme();
      if ("wss".equals(scheme)) {
         return port == -1 ? 443 : port;
      } else if ("ws".equals(scheme)) {
         return port == -1 ? 80 : port;
      } else {
         throw new IllegalArgumentException("unknown scheme: " + scheme);
      }
   }

   private void sendHandshake() throws InvalidHandshakeException {
      String part1 = this.uri.getRawPath();
      String part2 = this.uri.getRawQuery();
      String path;
      if (part1 != null && part1.length() != 0) {
         path = part1;
      } else {
         path = "/";
      }

      if (part2 != null) {
         path = path + '?' + part2;
      }

      int port = this.getPort();
      String host = this.uri.getHost() + (port != 80 && port != 443 ? ":" + port : "");
      HandshakeImpl1Client handshake = new HandshakeImpl1Client();
      handshake.setResourceDescriptor(path);
      handshake.put("Host", host);
      if (this.headers != null) {
         for(Entry kv : this.headers.entrySet()) {
            handshake.put((String)kv.getKey(), (String)kv.getValue());
         }
      }

      this.engine.startHandshake(handshake);
   }

   public ReadyState getReadyState() {
      return this.engine.getReadyState();
   }

   public final void onWebsocketMessage(WebSocket conn, String message) {
      this.onMessage(message);
   }

   public final void onWebsocketMessage(WebSocket conn, ByteBuffer blob) {
      this.onMessage(blob);
   }

   public final void onWebsocketOpen(WebSocket conn, Handshakedata handshake) {
      this.startConnectionLostTimer();
      this.onOpen((ServerHandshake)handshake);
      this.connectLatch.countDown();
   }

   public final void onWebsocketClose(WebSocket conn, int code, String reason, boolean remote) {
      this.stopConnectionLostTimer();
      if (this.writeThread != null) {
         this.writeThread.interrupt();
      }

      this.onClose(code, reason, remote);
      this.connectLatch.countDown();
      this.closeLatch.countDown();
   }

   public final void onWebsocketError(WebSocket conn, Exception ex) {
      this.onError(ex);
   }

   public final void onWriteDemand(WebSocket conn) {
   }

   public void onWebsocketCloseInitiated(WebSocket conn, int code, String reason) {
      this.onCloseInitiated(code, reason);
   }

   public void onWebsocketClosing(WebSocket conn, int code, String reason, boolean remote) {
      this.onClosing(code, reason, remote);
   }

   public void onCloseInitiated(int code, String reason) {
   }

   public void onClosing(int code, String reason, boolean remote) {
   }

   public WebSocket getConnection() {
      return this.engine;
   }

   public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
      return this.socket != null ? (InetSocketAddress)this.socket.getLocalSocketAddress() : null;
   }

   public InetSocketAddress getRemoteSocketAddress(WebSocket conn) {
      return this.socket != null ? (InetSocketAddress)this.socket.getRemoteSocketAddress() : null;
   }

   public abstract void onOpen(ServerHandshake var1);

   public abstract void onMessage(String var1);

   public abstract void onClose(int var1, String var2, boolean var3);

   public abstract void onError(Exception var1);

   public void onMessage(ByteBuffer bytes) {
   }

   public void setProxy(Proxy proxy) {
      if (proxy == null) {
         throw new IllegalArgumentException();
      } else {
         this.proxy = proxy;
      }
   }

   /** @deprecated */
   @Deprecated
   public void setSocket(Socket socket) {
      if (this.socket != null) {
         throw new IllegalStateException("socket has already been set");
      } else {
         this.socket = socket;
      }
   }

   public void setSocketFactory(SocketFactory socketFactory) {
      this.socketFactory = socketFactory;
   }

   public void sendFragmentedFrame(Opcode op, ByteBuffer buffer, boolean fin) {
      this.engine.sendFragmentedFrame(op, buffer, fin);
   }

   public boolean isOpen() {
      return this.engine.isOpen();
   }

   public boolean isFlushAndClose() {
      return this.engine.isFlushAndClose();
   }

   public boolean isClosed() {
      return this.engine.isClosed();
   }

   public boolean isClosing() {
      return this.engine.isClosing();
   }

   public boolean hasBufferedData() {
      return this.engine.hasBufferedData();
   }

   public void close(int code) {
      this.engine.close(code);
   }

   public void close(int code, String message) {
      this.engine.close(code, message);
   }

   public void closeConnection(int code, String message) {
      this.engine.closeConnection(code, message);
   }

   public void send(ByteBuffer bytes) {
      this.engine.send(bytes);
   }

   public void sendFrame(Framedata framedata) {
      this.engine.sendFrame(framedata);
   }

   public void sendFrame(Collection frames) {
      this.engine.sendFrame(frames);
   }

   public InetSocketAddress getLocalSocketAddress() {
      return this.engine.getLocalSocketAddress();
   }

   public InetSocketAddress getRemoteSocketAddress() {
      return this.engine.getRemoteSocketAddress();
   }

   public String getResourceDescriptor() {
      return this.uri.getPath();
   }

   public boolean hasSSLSupport() {
      return this.engine.hasSSLSupport();
   }

   public SSLSession getSSLSession() {
      return this.engine.getSSLSession();
   }

   public IProtocol getProtocol() {
      return this.engine.getProtocol();
   }

   private void handleIOException(IOException e) {
      if (e instanceof SSLException) {
         this.onError(e);
      }

      this.engine.eot();
   }

   private class WebsocketWriteThread implements Runnable {
      private final WebSocketClient webSocketClient;

      WebsocketWriteThread(WebSocketClient webSocketClient) {
         super();
         this.webSocketClient = webSocketClient;
      }

      public void run() {
         Thread.currentThread().setName("WebSocketWriteThread-" + Thread.currentThread().getId());

         try {
            this.runWriteData();
         } catch (IOException var5) {
            WebSocketClient.this.handleIOException(var5);
         } finally {
            this.closeSocket();
            WebSocketClient.this.writeThread = null;
         }

      }

      private void runWriteData() throws IOException {
         while(true) {
            try {
               if (!Thread.interrupted()) {
                  ByteBuffer buffer = (ByteBuffer)WebSocketClient.this.engine.outQueue.take();
                  WebSocketClient.this.ostream.write(buffer.array(), 0, buffer.limit());
                  WebSocketClient.this.ostream.flush();
                  continue;
               }
            } catch (InterruptedException var4) {
               for(ByteBuffer buffer : WebSocketClient.this.engine.outQueue) {
                  WebSocketClient.this.ostream.write(buffer.array(), 0, buffer.limit());
                  WebSocketClient.this.ostream.flush();
               }

               Thread.currentThread().interrupt();
            }

            return;
         }
      }

      private void closeSocket() {
         try {
            if (WebSocketClient.this.socket != null) {
               WebSocketClient.this.socket.close();
            }
         } catch (IOException var2) {
            WebSocketClient.this.onWebsocketError(this.webSocketClient, var2);
         }

      }
   }
}
