package cn.hanabi.utils.auth.j4socket;

import cn.hanabi.utils.auth.j4socket.drafts.Draft;
import java.util.List;

public interface WebSocketFactory {
   WebSocket createWebSocket(WebSocketAdapter var1, Draft var2);

   WebSocket createWebSocket(WebSocketAdapter var1, List var2);
}
