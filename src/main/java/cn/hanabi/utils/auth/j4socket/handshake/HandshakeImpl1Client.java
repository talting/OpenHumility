package cn.hanabi.utils.auth.j4socket.handshake;

public class HandshakeImpl1Client extends HandshakedataImpl1 implements ClientHandshakeBuilder {
   private String resourceDescriptor = "*";

   public HandshakeImpl1Client() {
      super();
   }

   public void setResourceDescriptor(String resourceDescriptor) {
      if (resourceDescriptor == null) {
         throw new IllegalArgumentException("http resource descriptor must not be null");
      } else {
         this.resourceDescriptor = resourceDescriptor;
      }
   }

   public String getResourceDescriptor() {
      return this.resourceDescriptor;
   }
}
