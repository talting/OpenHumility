package cn.hanabi.modules.modules.render;

import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.value.Value;

public class NoFov extends Mod {
   public static Value fovspoof = new Value("NoFov", "Fov", 1.0D, 0.1D, 1.5D, 0.01D);

   public NoFov() {
      super("NoFov", Category.RENDER);
   }
}
