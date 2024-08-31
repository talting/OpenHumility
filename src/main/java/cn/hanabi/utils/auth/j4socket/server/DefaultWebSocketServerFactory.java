package cn.hanabi.utils.auth.j4socket.server;

import cn.hanabi.utils.auth.j4socket.WebSocketAdapter;
import cn.hanabi.utils.auth.j4socket.WebSocketImpl;
import cn.hanabi.utils.auth.j4socket.WebSocketServerFactory;
import cn.hanabi.utils.auth.j4socket.drafts.Draft;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

public class DefaultWebSocketServerFactory implements WebSocketServerFactory {
   public DefaultWebSocketServerFactory() {
      super();
   }

   public WebSocketImpl createWebSocket(WebSocketAdapter a, Draft d) {
      return new WebSocketImpl(a, d);
   }

   public WebSocketImpl createWebSocket(WebSocketAdapter a, List d) {
      return new WebSocketImpl(a, d);
   }

   public SocketChannel wrapChannel(SocketChannel channel, SelectionKey key) {
      return channel;
   }

   public void close() {
   }
}
