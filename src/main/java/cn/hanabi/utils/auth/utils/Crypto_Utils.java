package cn.hanabi.utils.auth.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Crypto_Utils {
   public Crypto_Utils() {
      super();
   }

   public static String HMACSHA256(String data, String key) throws Exception {
      Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
      SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
      sha256_HMAC.init(secret_key);
      byte[] array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
      StringBuilder sb = new StringBuilder();

      for(byte item : array) {
         sb.append(Integer.toHexString(item & 255 | 256).substring(1, 3));
      }

      return sb.toString();
   }
}
