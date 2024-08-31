package cn.hanabi.modules.modules.movement;

import cn.hanabi.events.EventUpdate;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.BlockUtils;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.block.BlockLiquid;

public class WaterSpeed extends Mod {
   public static Value speed = new Value("WaterSpeed", "Speed ", 1.2D, 1.0D, 1.5D, 0.1D);

   public WaterSpeed() {
      super("WaterSpeed", Category.MOVEMENT);
   }

   @EventTarget
   public void onUpdate(EventUpdate event) {
      if (mc.thePlayer.isInWater() && BlockUtils.getBlock(mc.thePlayer.getPosition()) instanceof BlockLiquid) {
         mc.thePlayer.motionX *= ((Double)speed.getValue()).doubleValue();
         mc.thePlayer.motionZ *= ((Double)speed.getValue()).doubleValue();
      }

   }
}
