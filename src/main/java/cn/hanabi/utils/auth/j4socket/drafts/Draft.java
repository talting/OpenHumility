package cn.hanabi.utils.auth.j4socket.drafts;

import cn.hanabi.utils.auth.j4socket.WebSocketImpl;
import cn.hanabi.utils.auth.j4socket.enums.CloseHandshakeType;
import cn.hanabi.utils.auth.j4socket.enums.HandshakeState;
import cn.hanabi.utils.auth.j4socket.enums.Opcode;
import cn.hanabi.utils.auth.j4socket.enums.Role;
import cn.hanabi.utils.auth.j4socket.exceptions.IncompleteHandshakeException;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidDataException;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidHandshakeException;
import cn.hanabi.utils.auth.j4socket.framing.BinaryFrame;
import cn.hanabi.utils.auth.j4socket.framing.ContinuousFrame;
import cn.hanabi.utils.auth.j4socket.framing.DataFrame;
import cn.hanabi.utils.auth.j4socket.framing.Framedata;
import cn.hanabi.utils.auth.j4socket.framing.TextFrame;
import cn.hanabi.utils.auth.j4socket.handshake.ClientHandshake;
import cn.hanabi.utils.auth.j4socket.handshake.ClientHandshakeBuilder;
import cn.hanabi.utils.auth.j4socket.handshake.HandshakeBuilder;
import cn.hanabi.utils.auth.j4socket.handshake.HandshakeImpl1Client;
import cn.hanabi.utils.auth.j4socket.handshake.HandshakeImpl1Server;
import cn.hanabi.utils.auth.j4socket.handshake.Handshakedata;
import cn.hanabi.utils.auth.j4socket.handshake.ServerHandshake;
import cn.hanabi.utils.auth.j4socket.handshake.ServerHandshakeBuilder;
import cn.hanabi.utils.auth.j4socket.util.Charsetfunctions;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public abstract class Draft {
   protected Role role = null;
   protected Opcode continuousFrameType = null;

   public Draft() {
      super();
   }

   public static ByteBuffer readLine(ByteBuffer buf) {
      ByteBuffer sbuf = ByteBuffer.allocate(buf.remaining());
      byte cur = 48;

      while(buf.hasRemaining()) {
         byte prev = cur;
         cur = buf.get();
         sbuf.put(cur);
         if (prev == 13 && cur == 10) {
            sbuf.limit(sbuf.position() - 2);
            sbuf.position(0);
            return sbuf;
         }
      }

      buf.position(buf.position() - sbuf.position());
      return null;
   }

   public static String readStringLine(ByteBuffer buf) {
      ByteBuffer b = readLine(buf);
      return b == null ? null : Charsetfunctions.stringAscii(b.array(), 0, b.limit());
   }

   public static HandshakeBuilder translateHandshakeHttp(ByteBuffer buf, Role role) throws InvalidHandshakeException {
      String line = readStringLine(buf);
      if (line == null) {
         throw new IncompleteHandshakeException(buf.capacity() + 128);
      } else {
         String[] firstLineTokens = line.split(" ", 3);
         if (firstLineTokens.length != 3) {
            throw new InvalidHandshakeException();
         } else {
            HandshakeBuilder handshake;
            if (role == Role.CLIENT) {
               handshake = translateHandshakeHttpClient(firstLineTokens, line);
            } else {
               handshake = translateHandshakeHttpServer(firstLineTokens, line);
            }

            for(line = readStringLine(buf); line != null && line.length() > 0; line = readStringLine(buf)) {
               String[] pair = line.split(":", 2);
               if (pair.length != 2) {
                  throw new InvalidHandshakeException("not an http header");
               }

               if (handshake.hasFieldValue(pair[0])) {
                  handshake.put(pair[0], handshake.getFieldValue(pair[0]) + "; " + pair[1].replaceFirst("^ +", ""));
               } else {
                  handshake.put(pair[0], pair[1].replaceFirst("^ +", ""));
               }
            }

            if (line == null) {
               throw new IncompleteHandshakeException();
            } else {
               return handshake;
            }
         }
      }
   }

   private static HandshakeBuilder translateHandshakeHttpServer(String[] firstLineTokens, String line) throws InvalidHandshakeException {
      if (!"GET".equalsIgnoreCase(firstLineTokens[0])) {
         throw new InvalidHandshakeException(String.format("Invalid request method received: %s Status line: %s", firstLineTokens[0], line));
      } else if (!"HTTP/1.1".equalsIgnoreCase(firstLineTokens[2])) {
         throw new InvalidHandshakeException(String.format("Invalid status line received: %s Status line: %s", firstLineTokens[2], line));
      } else {
         ClientHandshakeBuilder clienthandshake = new HandshakeImpl1Client();
         clienthandshake.setResourceDescriptor(firstLineTokens[1]);
         return clienthandshake;
      }
   }

   private static HandshakeBuilder translateHandshakeHttpClient(String[] firstLineTokens, String line) throws InvalidHandshakeException {
      if (!"101".equals(firstLineTokens[1])) {
         throw new InvalidHandshakeException(String.format("Invalid status code received: %s Status line: %s", firstLineTokens[1], line));
      } else if (!"HTTP/1.1".equalsIgnoreCase(firstLineTokens[0])) {
         throw new InvalidHandshakeException(String.format("Invalid status line received: %s Status line: %s", firstLineTokens[0], line));
      } else {
         HandshakeBuilder handshake = new HandshakeImpl1Server();
         ServerHandshakeBuilder serverhandshake = (ServerHandshakeBuilder)handshake;
         serverhandshake.setHttpStatus(Short.parseShort(firstLineTokens[1]));
         serverhandshake.setHttpStatusMessage(firstLineTokens[2]);
         return handshake;
      }
   }

   public abstract HandshakeState acceptHandshakeAsClient(ClientHandshake var1, ServerHandshake var2) throws InvalidHandshakeException;

   public abstract HandshakeState acceptHandshakeAsServer(ClientHandshake var1) throws InvalidHandshakeException;

   protected boolean basicAccept(Handshakedata handshakedata) {
      return handshakedata.getFieldValue("Upgrade").equalsIgnoreCase("websocket") && handshakedata.getFieldValue("Connection").toLowerCase(Locale.ENGLISH).contains("upgrade");
   }

   public abstract ByteBuffer createBinaryFrame(Framedata var1);

   public abstract List createFrames(ByteBuffer var1, boolean var2);

   public abstract List createFrames(String var1, boolean var2);

   public abstract void processFrame(WebSocketImpl var1, Framedata var2) throws InvalidDataException;

   public List continuousFrame(Opcode op, ByteBuffer buffer, boolean fin) {
      if (op != Opcode.BINARY && op != Opcode.TEXT) {
         throw new IllegalArgumentException("Only Opcode.BINARY or  Opcode.TEXT are allowed");
      } else {
         DataFrame bui = null;
         if (this.continuousFrameType != null) {
            bui = new ContinuousFrame();
         } else {
            this.continuousFrameType = op;
            if (op == Opcode.BINARY) {
               bui = new BinaryFrame();
            } else if (op == Opcode.TEXT) {
               bui = new TextFrame();
            }
         }

         bui.setPayload(buffer);
         bui.setFin(fin);

         try {
            bui.isValid();
         } catch (InvalidDataException var6) {
            throw new IllegalArgumentException(var6);
         }

         if (fin) {
            this.continuousFrameType = null;
         } else {
            this.continuousFrameType = op;
         }

         return Collections.singletonList(bui);
      }
   }

   public abstract void reset();

   /** @deprecated */
   @Deprecated
   public List createHandshake(Handshakedata handshakedata, Role ownrole) {
      return this.createHandshake(handshakedata);
   }

   public List createHandshake(Handshakedata handshakedata) {
      return this.createHandshake(handshakedata, true);
   }

   /** @deprecated */
   @Deprecated
   public List createHandshake(Handshakedata handshakedata, Role ownrole, boolean withcontent) {
      return this.createHandshake(handshakedata, withcontent);
   }

   public List createHandshake(Handshakedata handshakedata, boolean withcontent) {
      StringBuilder bui = new StringBuilder(100);
      if (handshakedata instanceof ClientHandshake) {
         bui.append("GET ").append(((ClientHandshake)handshakedata).getResourceDescriptor()).append(" HTTP/1.1");
      } else {
         if (!(handshakedata instanceof ServerHandshake)) {
            throw new IllegalArgumentException("unknown role");
         }

         bui.append("HTTP/1.1 101 ").append(((ServerHandshake)handshakedata).getHttpStatusMessage());
      }

      bui.append("\r\n");
      Iterator it = handshakedata.iterateHttpFields();

      while(it.hasNext()) {
         String fieldname = (String)it.next();
         String fieldvalue = handshakedata.getFieldValue(fieldname);
         bui.append(fieldname);
         bui.append(": ");
         bui.append(fieldvalue);
         bui.append("\r\n");
      }

      bui.append("\r\n");
      byte[] httpheader = Charsetfunctions.asciiBytes(bui.toString());
      byte[] content = withcontent ? handshakedata.getContent() : null;
      ByteBuffer bytebuffer = ByteBuffer.allocate((content == null ? 0 : content.length) + httpheader.length);
      bytebuffer.put(httpheader);
      if (content != null) {
         bytebuffer.put(content);
      }

      bytebuffer.flip();
      return Collections.singletonList(bytebuffer);
   }

   public abstract ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder var1) throws InvalidHandshakeException;

   public abstract HandshakeBuilder postProcessHandshakeResponseAsServer(ClientHandshake var1, ServerHandshakeBuilder var2) throws InvalidHandshakeException;

   public abstract List<Framedata> translateFrame(ByteBuffer var1) throws InvalidDataException;

   public abstract CloseHandshakeType getCloseHandshakeType();

   public abstract Draft copyInstance();

   public Handshakedata translateHandshake(ByteBuffer buf) throws InvalidHandshakeException {
      return translateHandshakeHttp(buf, this.role);
   }

   public int checkAlloc(int bytecount) throws InvalidDataException {
      if (bytecount < 0) {
         throw new InvalidDataException(1002, "Negative count");
      } else {
         return bytecount;
      }
   }

   int readVersion(Handshakedata handshakedata) {
      String vers = handshakedata.getFieldValue("Sec-WebSocket-Version");
      if (vers.length() > 0) {
         try {
            int v = (new Integer(vers.trim())).intValue();
            return v;
         } catch (NumberFormatException var5) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   public void setParseMode(Role role) {
      this.role = role;
   }

   public Role getRole() {
      return this.role;
   }

   public String toString() {
      return this.getClass().getSimpleName();
   }
}
