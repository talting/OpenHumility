package cn.hanabi.modules.modules.world;

import cn.hanabi.events.EventPreMotion;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.PlayerUtil;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;

public class AntiFall extends Mod {
   public static Value falldistance = new Value("AntiFall", "FallDistance", 10.0D, 5.0D, 30.0D, 0.1D);
   public static Value delay = new Value("AntiFall", "Delay", 800.0D, 200.0D, 2000.0D, 100.0D);
   public Value onlyvoid = new Value("AntiFall", "OnlyVoid", true);
   public Value nodmg = new Value("AntiFall", "0 DMG", true);
   TimeHelper timer = new TimeHelper();
   boolean falling;

   public AntiFall() {
      super("AntiFall", Category.WORLD);
   }

   @EventTarget
   public void onUpdate(EventPreMotion e) {
      boolean canFall = !mc.thePlayer.onGround;
      boolean aboveVoid = !((Boolean)this.onlyvoid.getValue()).booleanValue() || PlayerUtil.isBlockUnder();
      if (canFall && PlayerUtil.isBlockUnder() && ((Boolean)this.nodmg.getValue()).booleanValue()) {
         e.setOnGround(true);
      }

      if ((double)mc.thePlayer.fallDistance >= ((Double)falldistance.getValue()).doubleValue() && aboveVoid) {
         if (!this.falling) {
            PlayerUtil.debugChat("1");
            mc.getNetHandler().getNetworkManager().sendPacket(new C04PacketPlayerPosition(mc.thePlayer.posX + 1.0D + 0.004D * Math.random(), mc.thePlayer.posY, mc.thePlayer.posZ + 1.0D + 0.004D * Math.random(), false));
            this.falling = true;
         }

         mc.thePlayer.motionX = 0.0D;
         mc.thePlayer.motionY = 0.0D;
         mc.thePlayer.motionZ = 0.0D;
      } else {
         this.falling = false;
      }

   }
}
