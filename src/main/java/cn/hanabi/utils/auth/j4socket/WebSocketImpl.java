package cn.hanabi.utils.auth.j4socket;

import cn.hanabi.utils.auth.j4socket.drafts.Draft;
import cn.hanabi.utils.auth.j4socket.drafts.Draft_6455;
import cn.hanabi.utils.auth.j4socket.enums.CloseHandshakeType;
import cn.hanabi.utils.auth.j4socket.enums.HandshakeState;
import cn.hanabi.utils.auth.j4socket.enums.Opcode;
import cn.hanabi.utils.auth.j4socket.enums.ReadyState;
import cn.hanabi.utils.auth.j4socket.enums.Role;
import cn.hanabi.utils.auth.j4socket.exceptions.IncompleteHandshakeException;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidDataException;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidHandshakeException;
import cn.hanabi.utils.auth.j4socket.exceptions.LimitExceededException;
import cn.hanabi.utils.auth.j4socket.exceptions.WebsocketNotConnectedException;
import cn.hanabi.utils.auth.j4socket.framing.CloseFrame;
import cn.hanabi.utils.auth.j4socket.framing.Framedata;
import cn.hanabi.utils.auth.j4socket.framing.PingFrame;
import cn.hanabi.utils.auth.j4socket.handshake.ClientHandshake;
import cn.hanabi.utils.auth.j4socket.handshake.ClientHandshakeBuilder;
import cn.hanabi.utils.auth.j4socket.handshake.Handshakedata;
import cn.hanabi.utils.auth.j4socket.handshake.ServerHandshake;
import cn.hanabi.utils.auth.j4socket.handshake.ServerHandshakeBuilder;
import cn.hanabi.utils.auth.j4socket.interfaces.ISSLChannel;
import cn.hanabi.utils.auth.j4socket.protocols.IProtocol;
import cn.hanabi.utils.auth.j4socket.server.WebSocketServer;
import cn.hanabi.utils.auth.j4socket.util.Charsetfunctions;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.net.ssl.SSLSession;

public class WebSocketImpl implements WebSocket {
   public static final int DEFAULT_PORT = 80;
   public static final int DEFAULT_WSS_PORT = 443;
   public static final int RCVBUF = 16384;
   public final BlockingQueue<ByteBuffer> outQueue;
   public final BlockingQueue<ByteBuffer> inQueue;
   private final WebSocketListener wsl;
   private SelectionKey key;
   private ByteChannel channel;
   private WebSocketServer.WebSocketWorker workerThread;
   private boolean flushandclosestate;
   private volatile ReadyState readyState;
   private List<Draft> knownDrafts;
   private Draft draft;
   private Role role;
   private ByteBuffer tmpHandshakeBytes;
   private ClientHandshake handshakerequest;
   private String closemessage;
   private Integer closecode;
   private Boolean closedremotely;
   private String resourceDescriptor;
   private long lastPong;
   private final Object synchronizeWriteObject;
   private Object attachment;

   public WebSocketImpl(WebSocketListener listener, List drafts) {
      this(listener, (Draft)null);
      this.role = Role.SERVER;
      if (drafts != null && !drafts.isEmpty()) {
         this.knownDrafts = drafts;
      } else {
         this.knownDrafts = new ArrayList();
         this.knownDrafts.add(new Draft_6455());
      }

   }

   public WebSocketImpl(WebSocketListener listener, Draft draft) {
      super();
      this.flushandclosestate = false;
      this.readyState = ReadyState.NOT_YET_CONNECTED;
      this.draft = null;
      this.tmpHandshakeBytes = ByteBuffer.allocate(0);
      this.handshakerequest = null;
      this.closemessage = null;
      this.closecode = null;
      this.closedremotely = null;
      this.resourceDescriptor = null;
      this.lastPong = System.nanoTime();
      this.synchronizeWriteObject = new Object();
      if (listener != null && (draft != null || this.role != Role.SERVER)) {
         this.outQueue = new LinkedBlockingQueue();
         this.inQueue = new LinkedBlockingQueue();
         this.wsl = listener;
         this.role = Role.CLIENT;
         if (draft != null) {
            this.draft = draft.copyInstance();
         }

      } else {
         throw new IllegalArgumentException("parameters must not be null");
      }
   }

