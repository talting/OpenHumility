package cn.hanabi.utils.auth.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES_Utils {
   private static final String AES = "AES";
   private static final String UTF8 = "UTF-8";
   private static final String IV_STRING = "Linsk110011ksniL";

   public AES_Utils() {
      super();
   }

   public static String generateSecreKey() {
      String uuid = UUID.randomUUID().toString();
      uuid = uuid.replaceAll("-", "");
      return uuid.substring(0, 16);
   }

   public static String aesEncry(String content, String key) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
      byte[] contentByte = content.getBytes("UTF-8");
      byte[] keyByte = key.getBytes();
      SecretKeySpec keySpec = new SecretKeySpec(keyByte, "AES");
      byte[] initParam = "Linsk110011ksniL".getBytes();
      IvParameterSpec ivSpec = new IvParameterSpec(initParam);
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(1, keySpec, ivSpec);
      byte[] encryptedBytes = cipher.doFinal(contentByte);
      String encodedString = Base64.getEncoder().encodeToString(encryptedBytes);
      return encodedString;
   }

   public static String aesDecry(String content, String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
      byte[] contentByte = Base64.getDecoder().decode(content);
      byte[] keyByte = key.getBytes();
      SecretKeySpec keySpec = new SecretKeySpec(keyByte, "AES");
      byte[] initParam = "Linsk110011ksniL".getBytes();
      IvParameterSpec ivSpec = new IvParameterSpec(initParam);
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(2, keySpec, ivSpec);
      byte[] result = cipher.doFinal(contentByte);
      return new String(result, "UTF-8");
   }
}
