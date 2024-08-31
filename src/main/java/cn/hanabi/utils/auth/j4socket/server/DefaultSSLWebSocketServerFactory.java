package cn.hanabi.utils.auth.j4socket.server;

import cn.hanabi.utils.auth.j4socket.SSLSocketChannel2;
import cn.hanabi.utils.auth.j4socket.WebSocketAdapter;
import cn.hanabi.utils.auth.j4socket.WebSocketImpl;
import cn.hanabi.utils.auth.j4socket.WebSocketServerFactory;
import cn.hanabi.utils.auth.j4socket.drafts.Draft;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class DefaultSSLWebSocketServerFactory implements WebSocketServerFactory {
   protected SSLContext sslcontext;
   protected ExecutorService exec;

   public DefaultSSLWebSocketServerFactory(SSLContext sslContext) {
      this(sslContext, Executors.newSingleThreadScheduledExecutor());
   }

   public DefaultSSLWebSocketServerFactory(SSLContext sslContext, ExecutorService exec) {
      super();
      if (sslContext != null && exec != null) {
         this.sslcontext = sslContext;
         this.exec = exec;
      } else {
         throw new IllegalArgumentException();
      }
   }

   public ByteChannel wrapChannel(SocketChannel channel, SelectionKey key) throws IOException {
      SSLEngine e = this.sslcontext.createSSLEngine();
      List ciphers = new ArrayList(Arrays.asList(e.getEnabledCipherSuites()));
      ciphers.remove("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
      e.setEnabledCipherSuites((String[])ciphers.toArray(new String[ciphers.size()]));
      e.setUseClientMode(false);
      return new SSLSocketChannel2(channel, e, this.exec, key);
   }

   public WebSocketImpl createWebSocket(WebSocketAdapter a, Draft d) {
      return new WebSocketImpl(a, d);
   }

   public WebSocketImpl createWebSocket(WebSocketAdapter a, List d) {
      return new WebSocketImpl(a, d);
   }

   public void close() {
      this.exec.shutdown();
   }
}
