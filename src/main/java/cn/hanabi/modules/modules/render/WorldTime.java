package cn.hanabi.modules.modules.render;

import cn.hanabi.events.EventPacket;
import cn.hanabi.events.EventRender;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

public class WorldTime extends Mod {
   public final Value time = new Value("World Time", "Time", 0.0D, 0.0D, 24.0D, 1.0D);

   public WorldTime() {
      super("World Time", Category.RENDER);
   }

   @EventTarget
   public void onRenderWorld(EventRender event) {
      mc.theWorld.setWorldTime((long)((Double)this.time.getValue()).intValue() * 1000L);
   }

   @EventTarget
   public void onPacket(EventPacket event) {
      if (event.packet instanceof S03PacketTimeUpdate) {
         event.setCancelled(true);
      }

   }
}