   public void decode(ByteBuffer socketBuffer) {
      assert socketBuffer.hasRemaining();

      if (this.readyState != ReadyState.NOT_YET_CONNECTED) {
         if (this.readyState == ReadyState.OPEN) {
            this.decodeFrames(socketBuffer);
         }
      } else if (this.decodeHandshake(socketBuffer) && !this.isClosing() && !this.isClosed()) {
         assert this.tmpHandshakeBytes.hasRemaining() != socketBuffer.hasRemaining() || !socketBuffer.hasRemaining();

         if (socketBuffer.hasRemaining()) {
            this.decodeFrames(socketBuffer);
         } else if (this.tmpHandshakeBytes.hasRemaining()) {
            this.decodeFrames(this.tmpHandshakeBytes);
         }
      }

   }

   private boolean decodeHandshake(ByteBuffer socketBufferNew) {
      ByteBuffer socketBuffer;
      if (this.tmpHandshakeBytes.capacity() == 0) {
         socketBuffer = socketBufferNew;
      } else {
         if (this.tmpHandshakeBytes.remaining() < socketBufferNew.remaining()) {
            ByteBuffer buf = ByteBuffer.allocate(this.tmpHandshakeBytes.capacity() + socketBufferNew.remaining());
            this.tmpHandshakeBytes.flip();
            buf.put(this.tmpHandshakeBytes);
            this.tmpHandshakeBytes = buf;
         }

         this.tmpHandshakeBytes.put(socketBufferNew);
         this.tmpHandshakeBytes.flip();
         socketBuffer = this.tmpHandshakeBytes;
      }

      socketBuffer.mark();

      try {
         try {
            if (this.role == Role.SERVER) {
               if (this.draft != null) {
                  Handshakedata tmphandshake = this.draft.translateHandshake(socketBuffer);
                  if (!(tmphandshake instanceof ClientHandshake)) {
                     this.flushAndClose(1002, "wrong http function", false);
                     return false;
                  }

                  ClientHandshake handshake = (ClientHandshake)tmphandshake;
                  HandshakeState handshakestate = this.draft.acceptHandshakeAsServer(handshake);
                  if (handshakestate == HandshakeState.MATCHED) {
                     this.open(handshake);
                     return true;
                  }

                  this.close(1002, "the handshake did finally not match");
                  return false;
               }

               for(Draft d : this.knownDrafts) {
                  d = d.copyInstance();

                  try {
                     d.setParseMode(this.role);
                     socketBuffer.reset();
                     Handshakedata tmphandshake = d.translateHandshake(socketBuffer);
                     if (!(tmphandshake instanceof ClientHandshake)) {
                        this.closeConnectionDueToWrongHandshake(new InvalidDataException(1002, "wrong http function"));
                        return false;
                     }

                     ClientHandshake handshake = (ClientHandshake)tmphandshake;
                     HandshakeState handshakestate = d.acceptHandshakeAsServer(handshake);
                     if (handshakestate == HandshakeState.MATCHED) {
                        this.resourceDescriptor = handshake.getResourceDescriptor();

                        ServerHandshakeBuilder response;
                        try {
                           response = this.wsl.onWebsocketHandshakeReceivedAsServer(this, d, handshake);
                        } catch (InvalidDataException var12) {
                           this.closeConnectionDueToWrongHandshake(var12);
                           return false;
                        } catch (RuntimeException var13) {
                           this.wsl.onWebsocketError(this, var13);
                           this.closeConnectionDueToInternalServerError(var13);
                           return false;
                        }

                        this.write(d.createHandshake(d.postProcessHandshakeResponseAsServer(handshake, response)));
                        this.draft = d;
                        this.open(handshake);
                        return true;
                     }
                  } catch (InvalidHandshakeException var14) {
                     ;
                  }
               }

               if (this.draft == null) {
                  ;
               }

               return false;
            }

            if (this.role == Role.CLIENT) {
               this.draft.setParseMode(this.role);
               Handshakedata tmphandshake = this.draft.translateHandshake(socketBuffer);
               if (!(tmphandshake instanceof ServerHandshake)) {
                  this.flushAndClose(1002, "wrong http function", false);
                  return false;
               }

               ServerHandshake handshake = (ServerHandshake)tmphandshake;
               HandshakeState handshakestate = this.draft.acceptHandshakeAsClient(this.handshakerequest, handshake);
               if (handshakestate == HandshakeState.MATCHED) {
                  try {
                     this.wsl.onWebsocketHandshakeReceivedAsClient(this, this.handshakerequest, handshake);
                  } catch (InvalidDataException var10) {
                     this.flushAndClose(var10.getCloseCode(), var10.getMessage(), false);
                     return false;
                  } catch (RuntimeException var11) {
                     this.wsl.onWebsocketError(this, var11);
                     this.flushAndClose(-1, var11.getMessage(), false);
                     return false;
                  }

                  this.open(handshake);
                  return true;
               }

               this.close(1002, "draft " + this.draft + " refuses handshake");
            }
         } catch (InvalidHandshakeException var15) {
            this.close(var15);
         }
      } catch (IncompleteHandshakeException var16) {
         if (this.tmpHandshakeBytes.capacity() == 0) {
            socketBuffer.reset();
            int newsize = var16.getPreferredSize();
            if (newsize == 0) {
               newsize = socketBuffer.capacity() + 16;
            } else {
               assert var16.getPreferredSize() >= socketBuffer.remaining();
            }

            this.tmpHandshakeBytes = ByteBuffer.allocate(newsize);
            this.tmpHandshakeBytes.put(socketBufferNew);
         } else {
            this.tmpHandshakeBytes.position(this.tmpHandshakeBytes.limit());
            this.tmpHandshakeBytes.limit(this.tmpHandshakeBytes.capacity());
         }
      }

      return false;
   }

