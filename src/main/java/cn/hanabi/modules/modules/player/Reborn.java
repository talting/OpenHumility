package cn.hanabi.modules.modules.player;

import cn.hanabi.events.EventMove;
import cn.hanabi.events.EventPacket;
import cn.hanabi.events.EventUpdate;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.ClientUtil;
import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class Reborn extends Mod {
   public static boolean isGhost = false;

   @EventTarget
   public void onUpdate(EventUpdate e) {
      if (mc.currentScreen instanceof GuiGameOver) {
         mc.displayGuiScreen((GuiScreen)null);
         mc.thePlayer.isDead = false;
         mc.thePlayer.setHealth(20.0F);
         mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
         isGhost = true;
         ClientUtil.displayMessage("§cYou are now a ghost.");
      }

   }

   @EventTarget
   public void onPacket(EventPacket e) {
      if (e.packet instanceof S08PacketPlayerPosLook && isGhost) {
         isGhost = false;
      }

   }

   @EventTarget
   public void onMove(EventMove e) {
      if (isGhost) {
         e.x = 0.0D;
         e.z = 0.0D;
      }

   }

   public void onDisable() {
      if (isGhost && mc.thePlayer != null) {
         mc.thePlayer.respawnPlayer();
         isGhost = false;
      }

      super.onEnable();
   }

   public Reborn() {
      super("Reborn", Category.PLAYER);
   }
}
