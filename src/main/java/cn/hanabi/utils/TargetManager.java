package cn.hanabi.utils;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;

public class TargetManager {
   private static final ArrayList target = new ArrayList();

   public TargetManager() {
      super();
   }

   public static ArrayList getTarget() {
      return target;
   }

   public static boolean isTarget(EntityPlayer player) {
      for(Object o : target) {
         String friendlist = (String)o;
         if (friendlist.equalsIgnoreCase(player.getName())) {
            return true;
         }
      }

      return false;
   }

   public static boolean isTarget(String player) {
      for(Object o : target) {
         String targetlist = (String)o;
         if (targetlist.equalsIgnoreCase(player)) {
            return true;
         }
      }

      return false;
   }
}
