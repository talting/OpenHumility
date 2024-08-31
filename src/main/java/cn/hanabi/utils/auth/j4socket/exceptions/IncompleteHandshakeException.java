package cn.hanabi.utils.auth.j4socket.exceptions;

public class IncompleteHandshakeException extends RuntimeException {
   private static final long serialVersionUID = 7906596804233893092L;
   private final int preferredSize;

   public IncompleteHandshakeException(int preferredSize) {
      super();
      this.preferredSize = preferredSize;
   }

   public IncompleteHandshakeException() {
      super();
      this.preferredSize = 0;
   }

   public int getPreferredSize() {
      return this.preferredSize;
   }
}
