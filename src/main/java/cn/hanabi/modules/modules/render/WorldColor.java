package cn.hanabi.modules.modules.render;

import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.value.Value;

public class WorldColor extends Mod {
   public final Value r = new Value("World Color", "Red Color", 0.0D, 0.0D, 255.0D, 1.0D);
   public final Value g = new Value("World Color", "Green Color", 0.0D, 0.0D, 255.0D, 1.0D);
   public final Value b = new Value("World Color", "Blue Color", 0.0D, 0.0D, 255.0D, 1.0D);
   public final Value a = new Value("World Color", "Alpha", 0.0D, 0.0D, 255.0D, 5.0D);

   public WorldColor() {
      super("World Color", Category.RENDER);
   }
}
