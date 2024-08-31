package cn.hanabi.modules.modules.movement.Fly;

import cn.hanabi.injection.interfaces.IKeyBinding;
import cn.hanabi.utils.PlayerUtil;
import net.minecraft.client.Minecraft;

public class Fly_Motion {
   Minecraft mc = Minecraft.getMinecraft();

   public Fly_Motion() {
      super();
   }

   public void onPre() {
      this.mc.thePlayer.motionY = 0.0D;
      if (PlayerUtil.MovementInput()) {
         PlayerUtil.setSpeed(((Double)Fly.timer.getValueState()).doubleValue() * 0.5D);
      } else {
         PlayerUtil.setSpeed(0.0D);
      }

      if (((IKeyBinding)this.mc.gameSettings.keyBindSneak).getPress()) {
         --this.mc.thePlayer.motionY;
      } else if (((IKeyBinding)this.mc.gameSettings.keyBindJump).getPress()) {
         ++this.mc.thePlayer.motionY;
      }

   }
}
