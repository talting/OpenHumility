package cn.hanabi.utils.auth.j4socket.drafts;

import cn.hanabi.utils.auth.j4socket.WebSocketImpl;
import cn.hanabi.utils.auth.j4socket.enums.CloseHandshakeType;
import cn.hanabi.utils.auth.j4socket.enums.HandshakeState;
import cn.hanabi.utils.auth.j4socket.enums.Opcode;
import cn.hanabi.utils.auth.j4socket.enums.ReadyState;
import cn.hanabi.utils.auth.j4socket.enums.Role;
import cn.hanabi.utils.auth.j4socket.exceptions.IncompleteException;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidDataException;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidFrameException;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidHandshakeException;
import cn.hanabi.utils.auth.j4socket.exceptions.LimitExceededException;
import cn.hanabi.utils.auth.j4socket.exceptions.NotSendableException;
import cn.hanabi.utils.auth.j4socket.extensions.DefaultExtension;
import cn.hanabi.utils.auth.j4socket.extensions.IExtension;
import cn.hanabi.utils.auth.j4socket.framing.BinaryFrame;
import cn.hanabi.utils.auth.j4socket.framing.CloseFrame;
import cn.hanabi.utils.auth.j4socket.framing.Framedata;
import cn.hanabi.utils.auth.j4socket.framing.FramedataImpl1;
import cn.hanabi.utils.auth.j4socket.framing.TextFrame;
import cn.hanabi.utils.auth.j4socket.handshake.ClientHandshake;
import cn.hanabi.utils.auth.j4socket.handshake.ClientHandshakeBuilder;
import cn.hanabi.utils.auth.j4socket.handshake.HandshakeBuilder;
import cn.hanabi.utils.auth.j4socket.handshake.ServerHandshake;
import cn.hanabi.utils.auth.j4socket.handshake.ServerHandshakeBuilder;
import cn.hanabi.utils.auth.j4socket.protocols.IProtocol;
import cn.hanabi.utils.auth.j4socket.protocols.Protocol;
import cn.hanabi.utils.auth.j4socket.util.Base64;
import cn.hanabi.utils.auth.j4socket.util.Charsetfunctions;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Draft_6455 extends Draft {
   private static final String SEC_WEB_SOCKET_KEY = "Sec-WebSocket-Key";
   private static final String SEC_WEB_SOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
   private static final String SEC_WEB_SOCKET_EXTENSIONS = "Sec-WebSocket-Extensions";
   private static final String SEC_WEB_SOCKET_ACCEPT = "Sec-WebSocket-Accept";
   private static final String UPGRADE = "Upgrade";
   private static final String CONNECTION = "Connection";
   private IExtension extension;
   private List<IExtension> knownExtensions;
   private IProtocol protocol;
   private List<IProtocol> knownProtocols;
   private Framedata currentContinuousFrame;
   private final List<ByteBuffer> byteBufferList;
   private ByteBuffer incompleteframe;
   private final SecureRandom reuseableRandom;
   private int maxFrameSize;

   public Draft_6455() {
      this(Collections.emptyList());
   }

   public Draft_6455(IExtension inputExtension) {
      this(Collections.singletonList(inputExtension));
   }

   public Draft_6455(List inputExtensions) {
      this(inputExtensions, Collections.singletonList(new Protocol("")));
   }

   public Draft_6455(List inputExtensions, List inputProtocols) {
      this(inputExtensions, inputProtocols, Integer.MAX_VALUE);
   }

   public Draft_6455(List inputExtensions, int inputMaxFrameSize) {
      this(inputExtensions, Collections.singletonList(new Protocol("")), inputMaxFrameSize);
   }

   public Draft_6455(List<IExtension> inputExtensions, List inputProtocols, int inputMaxFrameSize) {
      super();
      this.extension = new DefaultExtension();
      this.reuseableRandom = new SecureRandom();
      if (inputExtensions != null && inputProtocols != null && inputMaxFrameSize >= 1) {
         this.knownExtensions = new ArrayList(inputExtensions.size());
         this.knownProtocols = new ArrayList(inputProtocols.size());
         boolean hasDefault = false;
         this.byteBufferList = new ArrayList();

         for(IExtension inputExtension : inputExtensions) {
            if (inputExtension.getClass().equals(DefaultExtension.class)) {
               hasDefault = true;
            }
         }

         this.knownExtensions.addAll(inputExtensions);
         if (!hasDefault) {
            this.knownExtensions.add(this.knownExtensions.size(), this.extension);
         }

         this.knownProtocols.addAll(inputProtocols);
         this.maxFrameSize = inputMaxFrameSize;
      } else {
         throw new IllegalArgumentException();
      }
   }

   public HandshakeState acceptHandshakeAsServer(ClientHandshake handshakedata) throws InvalidHandshakeException {
      int v = this.readVersion(handshakedata);
      if (v != 13) {
         return HandshakeState.NOT_MATCHED;
      } else {
         HandshakeState extensionState = HandshakeState.NOT_MATCHED;
         String requestedExtension = handshakedata.getFieldValue("Sec-WebSocket-Extensions");

         for(IExtension knownExtension : this.knownExtensions) {
            if (knownExtension.acceptProvidedExtensionAsServer(requestedExtension)) {
               this.extension = knownExtension;
               extensionState = HandshakeState.MATCHED;
               break;
            }
         }

         HandshakeState protocolState = this.containsRequestedProtocol(handshakedata.getFieldValue("Sec-WebSocket-Protocol"));
         return protocolState == HandshakeState.MATCHED && extensionState == HandshakeState.MATCHED ? HandshakeState.MATCHED : HandshakeState.NOT_MATCHED;
      }
   }

   private HandshakeState containsRequestedProtocol(String requestedProtocol) {
      for(IProtocol knownProtocol : this.knownProtocols) {
         if (knownProtocol.acceptProvidedProtocol(requestedProtocol)) {
            this.protocol = knownProtocol;
            return HandshakeState.MATCHED;
         }
      }

      return HandshakeState.NOT_MATCHED;
   }

   public HandshakeState acceptHandshakeAsClient(ClientHandshake request, ServerHandshake response) throws InvalidHandshakeException {
      if (!this.basicAccept(response)) {
         return HandshakeState.NOT_MATCHED;
      } else if (request.hasFieldValue("Sec-WebSocket-Key") && response.hasFieldValue("Sec-WebSocket-Accept")) {
         String seckeyAnswer = response.getFieldValue("Sec-WebSocket-Accept");
         String seckeyChallenge = request.getFieldValue("Sec-WebSocket-Key");
         seckeyChallenge = this.generateFinalKey(seckeyChallenge);
         if (!seckeyChallenge.equals(seckeyAnswer)) {
            return HandshakeState.NOT_MATCHED;
         } else {
            HandshakeState extensionState = HandshakeState.NOT_MATCHED;
            String requestedExtension = response.getFieldValue("Sec-WebSocket-Extensions");

            for(IExtension knownExtension : this.knownExtensions) {
               if (knownExtension.acceptProvidedExtensionAsClient(requestedExtension)) {
                  this.extension = knownExtension;
                  extensionState = HandshakeState.MATCHED;
                  break;
               }
            }

            HandshakeState protocolState = this.containsRequestedProtocol(response.getFieldValue("Sec-WebSocket-Protocol"));
            return protocolState == HandshakeState.MATCHED && extensionState == HandshakeState.MATCHED ? HandshakeState.MATCHED : HandshakeState.NOT_MATCHED;
         }
      } else {
         return HandshakeState.NOT_MATCHED;
      }
   }

   public IExtension getExtension() {
      return this.extension;
   }

   public List<IExtension> getKnownExtensions() {
      return this.knownExtensions;
   }

   public IProtocol getProtocol() {
      return this.protocol;
   }

   public int getMaxFrameSize() {
      return this.maxFrameSize;
   }

   public List<IProtocol> getKnownProtocols() {
      return this.knownProtocols;
   }

   public ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder request) {
      request.put("Upgrade", "websocket");
      request.put("Connection", "Upgrade");
      byte[] random = new byte[16];
      this.reuseableRandom.nextBytes(random);
      request.put("Sec-WebSocket-Key", Base64.encodeBytes(random));
      request.put("Sec-WebSocket-Version", "13");
      StringBuilder requestedExtensions = new StringBuilder();

      for(IExtension knownExtension : this.knownExtensions) {
         if (knownExtension.getProvidedExtensionAsClient() != null && knownExtension.getProvidedExtensionAsClient().length() != 0) {
            if (requestedExtensions.length() > 0) {
               requestedExtensions.append(", ");
            }

            requestedExtensions.append(knownExtension.getProvidedExtensionAsClient());
         }
      }

      if (requestedExtensions.length() != 0) {
         request.put("Sec-WebSocket-Extensions", requestedExtensions.toString());
      }

      StringBuilder requestedProtocols = new StringBuilder();

      for(IProtocol knownProtocol : this.knownProtocols) {
         if (knownProtocol.getProvidedProtocol().length() != 0) {
            if (requestedProtocols.length() > 0) {
               requestedProtocols.append(", ");
            }

            requestedProtocols.append(knownProtocol.getProvidedProtocol());
         }
      }

      if (requestedProtocols.length() != 0) {
         request.put("Sec-WebSocket-Protocol", requestedProtocols.toString());
      }

      return request;
   }

   public HandshakeBuilder postProcessHandshakeResponseAsServer(ClientHandshake request, ServerHandshakeBuilder response) throws InvalidHandshakeException {
      response.put("Upgrade", "websocket");
      response.put("Connection", request.getFieldValue("Connection"));
      String seckey = request.getFieldValue("Sec-WebSocket-Key");
      if (seckey != null && !"".equals(seckey)) {
         response.put("Sec-WebSocket-Accept", this.generateFinalKey(seckey));
         if (this.getExtension().getProvidedExtensionAsServer().length() != 0) {
            response.put("Sec-WebSocket-Extensions", this.getExtension().getProvidedExtensionAsServer());
         }

         if (this.getProtocol() != null && this.getProtocol().getProvidedProtocol().length() != 0) {
            response.put("Sec-WebSocket-Protocol", this.getProtocol().getProvidedProtocol());
         }

         response.setHttpStatusMessage("Web Socket Protocol Handshake");
         response.put("Server", "TooTallNate Java-WebSocket");
         response.put("Date", this.getServerTime());
         return response;
      } else {
         throw new InvalidHandshakeException("missing Sec-WebSocket-Key");
      }
   }

   public Draft copyInstance() {
      ArrayList<IExtension> newExtensions = new ArrayList();

      for(IExtension knownExtension : this.getKnownExtensions()) {
         newExtensions.add(knownExtension.copyInstance());
      }

      ArrayList<IProtocol> newProtocols = new ArrayList();

      for(IProtocol knownProtocol : this.getKnownProtocols()) {
         newProtocols.add(knownProtocol.copyInstance());
      }

      return new Draft_6455(newExtensions, newProtocols, this.maxFrameSize);
   }

   public ByteBuffer createBinaryFrame(Framedata framedata) {
      this.getExtension().encodeFrame(framedata);
      return this.createByteBufferFromFramedata(framedata);
   }

   private ByteBuffer createByteBufferFromFramedata(Framedata framedata) {
      ByteBuffer mes = framedata.getPayloadData();
      boolean mask = this.role == Role.CLIENT;
      int sizebytes = this.getSizeBytes(mes);
      ByteBuffer buf = ByteBuffer.allocate(1 + (sizebytes > 1 ? sizebytes + 1 : sizebytes) + (mask ? 4 : 0) + mes.remaining());
      byte optcode = this.fromOpcode(framedata.getOpcode());
      byte one = (byte)(framedata.isFin() ? -128 : 0);
      one = (byte)(one | optcode);
      if (framedata.isRSV1()) {
         one |= this.getRSVByte(1);
      }

      if (framedata.isRSV2()) {
         one |= this.getRSVByte(2);
      }

      if (framedata.isRSV3()) {
         one |= this.getRSVByte(3);
      }

      buf.put(one);
      byte[] payloadlengthbytes = this.toByteArray((long)mes.remaining(), sizebytes);

      assert payloadlengthbytes.length == sizebytes;

      if (sizebytes == 1) {
         buf.put((byte)(payloadlengthbytes[0] | this.getMaskByte(mask)));
      } else if (sizebytes == 2) {
         buf.put((byte)(126 | this.getMaskByte(mask)));
         buf.put(payloadlengthbytes);
      } else {
         if (sizebytes != 8) {
            throw new IllegalStateException("Size representation not supported/specified");
         }

         buf.put((byte)(127 | this.getMaskByte(mask)));
         buf.put(payloadlengthbytes);
      }

      if (mask) {
         ByteBuffer maskkey = ByteBuffer.allocate(4);
         maskkey.putInt(this.reuseableRandom.nextInt());
         buf.put(maskkey.array());

         for(int i = 0; mes.hasRemaining(); ++i) {
            buf.put((byte)(mes.get() ^ maskkey.get(i % 4)));
         }
      } else {
         buf.put(mes);
         mes.flip();
      }

      assert buf.remaining() == 0 : buf.remaining();

      buf.flip();
      return buf;
   }

   private Framedata translateSingleFrame(ByteBuffer buffer) throws IncompleteException, InvalidDataException {
      if (buffer == null) {
         throw new IllegalArgumentException();
      } else {
         int maxpacketsize = buffer.remaining();
         int realpacketsize = 2;
         this.translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize);
         byte b1 = buffer.get();
         boolean fin = b1 >> 8 != 0;
         boolean rsv1 = (b1 & 64) != 0;
         boolean rsv2 = (b1 & 32) != 0;
         boolean rsv3 = (b1 & 16) != 0;
         byte b2 = buffer.get();
         boolean mask = (b2 & -128) != 0;
         int payloadlength = (byte)(b2 & 127);
         Opcode optcode = this.toOpcode((byte)(b1 & 15));
         if (payloadlength < 0 || payloadlength > 125) {
            Draft_6455.TranslatedPayloadMetaData payloadData = this.translateSingleFramePayloadLength(buffer, optcode, payloadlength, maxpacketsize, realpacketsize);
            payloadlength = payloadData.getPayloadLength();
            realpacketsize = payloadData.getRealPackageSize();
         }

         this.translateSingleFrameCheckLengthLimit((long)payloadlength);
         realpacketsize = realpacketsize + (mask ? 4 : 0);
         realpacketsize = realpacketsize + payloadlength;
         this.translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize);
         ByteBuffer payload = ByteBuffer.allocate(this.checkAlloc(payloadlength));
         if (mask) {
            byte[] maskskey = new byte[4];
            buffer.get(maskskey);

            for(int i = 0; i < payloadlength; ++i) {
               payload.put((byte)(buffer.get() ^ maskskey[i % 4]));
            }
         } else {
            payload.put(buffer.array(), buffer.position(), payload.limit());
            buffer.position(buffer.position() + payload.limit());
         }

         FramedataImpl1 frame = FramedataImpl1.get(optcode);
         frame.setFin(fin);
         frame.setRSV1(rsv1);
         frame.setRSV2(rsv2);
         frame.setRSV3(rsv3);
         payload.flip();
         frame.setPayload(payload);
         this.getExtension().isFrameValid(frame);
         this.getExtension().decodeFrame(frame);
         frame.isValid();
         return frame;
      }
   }

   private Draft_6455.TranslatedPayloadMetaData translateSingleFramePayloadLength(ByteBuffer buffer, Opcode optcode, int oldPayloadlength, int maxpacketsize, int oldRealpacketsize) throws InvalidFrameException, IncompleteException, LimitExceededException {
      if (optcode != Opcode.PING && optcode != Opcode.PONG && optcode != Opcode.CLOSING) {
         int payloadlength;
         int realpacketsize;
         if (oldPayloadlength == 126) {
            realpacketsize = oldRealpacketsize + 2;
            this.translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize);
            byte[] sizebytes = new byte[]{0, buffer.get(), buffer.get()};
            payloadlength = (new BigInteger(sizebytes)).intValue();
         } else {
            realpacketsize = oldRealpacketsize + 8;
            this.translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize);
            byte[] bytes = new byte[8];

            for(int i = 0; i < 8; ++i) {
               bytes[i] = buffer.get();
            }

            long length = (new BigInteger(bytes)).longValue();
            this.translateSingleFrameCheckLengthLimit(length);
            payloadlength = (int)length;
         }

         return new Draft_6455.TranslatedPayloadMetaData(payloadlength, realpacketsize);
      } else {
         throw new InvalidFrameException("more than 125 octets");
      }
   }

   private void translateSingleFrameCheckLengthLimit(long length) throws LimitExceededException {
      if (length > 2147483647L) {
         throw new LimitExceededException("Payloadsize is to big...");
      } else if (length > (long)this.maxFrameSize) {
         throw new LimitExceededException("Payload limit reached.", this.maxFrameSize);
      } else if (length < 0L) {
         throw new LimitExceededException("Payloadsize is to little...");
      }
   }

   private void translateSingleFrameCheckPacketSize(int maxpacketsize, int realpacketsize) throws IncompleteException {
      if (maxpacketsize < realpacketsize) {
         throw new IncompleteException(realpacketsize);
      }
   }

   private byte getRSVByte(int rsv) {
      switch(rsv) {
      case 1:
         return 64;
      case 2:
         return 32;
      case 3:
         return 16;
      default:
         return 0;
      }
   }

   private byte getMaskByte(boolean mask) {
      return (byte)(mask ? -128 : 0);
   }

   private int getSizeBytes(ByteBuffer mes) {
      if (mes.remaining() <= 125) {
         return 1;
      } else {
         return mes.remaining() <= 65535 ? 2 : 8;
      }
   }

   public List translateFrame(ByteBuffer buffer) throws InvalidDataException {
      while(true) {
         List frames = new LinkedList();
         if (this.incompleteframe != null) {
            try {
               buffer.mark();
               int availableNextByteCount = buffer.remaining();
               int expectedNextByteCount = this.incompleteframe.remaining();
               if (expectedNextByteCount > availableNextByteCount) {
                  this.incompleteframe.put(buffer.array(), buffer.position(), availableNextByteCount);
                  buffer.position(buffer.position() + availableNextByteCount);
                  return Collections.emptyList();
               }

               this.incompleteframe.put(buffer.array(), buffer.position(), expectedNextByteCount);
               buffer.position(buffer.position() + expectedNextByteCount);
               Framedata cur = this.translateSingleFrame((ByteBuffer)this.incompleteframe.duplicate().position(0));
               frames.add(cur);
               this.incompleteframe = null;
            } catch (IncompleteException var6) {
               ByteBuffer extendedframe = ByteBuffer.allocate(this.checkAlloc(var6.getPreferredSize()));

               assert extendedframe.limit() > this.incompleteframe.limit();

               this.incompleteframe.rewind();
               extendedframe.put(this.incompleteframe);
               this.incompleteframe = extendedframe;
               continue;
            }
         }

         while(buffer.hasRemaining()) {
            buffer.mark();

            try {
               Framedata cur = this.translateSingleFrame(buffer);
               frames.add(cur);
            } catch (IncompleteException var7) {
               buffer.reset();
               int pref = var7.getPreferredSize();
               this.incompleteframe = ByteBuffer.allocate(this.checkAlloc(pref));
               this.incompleteframe.put(buffer);
               break;
            }
         }

         return frames;
      }
   }

   public List createFrames(ByteBuffer binary, boolean mask) {
      BinaryFrame curframe = new BinaryFrame();
      curframe.setPayload(binary);
      curframe.setTransferemasked(mask);

      try {
         curframe.isValid();
      } catch (InvalidDataException var5) {
         throw new NotSendableException(var5);
      }

      return Collections.singletonList(curframe);
   }

   public List createFrames(String text, boolean mask) {
      TextFrame curframe = new TextFrame();
      curframe.setPayload(ByteBuffer.wrap(Charsetfunctions.utf8Bytes(text)));
      curframe.setTransferemasked(mask);

      try {
         curframe.isValid();
      } catch (InvalidDataException var5) {
         throw new NotSendableException(var5);
      }

      return Collections.singletonList(curframe);
   }

   public void reset() {
      this.incompleteframe = null;
      if (this.extension != null) {
         this.extension.reset();
      }

      this.extension = new DefaultExtension();
      this.protocol = null;
   }

   private String getServerTime() {
      Calendar calendar = Calendar.getInstance();
      SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      return dateFormat.format(calendar.getTime());
   }

   private String generateFinalKey(String in) {
      String seckey = in.trim();
      String acc = seckey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

      MessageDigest sh1;
      try {
         sh1 = MessageDigest.getInstance("SHA1");
      } catch (NoSuchAlgorithmException var6) {
         throw new IllegalStateException(var6);
      }

      return Base64.encodeBytes(sh1.digest(acc.getBytes()));
   }

   private byte[] toByteArray(long val, int bytecount) {
      byte[] buffer = new byte[bytecount];
      int highest = 8 * bytecount - 8;

      for(int i = 0; i < bytecount; ++i) {
         buffer[i] = (byte)((int)(val >>> highest - 8 * i));
      }

      return buffer;
   }

   private byte fromOpcode(Opcode opcode) {
      if (opcode == Opcode.CONTINUOUS) {
         return 0;
      } else if (opcode == Opcode.TEXT) {
         return 1;
      } else if (opcode == Opcode.BINARY) {
         return 2;
      } else if (opcode == Opcode.CLOSING) {
         return 8;
      } else if (opcode == Opcode.PING) {
         return 9;
      } else if (opcode == Opcode.PONG) {
         return 10;
      } else {
         throw new IllegalArgumentException("Don't know how to handle " + opcode.toString());
      }
   }

   private Opcode toOpcode(byte opcode) throws InvalidFrameException {
      switch(opcode) {
      case 0:
         return Opcode.CONTINUOUS;
      case 1:
         return Opcode.TEXT;
      case 2:
         return Opcode.BINARY;
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
      default:
         throw new InvalidFrameException("Unknown opcode " + (short)opcode);
      case 8:
         return Opcode.CLOSING;
      case 9:
         return Opcode.PING;
      case 10:
         return Opcode.PONG;
      }
   }

   public void processFrame(WebSocketImpl webSocketImpl, Framedata frame) throws InvalidDataException {
      Opcode curop = frame.getOpcode();
      if (curop == Opcode.CLOSING) {
         this.processFrameClosing(webSocketImpl, frame);
      } else if (curop == Opcode.PING) {
         webSocketImpl.getWebSocketListener().onWebsocketPing(webSocketImpl, frame);
      } else if (curop == Opcode.PONG) {
         webSocketImpl.updateLastPong();
         webSocketImpl.getWebSocketListener().onWebsocketPong(webSocketImpl, frame);
      } else if (frame.isFin() && curop != Opcode.CONTINUOUS) {
         if (this.currentContinuousFrame != null) {
            throw new InvalidDataException(1002, "Continuous frame sequence not completed.");
         }

         if (curop == Opcode.TEXT) {
            this.processFrameText(webSocketImpl, frame);
         } else {
            if (curop != Opcode.BINARY) {
               throw new InvalidDataException(1002, "non control or continious frame expected");
            }

            this.processFrameBinary(webSocketImpl, frame);
         }
      } else {
         this.processFrameContinuousAndNonFin(webSocketImpl, frame, curop);
      }

   }

   private void processFrameContinuousAndNonFin(WebSocketImpl webSocketImpl, Framedata frame, Opcode curop) throws InvalidDataException {
      if (curop != Opcode.CONTINUOUS) {
         this.processFrameIsNotFin(frame);
      } else if (frame.isFin()) {
         this.processFrameIsFin(webSocketImpl, frame);
      } else if (this.currentContinuousFrame == null) {
         throw new InvalidDataException(1002, "Continuous frame sequence was not started.");
      }

      if (curop == Opcode.TEXT && !Charsetfunctions.isValidUTF8(frame.getPayloadData())) {
         throw new InvalidDataException(1007);
      } else {
         if (curop == Opcode.CONTINUOUS && this.currentContinuousFrame != null) {
            this.addToBufferList(frame.getPayloadData());
         }

      }
   }

   private void processFrameBinary(WebSocketImpl webSocketImpl, Framedata frame) {
      try {
         webSocketImpl.getWebSocketListener().onWebsocketMessage(webSocketImpl, frame.getPayloadData());
      } catch (RuntimeException var4) {
         this.logRuntimeException(webSocketImpl, var4);
      }

   }

   private void logRuntimeException(WebSocketImpl webSocketImpl, RuntimeException e) {
      webSocketImpl.getWebSocketListener().onWebsocketError(webSocketImpl, e);
   }

   private void processFrameText(WebSocketImpl webSocketImpl, Framedata frame) throws InvalidDataException {
      try {
         webSocketImpl.getWebSocketListener().onWebsocketMessage(webSocketImpl, Charsetfunctions.stringUtf8(frame.getPayloadData()));
      } catch (RuntimeException var4) {
         this.logRuntimeException(webSocketImpl, var4);
      }

   }

   private void processFrameIsFin(WebSocketImpl webSocketImpl, Framedata frame) throws InvalidDataException {
      if (this.currentContinuousFrame == null) {
         throw new InvalidDataException(1002, "Continuous frame sequence was not started.");
      } else {
         this.addToBufferList(frame.getPayloadData());
         this.checkBufferLimit();
         if (this.currentContinuousFrame.getOpcode() == Opcode.TEXT) {
            ((FramedataImpl1)this.currentContinuousFrame).setPayload(this.getPayloadFromByteBufferList());
            ((FramedataImpl1)this.currentContinuousFrame).isValid();

            try {
               webSocketImpl.getWebSocketListener().onWebsocketMessage(webSocketImpl, Charsetfunctions.stringUtf8(this.currentContinuousFrame.getPayloadData()));
            } catch (RuntimeException var5) {
               this.logRuntimeException(webSocketImpl, var5);
            }
         } else if (this.currentContinuousFrame.getOpcode() == Opcode.BINARY) {
            ((FramedataImpl1)this.currentContinuousFrame).setPayload(this.getPayloadFromByteBufferList());
            ((FramedataImpl1)this.currentContinuousFrame).isValid();

            try {
               webSocketImpl.getWebSocketListener().onWebsocketMessage(webSocketImpl, this.currentContinuousFrame.getPayloadData());
            } catch (RuntimeException var4) {
               this.logRuntimeException(webSocketImpl, var4);
            }
         }

         this.currentContinuousFrame = null;
         this.clearBufferList();
      }
   }

   private void processFrameIsNotFin(Framedata frame) throws InvalidDataException {
      if (this.currentContinuousFrame != null) {
         throw new InvalidDataException(1002, "Previous continuous frame sequence not completed.");
      } else {
         this.currentContinuousFrame = frame;
         this.addToBufferList(frame.getPayloadData());
         this.checkBufferLimit();
      }
   }

   private void processFrameClosing(WebSocketImpl webSocketImpl, Framedata frame) {
      int code = 1005;
      String reason = "";
      if (frame instanceof CloseFrame) {
         CloseFrame cf = (CloseFrame)frame;
         code = cf.getCloseCode();
         reason = cf.getMessage();
      }

      if (webSocketImpl.getReadyState() == ReadyState.CLOSING) {
         webSocketImpl.closeConnection(code, reason, true);
      } else if (this.getCloseHandshakeType() == CloseHandshakeType.TWOWAY) {
         webSocketImpl.close(code, reason, true);
      } else {
         webSocketImpl.flushAndClose(code, reason, false);
      }

   }

   private void clearBufferList() {
      synchronized(this.byteBufferList) {
         this.byteBufferList.clear();
      }
   }

   private void addToBufferList(ByteBuffer payloadData) {
      synchronized(this.byteBufferList) {
         this.byteBufferList.add(payloadData);
      }
   }

   private void checkBufferLimit() throws LimitExceededException {
      long totalSize = this.getByteBufferListSize();
      if (totalSize > (long)this.maxFrameSize) {
         this.clearBufferList();
         throw new LimitExceededException(this.maxFrameSize);
      }
   }

   public CloseHandshakeType getCloseHandshakeType() {
      return CloseHandshakeType.TWOWAY;
   }

   public String toString() {
      String result = super.toString();
      if (this.getExtension() != null) {
         result = result + " extension: " + this.getExtension().toString();
      }

      if (this.getProtocol() != null) {
         result = result + " protocol: " + this.getProtocol().toString();
      }

      result = result + " max frame size: " + this.maxFrameSize;
      return result;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Draft_6455 that = (Draft_6455)o;
         if (this.maxFrameSize != that.getMaxFrameSize()) {
            return false;
         } else {
            if (this.extension != null) {
               if (!this.extension.equals(that.getExtension())) {
                  return false;
               }
            } else if (that.getExtension() != null) {
               return false;
            }

            return this.protocol != null ? this.protocol.equals(that.getProtocol()) : that.getProtocol() == null;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.extension != null ? this.extension.hashCode() : 0;
      result = 31 * result + (this.protocol != null ? this.protocol.hashCode() : 0);
      result = 31 * result + (this.maxFrameSize ^ this.maxFrameSize >>> 32);
      return result;
   }

   private ByteBuffer getPayloadFromByteBufferList() throws LimitExceededException {
      long totalSize = 0L;
      ByteBuffer resultingByteBuffer;
      synchronized(this.byteBufferList) {
         for(ByteBuffer buffer : this.byteBufferList) {
            totalSize += (long)buffer.limit();
         }

         this.checkBufferLimit();
         resultingByteBuffer = ByteBuffer.allocate((int)totalSize);

         for(ByteBuffer buffer : this.byteBufferList) {
            resultingByteBuffer.put(buffer);
         }
      }

      resultingByteBuffer.flip();
      return resultingByteBuffer;
   }

   private long getByteBufferListSize() {
      long totalSize = 0L;
      synchronized(this.byteBufferList) {
         for(ByteBuffer buffer : this.byteBufferList) {
            totalSize += (long)buffer.limit();
         }

         return totalSize;
      }
   }

   private class TranslatedPayloadMetaData {
      private int payloadLength;
      private int realPackageSize;

      private int getPayloadLength() {
         return this.payloadLength;
      }

      private int getRealPackageSize() {
         return this.realPackageSize;
      }

      TranslatedPayloadMetaData(int newPayloadLength, int newRealPackageSize) {
         super();
         this.payloadLength = newPayloadLength;
         this.realPackageSize = newRealPackageSize;
      }
   }
}