   private void decodeFrames(ByteBuffer socketBuffer) {
      try {
         for(Framedata f : this.draft.translateFrame(socketBuffer)) {
            this.draft.processFrame(this, f);
         }
      } catch (LimitExceededException var5) {
         if (var5.getLimit() == Integer.MAX_VALUE) {
            this.wsl.onWebsocketError(this, var5);
         }

         this.close(var5);
      } catch (InvalidDataException var6) {
         this.wsl.onWebsocketError(this, var6);
         this.close(var6);
      }

   }

   private void closeConnectionDueToWrongHandshake(InvalidDataException exception) {
      this.write(this.generateHttpResponseDueToError(404));
      this.flushAndClose(exception.getCloseCode(), exception.getMessage(), false);
   }

   private void closeConnectionDueToInternalServerError(RuntimeException exception) {
      this.write(this.generateHttpResponseDueToError(500));
      this.flushAndClose(-1, exception.getMessage(), false);
   }

   private ByteBuffer generateHttpResponseDueToError(int errorCode) {
      String errorCodeDescription;
      switch(errorCode) {
      case 404:
         errorCodeDescription = "404 WebSocket Upgrade Failure";
         break;
      case 500:
      default:
         errorCodeDescription = "500 Internal Server Error";
      }

      return ByteBuffer.wrap(Charsetfunctions.asciiBytes("HTTP/1.1 " + errorCodeDescription + "\r\nContent-Type: text/html\r\nServer: TooTallNate Java-WebSocket\r\nContent-Length: " + (48 + errorCodeDescription.length()) + "\r\n\r\n<html><head></head><body><h1>" + errorCodeDescription + "</h1></body></html>"));
   }

   public synchronized void close(int code, String message, boolean remote) {
      if (this.readyState != ReadyState.CLOSING && this.readyState != ReadyState.CLOSED) {
         if (this.readyState == ReadyState.OPEN) {
            if (code == 1006) {
               assert !remote;

               this.readyState = ReadyState.CLOSING;
               this.flushAndClose(code, message, false);
               return;
            }

            if (this.draft.getCloseHandshakeType() != CloseHandshakeType.NONE) {
               try {
                  if (!remote) {
                     try {
                        this.wsl.onWebsocketCloseInitiated(this, code, message);
                     } catch (RuntimeException var5) {
                        this.wsl.onWebsocketError(this, var5);
                     }
                  }

                  if (this.isOpen()) {
                     CloseFrame closeFrame = new CloseFrame();
                     closeFrame.setReason(message);
                     closeFrame.setCode(code);
                     closeFrame.isValid();
                     this.sendFrame(closeFrame);
                  }
               } catch (InvalidDataException var6) {
                  this.wsl.onWebsocketError(this, var6);
                  this.flushAndClose(1006, "generated frame is invalid", false);
               }
            }

            this.flushAndClose(code, message, remote);
         } else if (code == -3) {
            assert remote;

            this.flushAndClose(-3, message, true);
         } else if (code == 1002) {
            this.flushAndClose(code, message, remote);
         } else {
            this.flushAndClose(-1, message, false);
         }

         this.readyState = ReadyState.CLOSING;
         this.tmpHandshakeBytes = null;
      }
   }

