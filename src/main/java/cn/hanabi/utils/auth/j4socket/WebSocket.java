package cn.hanabi.utils.auth.j4socket;

import cn.hanabi.utils.auth.j4socket.drafts.Draft;
import cn.hanabi.utils.auth.j4socket.enums.Opcode;
import cn.hanabi.utils.auth.j4socket.enums.ReadyState;
import cn.hanabi.utils.auth.j4socket.framing.Framedata;
import cn.hanabi.utils.auth.j4socket.protocols.IProtocol;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import javax.net.ssl.SSLSession;

public interface WebSocket {
   void close(int var1, String var2);

   void close(int var1);

   void close();

   void closeConnection(int var1, String var2);

   void send(String var1);

   void send(ByteBuffer var1);

   void send(byte[] var1);

   void sendFrame(Framedata var1);

   void sendFrame(Collection var1);

   void sendPing();

   void sendFragmentedFrame(Opcode var1, ByteBuffer var2, boolean var3);

   boolean hasBufferedData();

   InetSocketAddress getRemoteSocketAddress();

   InetSocketAddress getLocalSocketAddress();

   boolean isOpen();

   boolean isClosing();

   boolean isFlushAndClose();

   boolean isClosed();

   Draft getDraft();

   ReadyState getReadyState();

   String getResourceDescriptor();

   void setAttachment(Object var1);

   Object getAttachment();

   boolean hasSSLSupport();

   SSLSession getSSLSession() throws IllegalArgumentException;

   IProtocol getProtocol();
}
