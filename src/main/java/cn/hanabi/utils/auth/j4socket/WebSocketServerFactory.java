package cn.hanabi.utils.auth.j4socket;

import cn.hanabi.utils.auth.j4socket.drafts.Draft;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

public interface WebSocketServerFactory extends WebSocketFactory {
   WebSocketImpl createWebSocket(WebSocketAdapter var1, Draft var2);

   WebSocketImpl createWebSocket(WebSocketAdapter var1, List var2);

   ByteChannel wrapChannel(SocketChannel var1, SelectionKey var2) throws IOException;

   void close();
}