   public void close(int code, String message) {
      this.close(code, message, false);
   }

   public synchronized void closeConnection(int code, String message, boolean remote) {
      if (this.readyState != ReadyState.CLOSED) {
         if (this.readyState == ReadyState.OPEN && code == 1006) {
            this.readyState = ReadyState.CLOSING;
         }

         if (this.key != null) {
            this.key.cancel();
         }

         if (this.channel != null) {
            try {
               this.channel.close();
            } catch (IOException var6) {
               if (var6.getMessage() == null || !var6.getMessage().equals("Broken pipe")) {
                  this.wsl.onWebsocketError(this, var6);
               }
            }
         }

         try {
            this.wsl.onWebsocketClose(this, code, message, remote);
         } catch (RuntimeException var5) {
            this.wsl.onWebsocketError(this, var5);
         }

         if (this.draft != null) {
            this.draft.reset();
         }

         this.handshakerequest = null;
         this.readyState = ReadyState.CLOSED;
      }
   }

   protected void closeConnection(int code, boolean remote) {
      this.closeConnection(code, "", remote);
   }

   public void closeConnection() {
      if (this.closedremotely == null) {
         throw new IllegalStateException("this method must be used in conjunction with flushAndClose");
      } else {
         this.closeConnection(this.closecode.intValue(), this.closemessage, this.closedremotely.booleanValue());
      }
   }

   public void closeConnection(int code, String message) {
      this.closeConnection(code, message, false);
   }

   public synchronized void flushAndClose(int code, String message, boolean remote) {
      if (!this.flushandclosestate) {
         this.closecode = code;
         this.closemessage = message;
         this.closedremotely = remote;
         this.flushandclosestate = true;
         this.wsl.onWriteDemand(this);

         try {
            this.wsl.onWebsocketClosing(this, code, message, remote);
         } catch (RuntimeException var5) {
            this.wsl.onWebsocketError(this, var5);
         }

         if (this.draft != null) {
            this.draft.reset();
         }

         this.handshakerequest = null;
      }
   }

   public void eot() {
      if (this.readyState == ReadyState.NOT_YET_CONNECTED) {
         this.closeConnection(-1, true);
      } else if (this.flushandclosestate) {
         this.closeConnection(this.closecode.intValue(), this.closemessage, this.closedremotely.booleanValue());
      } else if (this.draft.getCloseHandshakeType() == CloseHandshakeType.NONE) {
         this.closeConnection(1000, true);
      } else if (this.draft.getCloseHandshakeType() == CloseHandshakeType.ONEWAY) {
         if (this.role == Role.SERVER) {
            this.closeConnection(1006, true);
         } else {
            this.closeConnection(1000, true);
         }
      } else {
         this.closeConnection(1006, true);
      }

   }

   public void close(int code) {
      this.close(code, "", false);
   }

   public void close(InvalidDataException e) {
      this.close(e.getCloseCode(), e.getMessage(), false);
   }

   public void send(String text) {
      if (text == null) {
         throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
      } else {
         this.send((Collection)this.draft.createFrames(text, this.role == Role.CLIENT));
      }
   }

   public void send(ByteBuffer bytes) {
      if (bytes == null) {
         throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
      } else {
         this.send((Collection)this.draft.createFrames(bytes, this.role == Role.CLIENT));
      }
   }

   public void send(byte[] bytes) {
      this.send(ByteBuffer.wrap(bytes));
   }

   private void send(Collection<Framedata> frames) {
      if (!this.isOpen()) {
         throw new WebsocketNotConnectedException();
      } else if (frames == null) {
         throw new IllegalArgumentException();
      } else {
         ArrayList outgoingFrames = new ArrayList();

         for(Framedata f : frames) {
            outgoingFrames.add(this.draft.createBinaryFrame(f));
         }

         this.write(outgoingFrames);
      }
   }

   public void sendFragmentedFrame(Opcode op, ByteBuffer buffer, boolean fin) {
      this.send((Collection)this.draft.continuousFrame(op, buffer, fin));
   }

   public void sendFrame(Collection frames) {
      this.send(frames);
   }

