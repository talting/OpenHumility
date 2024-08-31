package cn.hanabi.utils.auth.utils;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class AES {
   private static final String ALGORITHM = "AES";
   private static final String ALGORITHM_STR = "AES/CBC/PKCS5Padding";
   private final SecretKeySpec key;
   private final IvParameterSpec ivParameterSpec;
   public String outKey;

   public AES(int hexKey, String iv) {
      super();
      this.outKey = getRandomString(hexKey);
      this.key = new SecretKeySpec(this.outKey.getBytes(), "AES");
      this.ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
   }

   public String encryptData(String data) throws Exception {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(1, this.key, this.ivParameterSpec);
      return this.BytetoBase64(cipher.doFinal(data.getBytes()));
   }

   public String decryptData(String base64Data) throws Exception {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(2, this.key, this.ivParameterSpec);
      return new String(cipher.doFinal(this.Base64toByte(base64Data)));
   }

   public byte[] Base64toByte(String a) {
      Base64 base64 = new Base64();
      return base64.decode(a);
   }

   public String BytetoBase64(byte[] a) {
      Base64 base64 = new Base64();
      return base64.encodeToString(a);
   }

   public static String getRandomString(int length) {
      String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
      Random random = new Random();
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < length; ++i) {
         int number = random.nextInt(62);
         sb.append(str.charAt(number));
      }

      return sb.toString();
   }
}
