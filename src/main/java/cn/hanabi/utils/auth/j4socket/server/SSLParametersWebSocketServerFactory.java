package cn.hanabi.utils.auth.j4socket.server;

import cn.hanabi.utils.auth.j4socket.SSLSocketChannel2;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

public class SSLParametersWebSocketServerFactory extends DefaultSSLWebSocketServerFactory {
   private final SSLParameters sslParameters;

   public SSLParametersWebSocketServerFactory(SSLContext sslContext, SSLParameters sslParameters) {
      this(sslContext, Executors.newSingleThreadScheduledExecutor(), sslParameters);
   }

   public SSLParametersWebSocketServerFactory(SSLContext sslContext, ExecutorService executerService, SSLParameters sslParameters) {
      super(sslContext, executerService);
      if (sslParameters == null) {
         throw new IllegalArgumentException();
      } else {
         this.sslParameters = sslParameters;
      }
   }

   public ByteChannel wrapChannel(SocketChannel channel, SelectionKey key) throws IOException {
      SSLEngine e = this.sslcontext.createSSLEngine();
      e.setUseClientMode(false);
      e.setSSLParameters(this.sslParameters);
      return new SSLSocketChannel2(channel, e, this.exec, key);
   }
}