   public void sendFrame(Framedata framedata) {
      this.send((Collection)Collections.singletonList(framedata));
   }

   public void sendPing() throws NullPointerException {
      PingFrame pingFrame = this.wsl.onPreparePing(this);
      if (pingFrame == null) {
         throw new NullPointerException("onPreparePing(WebSocket) returned null. PingFrame to sent can't be null.");
      } else {
         this.sendFrame(pingFrame);
      }
   }

   public boolean hasBufferedData() {
      return !this.outQueue.isEmpty();
   }

   public void startHandshake(ClientHandshakeBuilder handshakedata) throws InvalidHandshakeException {
      this.handshakerequest = this.draft.postProcessHandshakeRequestAsClient(handshakedata);
      this.resourceDescriptor = handshakedata.getResourceDescriptor();

      assert this.resourceDescriptor != null;

      try {
         this.wsl.onWebsocketHandshakeSentAsClient(this, this.handshakerequest);
      } catch (InvalidDataException var3) {
         throw new InvalidHandshakeException("Handshake data rejected by client.");
      } catch (RuntimeException var4) {
         this.wsl.onWebsocketError(this, var4);
         throw new InvalidHandshakeException("rejected because of " + var4);
      }

      this.write(this.draft.createHandshake(this.handshakerequest));
   }

   private void write(ByteBuffer buf) {
      this.outQueue.add(buf);
      this.wsl.onWriteDemand(this);
   }

   private void write(List<ByteBuffer> bufs) {
      synchronized(this.synchronizeWriteObject) {
         for(ByteBuffer b : bufs) {
            this.write(b);
         }

      }
   }

   private void open(Handshakedata d) {
      this.readyState = ReadyState.OPEN;

      try {
         this.wsl.onWebsocketOpen(this, d);
      } catch (RuntimeException var3) {
         this.wsl.onWebsocketError(this, var3);
      }

   }

   public boolean isOpen() {
      return this.readyState == ReadyState.OPEN;
   }

   public boolean isClosing() {
      return this.readyState == ReadyState.CLOSING;
   }

   public boolean isFlushAndClose() {
      return this.flushandclosestate;
   }

   public boolean isClosed() {
      return this.readyState == ReadyState.CLOSED;
   }

   public ReadyState getReadyState() {
      return this.readyState;
   }

   public void setSelectionKey(SelectionKey key) {
      this.key = key;
   }

   public SelectionKey getSelectionKey() {
      return this.key;
   }

   public String toString() {
      return super.toString();
   }

   public InetSocketAddress getRemoteSocketAddress() {
      return this.wsl.getRemoteSocketAddress(this);
   }

   public InetSocketAddress getLocalSocketAddress() {
      return this.wsl.getLocalSocketAddress(this);
   }

   public Draft getDraft() {
      return this.draft;
   }

   public void close() {
      this.close(1000);
   }

   public String getResourceDescriptor() {
      return this.resourceDescriptor;
   }

   long getLastPong() {
      return this.lastPong;
   }

   public void updateLastPong() {
      this.lastPong = System.nanoTime();
   }

   public WebSocketListener getWebSocketListener() {
      return this.wsl;
   }

   public Object getAttachment() {
      return this.attachment;
   }

   public boolean hasSSLSupport() {
      return this.channel instanceof ISSLChannel;
   }

   public SSLSession getSSLSession() {
      if (!this.hasSSLSupport()) {
         throw new IllegalArgumentException("This websocket uses ws instead of wss. No SSLSession available.");
      } else {
         return ((ISSLChannel)this.channel).getSSLEngine().getSession();
      }
   }

   public IProtocol getProtocol() {
      if (this.draft == null) {
         return null;
      } else if (!(this.draft instanceof Draft_6455)) {
         throw new IllegalArgumentException("This draft does not support Sec-WebSocket-Protocol");
      } else {
         return ((Draft_6455)this.draft).getProtocol();
      }
   }

   public void setAttachment(Object attachment) {
      this.attachment = attachment;
   }

   public ByteChannel getChannel() {
      return this.channel;
   }

   public void setChannel(ByteChannel channel) {
      this.channel = channel;
   }

   public WebSocketServer.WebSocketWorker getWorkerThread() {
      return this.workerThread;
   }

   public void setWorkerThread(WebSocketServer.WebSocketWorker workerThread) {
      this.workerThread = workerThread;
   }
}
