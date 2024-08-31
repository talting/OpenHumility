package cn.hanabi.utils;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class CrashUtils {
   public CrashUtils() {
      super();
   }

   public static void doCrash() {
      try {
         Field field = Unsafe.class.getDeclaredField("theUnsafe");
         field.setAccessible(true);
         Unsafe unsafe = null;

         try {
            unsafe = (Unsafe)field.get((Object)null);
         } catch (IllegalAccessException var7) {
            var7.printStackTrace();
         }

         Class cacheClass = null;

         try {
            cacheClass = Class.forName("java.lang.Integer$IntegerCache");
         } catch (ClassNotFoundException var6) {
            var6.printStackTrace();
         }

         Field cache = cacheClass.getDeclaredField("cache");
         long offset = unsafe.staticFieldOffset(cache);
         unsafe.putObject(Integer.getInteger("SkidSense.pub NeverDie"), offset, (Object)null);
      } catch (NoSuchFieldException var8) {
         var8.printStackTrace();
      }

   }
}
