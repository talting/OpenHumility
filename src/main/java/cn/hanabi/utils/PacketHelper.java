package cn.hanabi.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

public enum PacketHelper {
   instance("instance", 0);

   private static int packetsPerSecondTemp = 0;
   private static int packetsPerSecond;
   private static long lastMS;
   private static boolean doneOneTime;
   private static long startTime;
   private static long lastReceiveTime;
   public static double tps;
   public static double lastTps;
   private static final List<Float> tpsList = new ArrayList();
   private static final float listTime = 300.0F;
   private static int tempTicks = 0;
   public static float fiveMinuteTPS = 0.0F;
   private static final TimeHelper th = new TimeHelper();
   private static final DecimalFormat df = new DecimalFormat();

   private PacketHelper(String s, int n2) {
   }

   public static void onPacketReceive(Packet event) {
      lastTps = tps;
      if (event instanceof S01PacketJoinGame) {
         tps = 20.0D;
         fiveMinuteTPS = 20.0F;
      }

      if (event instanceof S03PacketTimeUpdate) {
         long currentReceiveTime = System.currentTimeMillis();
         if (lastReceiveTime != -1L) {
            long timeBetween = currentReceiveTime - lastReceiveTime;
            double neededTps = (double)timeBetween / 50.0D;
            double niceTps = 20.0D;
            double multi = neededTps / 20.0D;
            tps = 20.0D / multi;
            if (tps < 0.0D) {
               tps = 0.0D;
            }

            if (tps > 20.0D) {
               tps = 20.0D;
            }
         }

         lastReceiveTime = currentReceiveTime;
      }

      if (event instanceof S03PacketTimeUpdate || event instanceof S00PacketKeepAlive) {
         ++packetsPerSecondTemp;
      }

   }

   public static void onUpdate() {
      if (th.isDelayComplete(2000L) && getServerLagTime() > 5000L) {
         th.reset();
         tps /= 2.0D;
      }

      if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) {
         tpsList.clear();
      }

      float tteemmpp = 0.0F;
      if (tempTicks >= 20) {
         tpsList.add(Float.valueOf((float)tps));
         tempTicks = 0;
      }

      if ((float)tpsList.size() >= 300.0F) {
         tpsList.clear();
         tpsList.add(Float.valueOf((float)tps));
      }

      for(Float aFloat : tpsList) {
         tteemmpp += aFloat.floatValue();
      }

      fiveMinuteTPS = tteemmpp / (float)tpsList.size();
      ++tempTicks;
      if (System.currentTimeMillis() - lastMS >= 1000L) {
         lastMS = System.currentTimeMillis();
         packetsPerSecond = packetsPerSecondTemp;
         packetsPerSecondTemp = 0;
      }

      if (packetsPerSecond < 1) {
         if (!doneOneTime) {
            startTime = System.currentTimeMillis();
            doneOneTime = true;
         }
      } else {
         if (doneOneTime) {
            doneOneTime = false;
         }

         startTime = 0L;
      }

   }

   public static long getServerLagTime() {
      return startTime <= 0L ? 0L : System.currentTimeMillis() - startTime;
   }

   public static char getTPSColorCode(double tps2) {
      if (tps2 >= 17.0D) {
         return 'a';
      } else if (tps2 >= 13.0D) {
         return 'e';
      } else {
         return (char)(tps2 > 9.0D ? 'c' : '4');
      }
   }
}
