package cn.hanabi.modules.modules.movement.Fly;

import cn.hanabi.events.EventMove;
import cn.hanabi.events.EventPreMotion;
import cn.hanabi.utils.TimeHelper;
import net.minecraft.client.Minecraft;

public class Fly_Hypixel {
   Minecraft mc = Minecraft.getMinecraft();
   TimeHelper timer = new TimeHelper();

   public Fly_Hypixel() {
      super();
   }

   public void onPre(EventPreMotion e) {
      if (this.timer.isDelayComplete(850L)) {
         this.HClip(0.5D);
         this.timer.reset();
      }

   }

   public void onMove(EventMove event) {
      event.setX(0.0D);
      event.setY(0.0D);
      event.setZ(0.0D);
   }

   private void HClip(double horizontal) {
      double playerYaw = Math.toRadians((double)this.mc.thePlayer.rotationYaw);
      this.mc.thePlayer.setPosition(this.mc.thePlayer.posX + horizontal * -Math.sin(playerYaw), this.mc.thePlayer.posY, this.mc.thePlayer.posZ + horizontal * Math.cos(playerYaw));
   }
}
