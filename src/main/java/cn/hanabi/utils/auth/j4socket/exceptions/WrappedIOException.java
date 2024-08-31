package cn.hanabi.utils.auth.j4socket.exceptions;

import cn.hanabi.utils.auth.j4socket.WebSocket;
import java.io.IOException;

public class WrappedIOException extends Exception {
   private final transient WebSocket connection;
   private final IOException ioException;

   public WrappedIOException(WebSocket connection, IOException ioException) {
      super();
      this.connection = connection;
      this.ioException = ioException;
   }

   public WebSocket getConnection() {
      return this.connection;
   }

   public IOException getIOException() {
      return this.ioException;
   }
}
