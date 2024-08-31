package cn.hanabi.utils.auth.j4socket.handshake;

public interface ServerHandshakeBuilder extends HandshakeBuilder, ServerHandshake {
   void setHttpStatus(short var1);

   void setHttpStatusMessage(String var1);
}
