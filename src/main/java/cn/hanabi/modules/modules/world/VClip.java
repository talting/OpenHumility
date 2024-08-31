package cn.hanabi.modules.modules.world;

import cn.hanabi.events.EventUpdate;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;

public class VClip extends Mod {
   public Value clips = new Value("DownClip", "Down Value", 4.0D, 1.0D, 10.0D, 0.5D);

   public VClip() {
      super("DownClip", Category.WORLD);
   }

   @EventTarget
   public void onUpdate(EventUpdate e) {
      mc.getNetHandler().addToSendQueue(new C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - ((Double)this.clips.getValue()).doubleValue(), mc.thePlayer.posZ, true));
      mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - ((Double)this.clips.getValue()).doubleValue(), mc.thePlayer.posZ);
   }
}
