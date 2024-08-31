package cn.hanabi.utils.auth.j4socket;

import cn.hanabi.utils.auth.j4socket.drafts.Draft;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidDataException;
import cn.hanabi.utils.auth.j4socket.framing.Framedata;
import cn.hanabi.utils.auth.j4socket.framing.PingFrame;
import cn.hanabi.utils.auth.j4socket.framing.PongFrame;
import cn.hanabi.utils.auth.j4socket.handshake.ClientHandshake;
import cn.hanabi.utils.auth.j4socket.handshake.HandshakeImpl1Server;
import cn.hanabi.utils.auth.j4socket.handshake.ServerHandshake;
import cn.hanabi.utils.auth.j4socket.handshake.ServerHandshakeBuilder;

public abstract class WebSocketAdapter implements WebSocketListener {
   private PingFrame pingFrame;

   public WebSocketAdapter() {
      super();
   }

   public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
      return new HandshakeImpl1Server();
   }

   public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response) throws InvalidDataException {
   }

   public void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) throws InvalidDataException {
   }

   public void onWebsocketPing(WebSocket conn, Framedata f) {
      conn.sendFrame(new PongFrame((PingFrame)f));
   }

   public void onWebsocketPong(WebSocket conn, Framedata f) {
   }

   public PingFrame onPreparePing(WebSocket conn) {
      if (this.pingFrame == null) {
         this.pingFrame = new PingFrame();
      }

      return this.pingFrame;
   }
}
