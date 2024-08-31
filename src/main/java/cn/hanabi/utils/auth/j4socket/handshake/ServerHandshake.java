package cn.hanabi.utils.auth.j4socket.handshake;

public interface ServerHandshake extends Handshakedata {
   short getHttpStatus();

   String getHttpStatusMessage();
}
