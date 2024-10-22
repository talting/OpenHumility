package cn.hanabi.modules.modules.ghost;

import cn.hanabi.events.EventUpdate;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;

public class Reach extends Mod {
   public static Value minreach = new Value("Reach", "Min-Range", 3.5D, 3.0D, 6.0D, 0.01D);
   public static Value maxreach = new Value("Reach", "Max-Range", 4.2D, 3.0D, 6.0D, 0.01D);
   public static Value throughWall = new Value("Reach", "Through Wall", true);

   public Reach() {
      super("Reach", Category.GHOST);
   }

   public static double getReach() {
      double min = Math.min(((Double)minreach.getValue()).doubleValue(), ((Double)maxreach.getValue()).doubleValue());
      double max = Math.max(((Double)minreach.getValue()).doubleValue(), ((Double)maxreach.getValue()).doubleValue());
      return Math.random() * (max - min) + min;
   }

   @EventTarget
   public void onUpdate(EventUpdate e) {
      this.setDisplayName("Range: " + maxreach.getValue());
   }
}
