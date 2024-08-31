package cn.hanabi.utils;

import cn.hanabi.Client;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;

public class ReflectionUtils {
   public ReflectionUtils() {
      super();
   }

   public static void clickMouse() {
      try {
         String s = !Client.map ? "clickMouse" : "func_147116_af";
         Minecraft mc = Minecraft.getMinecraft();
         Class c = mc.getClass();
         Method m = c.getDeclaredMethod(s);
         m.setAccessible(true);
         m.invoke(mc);
      } catch (Exception var4) {
         ;
      }

   }

   public static void rightClickMouse() {
      try {
         String s = !Client.map ? "rightClickMouse" : "func_147121_ag";
         Minecraft mc = Minecraft.getMinecraft();
         Class c = mc.getClass();
         Method m = c.getDeclaredMethod(s);
         m.setAccessible(true);
         m.invoke(mc);
      } catch (Exception var4) {
         ;
      }

   }

   public static void setLeftClickCounter(int i) {
      try {
         String s = !Client.map ? "leftClickCounter" : "field_71429_W";
         Minecraft mc = Minecraft.getMinecraft();
         Class c = mc.getClass();
         Field f = c.getDeclaredField(s);
         f.setAccessible(true);
         f.set(mc, Integer.valueOf(i));
      } catch (Exception var5) {
         ;
      }

   }

   public static void setRightClickDelayTimer(int i) {
      try {
         String s = !Client.map ? "rightClickDelayTimer" : "field_71467_ac";
         Minecraft mc = Minecraft.getMinecraft();
         Class c = mc.getClass();
         Field f = c.getDeclaredField(s);
         f.setAccessible(true);
         f.set(mc, Integer.valueOf(i));
      } catch (Exception var5) {
         ;
      }

   }
}
