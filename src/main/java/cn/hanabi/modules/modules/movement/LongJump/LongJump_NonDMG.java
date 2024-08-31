package cn.hanabi.modules.modules.movement.LongJump;

import cn.hanabi.events.EventMove;
import cn.hanabi.modules.ModManager;
import cn.hanabi.utils.MoveUtils;
import cn.hanabi.utils.PlayerUtil;
import net.minecraft.client.Minecraft;

public class LongJump_NonDMG {
   final Minecraft mc = Minecraft.getMinecraft();
   private int stage;
   private double speed;
   private double verticalSpeed;

   public LongJump_NonDMG() {
      super();
   }

   public void onMove(EventMove e) {
      if (MoveUtils.isOnGround(0.01D) || this.stage > 0) {
         switch(this.stage) {
         case 0:
            this.mc.thePlayer.setPosition(this.mc.thePlayer.posX, this.mc.thePlayer.posY + 0.004D * Math.random(), this.mc.thePlayer.posZ);
            this.verticalSpeed = PlayerUtil.getBaseJumpHeight();
            this.speed = MoveUtils.getBaseMoveSpeed(0.2873D, 0.1D) * 2.149D;
            break;
         case 1:
            this.speed *= 0.65D;
         }

         e.setY(this.verticalSpeed);
         if (this.stage > 8) {
            this.speed *= 0.98D;
            this.verticalSpeed -= 0.035D;
         } else {
            this.speed *= 0.99D;
            this.verticalSpeed *= 0.65D;
         }

         ++this.stage;
         if (MoveUtils.isOnGround(0.01D) && this.stage > 4) {
            ModManager.getModule("LongJump").set(false);
         }

         MoveUtils.setMotion(e, Math.max(MoveUtils.getBaseMoveSpeed(0.2873D, 0.1D), this.speed));
      }

   }

   public void onEnable() {
      this.stage = 0;
   }

   public void onDisable() {
      this.stage = 0;
   }
}
