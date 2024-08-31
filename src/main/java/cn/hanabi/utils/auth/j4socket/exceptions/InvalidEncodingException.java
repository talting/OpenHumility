package cn.hanabi.utils.auth.j4socket.exceptions;

import java.io.UnsupportedEncodingException;

public class InvalidEncodingException extends RuntimeException {
   private final UnsupportedEncodingException encodingException;

   public InvalidEncodingException(UnsupportedEncodingException encodingException) {
      super();
      if (encodingException == null) {
         throw new IllegalArgumentException();
      } else {
         this.encodingException = encodingException;
      }
   }

   public UnsupportedEncodingException getEncodingException() {
      return this.encodingException;
   }
}
