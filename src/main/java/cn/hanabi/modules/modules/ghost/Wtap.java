package cn.hanabi.modules.modules.ghost;

import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.value.Value;

public class Wtap extends Mod {
   public Value range = new Value("WTap", "Range", 3.0D, 0.0D, 6.0D, 0.1D);
   public Value delay = new Value("WTap", "Delay", 500.0D, 100.0D, 2000.0D, 50.0D);
   public Value hold = new Value("WTap", "Held", 100.0D, 50.0D, 250.0D, 50.0D);

   public Wtap() {
      super("WTap", Category.GHOST);
   }
}
