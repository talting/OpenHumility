package cn.hanabi.modules.modules.ghost;

import cn.hanabi.events.EventUpdate;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;

public class LegitVelocity extends Mod {
   public Value chance = new Value("LegitVelocity", "Chance", 100.0D, 0.0D, 100.0D, 1.0D);
   public Value verti = new Value("LegitVelocity", "Vertical", 100.0D, 0.0D, 100.0D, 1.0D);
   public Value hori = new Value("LegitVelocity", "Horizontal", 100.0D, 0.0D, 100.0D, 1.0D);

   public LegitVelocity() {
      super("LegitVelocity", Category.GHOST);
   }

   @EventTarget
   public void onUpdate(EventUpdate event) {
      if (mc.thePlayer.maxHurtResistantTime == mc.thePlayer.hurtResistantTime && mc.thePlayer.maxHurtResistantTime != 0) {
         double random = Math.random();
         random = random * 100.0D;
         if (random < (double)((Double)this.chance.getValueState()).intValue()) {
            float hori = ((Double)this.hori.getValueState()).floatValue();
            hori = hori / 100.0F;
            float verti = ((Double)this.verti.getValueState()).floatValue();
            verti = verti / 100.0F;
            mc.thePlayer.motionX *= (double)hori;
            mc.thePlayer.motionZ *= (double)hori;
            mc.thePlayer.motionY *= (double)verti;
         } else {
            mc.thePlayer.motionX *= 1.0D;
            mc.thePlayer.motionY *= 1.0D;
            mc.thePlayer.motionZ *= 1.0D;
         }

      }
   }
}
