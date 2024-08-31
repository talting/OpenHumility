package cn.hanabi.modules.modules.world;

import cn.hanabi.events.EventPacket;
import cn.hanabi.events.EventUpdate;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.modules.ModManager;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class AntiBot extends Mod {
   private static final Value mode = new Value("AntiBot", "Mode", 0);
   private static final List invalid = new CopyOnWriteArrayList();
   private static final List whitelist = new CopyOnWriteArrayList();
   public static Value armor = new Value("AntiBot", "ArmorEmpty", true);
   public int count = 0;

   public AntiBot() {
      super("AntiBot", Category.COMBAT);
      mode.LoadValue(new String[]{"Hypixel", "Mineplex", "Advanced", "MineLand", "QuickMacro", "JobWars"});
      this.setState(true);
   }

   public static boolean isBot(Entity e) {
      if (e instanceof EntityPlayer && ModManager.getModule("AntiBot").isEnabled()) {
         EntityPlayer player = (EntityPlayer)e;
         if (((Boolean)armor.getValueState()).booleanValue() && ((EntityPlayer)e).inventory.armorInventory[0] == null && ((EntityPlayer)e).inventory.armorInventory[1] == null && ((EntityPlayer)e).inventory.armorInventory[2] == null && ((EntityPlayer)e).inventory.armorInventory[3] == null) {
            return true;
         } else {
            if (mode.isCurrentMode("QuickMacro") && mc.thePlayer.inventory.armorInventory[3] != null && ((EntityPlayer)e).inventory.armorInventory[3] != null) {
               ItemStack myHead = mc.thePlayer.inventory.armorInventory[3];
               ItemArmor myItemArmor = (ItemArmor)myHead.getItem();
               ItemStack entityHead = ((EntityPlayer)e).inventory.armorInventory[3];
               ItemArmor entityItemArmor = (ItemArmor)entityHead.getItem();
               if (entityItemArmor.getColor(entityHead) == 10511680) {
                  return true;
               }

               if (myItemArmor.getColor(myHead) == entityItemArmor.getColor(entityHead)) {
                  return true;
               }

               if (!inTab(player)) {
                  return true;
               }
            }

            if (mode.isCurrentMode("JobWars") && player.ticksExisted < 100) {
               return true;
            } else if (mode.isCurrentMode("Hypixel")) {
               return !inTab(player) && !whitelist.contains(player);
            } else {
               return mode.isCurrentMode("Mineplex") && !Float.isNaN(player.getHealth());
            }
         }
      } else {
         return false;
      }
   }

   private static boolean inTab(EntityLivingBase entity) {
      for(NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
         if (info != null && info.getGameProfile() != null && info.getGameProfile().getName().contains(entity.getName())) {
            return true;
         }
      }

      return false;
   }

   public void onEnable() {
      super.onEnable();
   }

   public void onDisable() {
      super.onDisable();
   }

   @EventTarget
   public void onReceivePacket(EventPacket event) {
   }

   @EventTarget
   public void onUpdate(EventUpdate e) {
      this.setDisplayName(mode.getModeAt(mode.getCurrentMode()));
      if (!mc.isSingleplayer()) {
         if (mc.thePlayer.ticksExisted < 5) {
            whitelist.clear();
         }

         if (mc.thePlayer.ticksExisted % 60 == 0) {
            whitelist.clear();
         }

         if (mode.isCurrentMode("Hypixel") && !mc.theWorld.getLoadedEntityList().isEmpty()) {
            for(Entity ent : mc.theWorld.getLoadedEntityList()) {
               if (ent instanceof EntityPlayer && !whitelist.contains(ent)) {
                  String formatted = ent.getDisplayName().getFormattedText();
                  if (formatted.startsWith("§r§8[NPC]")) {
                     return;
                  }

                  if (!ent.isInvisible()) {
                     whitelist.add(ent);
                  }

                  if (ent.hurtResistantTime == 8) {
                     whitelist.add(ent);
                  }
               }
            }
         }

      }
   }
}
