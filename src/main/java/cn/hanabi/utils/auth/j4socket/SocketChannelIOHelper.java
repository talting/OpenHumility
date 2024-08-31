package cn.hanabi.utils.auth.j4socket;

import cn.hanabi.utils.auth.j4socket.enums.Role;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class SocketChannelIOHelper {
   private SocketChannelIOHelper() {
      super();
      throw new IllegalStateException("Utility class");
   }

   public static boolean read(ByteBuffer buf, WebSocketImpl ws, ByteChannel channel) throws IOException {
      buf.clear();
      int read = channel.read(buf);
      buf.flip();
      if (read == -1) {
         ws.eot();
         return false;
      } else {
         return read != 0;
      }
   }

   public static boolean readMore(ByteBuffer buf, WebSocketImpl ws, WrappedByteChannel channel) throws IOException {
      buf.clear();
      int read = channel.readMore(buf);
      buf.flip();
      if (read == -1) {
         ws.eot();
         return false;
      } else {
         return channel.isNeedRead();
      }
   }

   public static boolean batch(WebSocketImpl ws, ByteChannel sockchannel) throws IOException {
      if (ws == null) {
         return false;
      } else {
         ByteBuffer buffer = (ByteBuffer)ws.outQueue.peek();
         WrappedByteChannel c = null;
         if (buffer == null) {
            if (sockchannel instanceof WrappedByteChannel) {
               c = (WrappedByteChannel)sockchannel;
               if (c.isNeedWrite()) {
                  c.writeMore();
               }
            }
         } else {
            while(true) {
               sockchannel.write(buffer);
               if (buffer.remaining() > 0) {
                  return false;
               }

               ws.outQueue.poll();
               buffer = (ByteBuffer)ws.outQueue.peek();
               if (buffer == null) {
                  break;
               }
            }
         }

         if (ws.outQueue.isEmpty() && ws.isFlushAndClose() && ws.getDraft() != null && ws.getDraft().getRole() != null && ws.getDraft().getRole() == Role.SERVER) {
            ws.closeConnection();
         }

         return c == null || !((WrappedByteChannel)sockchannel).isNeedWrite();
      }
   }
}
