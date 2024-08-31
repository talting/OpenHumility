package cn.hanabi.modules.modules.movement;

import cn.hanabi.events.EventPacket;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.block.BlockAir;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;

public class HighJump extends Mod {
   private final Value mode = new Value("HighJump", "Mode", 0);
   private final Value boost = new Value("HighJump", "Boost", 0.5D, 0.1D, 5.0D, 0.05D);
   int counter = 0;
   int counter2 = 0;
   TimeHelper wait = new TimeHelper();

   public HighJump() {
      super("HighJump", Category.MOVEMENT);
      this.mode.addValue("Vanilla");
      this.mode.addValue("Hypixel");
   }

   public void onEnable() {
      this.counter = 0;
      this.counter2 = 0;
      super.onEnable();
   }

   public void onDisable() {
      super.onDisable();
   }

   @EventTarget
   public void onPacket(EventPacket e) {
      if (mc.thePlayer.onGround && mc.gameSettings.keyBindForward.isPressed() && this.wait.isDelayComplete(500L) && this.mode.isCurrentMode("Vanilla")) {
         mc.thePlayer.motionY = ((Double)this.boost.getValueState()).doubleValue();
         this.wait.reset();
      }

      boolean blockUnderneath = false;

      for(int i = 0; (double)i < mc.thePlayer.posY + 2.0D; ++i) {
         BlockPos pos = new BlockPos(mc.thePlayer.posX, (double)i, mc.thePlayer.posZ);
         if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir)) {
            blockUnderneath = true;
         }
      }

      if (this.mode.isCurrentMode("Hypixel") && !blockUnderneath && e.getPacket() instanceof C03PacketPlayer && mc.thePlayer.fallDistance > 8.0F) {
         mc.thePlayer.motionY = ((Double)this.boost.getValueState()).doubleValue();
      }

   }
}
