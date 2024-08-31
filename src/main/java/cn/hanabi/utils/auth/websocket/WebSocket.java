package cn.hanabi.utils.auth.websocket;

import cn.hanabi.utils.auth.Auth;
import cn.hanabi.utils.auth.j4socket.client.WebSocketClient;
import cn.hanabi.utils.auth.j4socket.handshake.ServerHandshake;
import java.net.URI;

public class WebSocket extends WebSocketClient {
   public String sendData;

   public WebSocket(URI serverUri) {
      super(serverUri);
   }

   public void onOpen(ServerHandshake serverHandshake) {
      this.send(this.sendData);
   }

   public void onMessage(String s) {
      Auth.responsePacket = s;
      Auth.verify = true;
   }

   public void onClose(int i, String s, boolean b) {
   }

   public void onError(Exception e) {
      e.printStackTrace();
   }

   public void setSendData(String s) {
      this.sendData = s;
   }
}
