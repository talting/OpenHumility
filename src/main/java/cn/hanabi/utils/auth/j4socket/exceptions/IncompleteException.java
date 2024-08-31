package cn.hanabi.utils.auth.j4socket.exceptions;

public class IncompleteException extends Exception {
   private static final long serialVersionUID = 7330519489840500997L;
   private final int preferredSize;

   public IncompleteException(int preferredSize) {
      super();
      this.preferredSize = preferredSize;
   }

   public int getPreferredSize() {
      return this.preferredSize;
   }
}
