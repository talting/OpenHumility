package cn.hanabi.modules.modules.movement.Fly;

import cn.hanabi.events.EventPacket;
import cn.hanabi.utils.MoveUtils;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;

public class Fly_AACv5 {
   Minecraft mc = Minecraft.getMinecraft();
   private boolean blockC03 = false;
   private final ArrayList<C03PacketPlayer> cacheList = new ArrayList();

   public Fly_AACv5() {
      super();
   }

   public void onPacket(EventPacket event) {
      Packet packet = event.getPacket();
      if (this.blockC03 && packet instanceof C03PacketPlayer) {
         this.cacheList.add((C03PacketPlayer)packet);
         event.setCancelled(true);
         if (this.cacheList.size() > 7) {
            this.sendC03();
         }
      }

   }

   public void onEnable() {
      this.blockC03 = true;
   }

   public void onDisable() {
      this.sendC03();
      this.blockC03 = false;
   }

   public void onUpdate() {
      double vanillaSpeed = ((Double)Fly.timer.getValue()).doubleValue();
      this.mc.thePlayer.capabilities.isFlying = false;
      this.mc.thePlayer.motionY = 0.0D;
      this.mc.thePlayer.motionX = 0.0D;
      this.mc.thePlayer.motionZ = 0.0D;
      if (this.mc.gameSettings.keyBindJump.isKeyDown()) {
         this.mc.thePlayer.motionY += vanillaSpeed;
      }

      if (this.mc.gameSettings.keyBindSneak.isKeyDown()) {
         this.mc.thePlayer.motionY -= vanillaSpeed;
      }

      MoveUtils.strafe(vanillaSpeed);
   }

   private void sendC03() {
      this.blockC03 = false;

      for(C03PacketPlayer packet : this.cacheList) {
         this.mc.getNetHandler().addToSendQueue(packet);
         if (packet.isMoving()) {
            this.mc.getNetHandler().addToSendQueue(new C04PacketPlayerPosition(packet.getPositionX(), 1.0E159D, packet.getPositionZ(), true));
            this.mc.getNetHandler().addToSendQueue(new C04PacketPlayerPosition(packet.getPositionX(), packet.getPositionY(), packet.getPositionZ(), true));
         }
      }

      this.cacheList.clear();
      this.blockC03 = true;
   }
}
