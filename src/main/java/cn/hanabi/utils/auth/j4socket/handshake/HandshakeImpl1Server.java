package cn.hanabi.utils.auth.j4socket.handshake;

public class HandshakeImpl1Server extends HandshakedataImpl1 implements ServerHandshakeBuilder {
   private short httpstatus;
   private String httpstatusmessage;

   public HandshakeImpl1Server() {
      super();
   }

   public String getHttpStatusMessage() {
      return this.httpstatusmessage;
   }

   public short getHttpStatus() {
      return this.httpstatus;
   }

   public void setHttpStatusMessage(String message) {
      this.httpstatusmessage = message;
   }

   public void setHttpStatus(short status) {
      this.httpstatus = status;
   }
}
