package cn.hanabi.utils.auth;

import cn.hanabi.Client;
import cn.hanabi.utils.CrashUtils;
import cn.hanabi.utils.auth.client.AuthClient;
import cn.hanabi.utils.auth.utils.AES;
import cn.hanabi.utils.auth.utils.DESEncrypt;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import sun.misc.Unsafe;

public class Auth {
   public static String responsePacket;
   public static boolean verify = false;

   public Auth() {
      super();
   }

   public static boolean auth() {
      try {
         Socket s = new Socket("127.0.0.1", 19394);
         OutputStream os = s.getOutputStream();
         PrintWriter bw = new PrintWriter(os);
         String nowTime = (new SimpleDateFormat("HH-mm")).format(new Date());
         AES aes = new AES(16, "v0xqu%@$6sdr@%a$");
         DESEncrypt des = new DESEncrypt();
         String data = des.encrypt(nowTime + "cao", aes.outKey);
         bw.write(data);
         bw.flush();
         InputStream is = s.getInputStream();
         BufferedReader br = new BufferedReader(new InputStreamReader(is));
         String result = br.readLine();

         try {
            String launcherResult = aes.decryptData(result);
            if (!Objects.equals(launcherResult, "hello")) {
               return false;
            }

            data = aes.encryptData(getHWID());
         } catch (Exception var24) {
            try {
               Field field = Unsafe.class.getDeclaredField("theUnsafe");
               field.setAccessible(true);
               Unsafe unsafe = null;

               try {
                  unsafe = (Unsafe)field.get((Object)null);
               } catch (IllegalAccessException var20) {
                  var20.printStackTrace();
               }

               Class cacheClass = null;

               try {
                  cacheClass = Class.forName("java.lang.Integer$IntegerCache");
               } catch (ClassNotFoundException var19) {
                  var19.printStackTrace();
               }

               Field cache = cacheClass.getDeclaredField("cache");
               long offset = unsafe.staticFieldOffset(cache);
               unsafe.putObject(Integer.getInteger("SkidSense.pub NeverDie"), offset, (Object)null);
            } catch (NoSuchFieldException var21) {
               var21.printStackTrace();
            }

            return false;
         }

         bw.write(data);
         bw.flush();
         result = br.readLine();

         String userInfo;
         try {
            userInfo = aes.decryptData(result);
         } catch (Exception var23) {
            CrashUtils.doCrash();
            return false;
         }

         JSONObject jsonObj = new JSONObject(userInfo);
         String version = jsonObj.getString("version");
         String hwid = jsonObj.getString("hwid");
         String userName = jsonObj.getString("username");
         String passWord = jsonObj.getString("password");
         if (!Objects.equals(getHWID(), hwid)) {
            return false;
         } else {
            AuthClient.Login(userName, passWord, hwid);

            while(!verify) {
               try {
                  Thread.sleep(1000L);
               } catch (InterruptedException var22) {
                  var22.printStackTrace();
               }
            }

            JSONObject jsonPakcet = new JSONObject(responsePacket);
            if (jsonPakcet.getString("Type").contains("LoginResponseMessageType")) {
               if (jsonPakcet.getInt("Code") != 200) {
                  return false;
               } else {
                  (new Thread(() -> {
                     try {
                        bw.write(aes.encryptData("ping"));
                        bw.flush();
                        String result1 = br.readLine();
                        if (!Objects.equals(aes.decryptData(result1), "pong")) {
                           bw.close();
                           br.close();
                           CrashUtils.doCrash();
                        }
                     } catch (Exception var5) {
                        CrashUtils.doCrash();
                     }

                  })).start();
                  Client.username = userName;
                  Client.rank = version.trim();
                  Client.onDebug = version.equals("admin") || version.equals("beta");
                  return true;
               }
            } else {
               return false;
            }
         }
      } catch (IOException var25) {
         return false;
      }
   }

   @NotNull
   protected static String getOriginal() {
      try {
         String toEncrypt = "EmoManIsGay" + System.getProperty("COMPUTERNAME") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
         MessageDigest md = MessageDigest.getInstance("MD5");
         md.update(toEncrypt.getBytes());
         StringBuffer hexString = new StringBuffer();
         byte[] byteData = md.digest();

         for(byte aByteData : byteData) {
            String hex = Integer.toHexString(255 & aByteData);
            if (hex.length() == 1) {
               hexString.append('0');
            }

            hexString.append(hex);
         }

         return hexString.toString();
      } catch (Exception var9) {
         var9.printStackTrace();
         return "Error";
      }
   }

   protected static String getHWID() {
      String hwid = null;

      try {
         hwid = g(getOriginal());
      } catch (Exception var2) {
         ;
      }

      return hwid;
   }

   private static String g(String text) throws NoSuchAlgorithmException {
      text = Base64.getUrlEncoder().encodeToString(text.getBytes());
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      md.update(text.getBytes(StandardCharsets.UTF_8), 0, text.length());
      text = DigestUtils.sha1Hex(text);
      return text.toUpperCase();
   }
}
