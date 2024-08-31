package cn.hanabi.utils.auth.j4socket.protocols;

import java.util.regex.Pattern;

public class Protocol implements IProtocol {
   private static final Pattern patternSpace = Pattern.compile(" ");
   private static final Pattern patternComma = Pattern.compile(",");
   private final String providedProtocol;

   public Protocol(String providedProtocol) {
      super();
      if (providedProtocol == null) {
         throw new IllegalArgumentException();
      } else {
         this.providedProtocol = providedProtocol;
      }
   }

   public boolean acceptProvidedProtocol(String inputProtocolHeader) {
      if ("".equals(this.providedProtocol)) {
         return true;
      } else {
         String protocolHeader = patternSpace.matcher(inputProtocolHeader).replaceAll("");
         String[] headers = patternComma.split(protocolHeader);

         for(String header : headers) {
            if (this.providedProtocol.equals(header)) {
               return true;
            }
         }

         return false;
      }
   }

   public String getProvidedProtocol() {
      return this.providedProtocol;
   }

   public IProtocol copyInstance() {
      return new Protocol(this.getProvidedProtocol());
   }

   public String toString() {
      return this.getProvidedProtocol();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Protocol protocol = (Protocol)o;
         return this.providedProtocol.equals(protocol.providedProtocol);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.providedProtocol.hashCode();
   }
}
