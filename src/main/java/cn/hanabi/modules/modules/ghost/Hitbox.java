package cn.hanabi.modules.modules.ghost;

import cn.hanabi.events.EventUpdate;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.modules.ModManager;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;

public class Hitbox extends Mod {
   public static Value minsize = new Value("Hitbox", "Min-Size", 0.1D, 0.1D, 0.8D, 0.01D);
   public static Value maxsize = new Value("Hitbox", "Max-Size", 0.25D, 0.1D, 1.0D, 0.01D);

   public Hitbox() {
      super("Hitbox", Category.GHOST);
   }

   public static float getSize() {
      double min = Math.min(((Double)minsize.getValue()).doubleValue(), ((Double)maxsize.getValue()).doubleValue());
      double max = Math.max(((Double)minsize.getValue()).doubleValue(), ((Double)maxsize.getValue()).doubleValue());
      return (float)(ModManager.getModule("Hitbox").isEnabled() ? Math.random() * (max - min) + min : 0.10000000149011612D);
   }

   @EventTarget
   public void onUpdate(EventUpdate e) {
      this.setDisplayName("Size: " + maxsize.getValueState());
   }
}
