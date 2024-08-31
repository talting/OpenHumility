package cn.hanabi.utils.auth.j4socket;

import cn.hanabi.utils.auth.j4socket.drafts.Draft;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidDataException;
import cn.hanabi.utils.auth.j4socket.framing.Framedata;
import cn.hanabi.utils.auth.j4socket.framing.PingFrame;
import cn.hanabi.utils.auth.j4socket.handshake.ClientHandshake;
import cn.hanabi.utils.auth.j4socket.handshake.Handshakedata;
import cn.hanabi.utils.auth.j4socket.handshake.ServerHandshake;
import cn.hanabi.utils.auth.j4socket.handshake.ServerHandshakeBuilder;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface WebSocketListener {
   ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket var1, Draft var2, ClientHandshake var3) throws InvalidDataException;

   void onWebsocketHandshakeReceivedAsClient(WebSocket var1, ClientHandshake var2, ServerHandshake var3) throws InvalidDataException;

   void onWebsocketHandshakeSentAsClient(WebSocket var1, ClientHandshake var2) throws InvalidDataException;

   void onWebsocketMessage(WebSocket var1, String var2);

   void onWebsocketMessage(WebSocket var1, ByteBuffer var2);

   void onWebsocketOpen(WebSocket var1, Handshakedata var2);

   void onWebsocketClose(WebSocket var1, int var2, String var3, boolean var4);

   void onWebsocketClosing(WebSocket var1, int var2, String var3, boolean var4);

   void onWebsocketCloseInitiated(WebSocket var1, int var2, String var3);

   void onWebsocketError(WebSocket var1, Exception var2);

   void onWebsocketPing(WebSocket var1, Framedata var2);

   PingFrame onPreparePing(WebSocket var1);

   void onWebsocketPong(WebSocket var1, Framedata var2);

   void onWriteDemand(WebSocket var1);

   InetSocketAddress getLocalSocketAddress(WebSocket var1);

   InetSocketAddress getRemoteSocketAddress(WebSocket var1);
}
