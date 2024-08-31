package cn.hanabi.gui.superskidder.material.items.other;

import cn.hanabi.gui.superskidder.material.clickgui.AnimationUtils;

public class Shadow {
   public double alpha;
   public double size;
   public double finalSize;
   public double xPos;
   public double yPos;
   public boolean end;
   public boolean delete;
   AnimationUtils animate;
   AnimationUtils animate2;

   public Shadow(double x, double y, double finalSize) {
      super();
      this.xPos = x;
      this.yPos = y;
      this.animate = new AnimationUtils();
      this.animate2 = new AnimationUtils();
      this.finalSize = finalSize;
   }

   public void update() {
      if (!this.end) {
         this.size = this.animate.animate(this.finalSize, this.size, 0.30000001192092896D);
         this.alpha = this.animate2.animate(100.0D, this.alpha, 0.20000000298023224D);
      } else {
         this.alpha = this.animate2.animate(0.0D, this.alpha, 0.4000000059604645D);
         if (this.alpha == 0.0D) {
            this.delete = true;
         }
      }

      if (this.size >= this.finalSize) {
         this.end = true;
      }

   }
}
