package cn.hanabi.utils.auth.utils;

import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DESEncrypt {
   private static final String IV_PARAMETER = "12345678";
   private static final String ALGORITHM = "DES";
   private static final String CIPHER_ALGORITHM = "DES/CBC/PKCS5Padding";
   private static final String CHARSET = "utf-8";

   public DESEncrypt() {
      super();
   }

   private static Key generateKey(String password) throws Exception {
      DESKeySpec dks = new DESKeySpec(password.getBytes("utf-8"));
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      return keyFactory.generateSecret(dks);
   }

   public String encrypt(String password, String data) {
      if (password != null && password.length() >= 8) {
         if (data == null) {
            return null;
         } else {
            try {
               Key secretKey = generateKey(password);
               Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
               IvParameterSpec iv = new IvParameterSpec("12345678".getBytes("utf-8"));
               cipher.init(1, secretKey, iv);
               byte[] bytes = cipher.doFinal(data.getBytes("utf-8"));
               return new String(Base64.getEncoder().encode(bytes));
            } catch (Exception var7) {
               var7.printStackTrace();
               return data;
            }
         }
      } else {
         throw new RuntimeException("unknown error");
      }
   }
}
