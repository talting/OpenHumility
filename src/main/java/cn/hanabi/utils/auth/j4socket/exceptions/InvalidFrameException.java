package cn.hanabi.utils.auth.j4socket.exceptions;

public class InvalidFrameException extends InvalidDataException {
   private static final long serialVersionUID = -9016496369828887591L;

   public InvalidFrameException() {
      super(1002);
   }

   public InvalidFrameException(String s) {
      super(1002, s);
   }

   public InvalidFrameException(Throwable t) {
      super(1002, t);
   }

   public InvalidFrameException(String s, Throwable t) {
      super(1002, s, t);
   }
}
