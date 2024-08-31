package cn.hanabi.gui.superskidder.material.clickgui;

import cn.hanabi.utils.TimeHelper;

public class AnimationUtils {
   private static float defaultSpeed = 0.125F;
   private TimeHelper timerUtil = new TimeHelper();

   public AnimationUtils() {
      super();
   }

   public static float calculateCompensation(float target, float current, long delta, double speed) {
      float diff = current - target;
      if (delta < 1L) {
         delta = 1L;
      }

      if (delta > 1000L) {
         delta = 16L;
      }

      if ((double)diff > speed) {
         double xD = speed * (double)delta / 16.0D < 0.5D ? 0.5D : speed * (double)delta / 16.0D;
         if ((current = (float)((double)current - xD)) < target) {
            current = target;
         }
      } else if ((double)diff < -speed) {
         double xD = speed * (double)delta / 16.0D < 0.5D ? 0.5D : speed * (double)delta / 16.0D;
         if ((current = (float)((double)current + xD)) > target) {
            current = target;
         }
      } else {
         current = target;
      }

      return current;
   }

   public float mvoeUD(float current, float end, float minSpeed) {
      return this.moveUD(current, end, defaultSpeed, minSpeed);
   }

   public double animate(double target, double current, double speed) {
      if (this.timerUtil.delay(4.0F, true)) {
         boolean larger = target > current;
         if (speed < 0.0D) {
            speed = 0.0D;
         } else if (speed > 1.0D) {
            speed = 1.0D;
         }

         double dif = Math.max(target, current) - Math.min(target, current);
         double factor = dif * speed;
         if (factor < 0.1D) {
            factor = 0.1D;
         }

         current = larger ? current + factor : current - factor;
      }

      return current;
   }

   public float animate(float target, float current, float speed) {
      if (this.timerUtil.delay(4.0F, true)) {
         boolean larger = target > current;
         if (speed < 0.0F) {
            speed = 0.0F;
         } else if ((double)speed > 1.0D) {
            speed = 1.0F;
         }

         float dif = Math.max(target, current) - Math.min(target, current);
         float factor = dif * speed;
         if (factor < 0.1F) {
            factor = 0.1F;
         }

         current = larger ? current + factor : current - factor;
      }

      return (double)Math.abs(current - target) < 0.2D ? target : current;
   }

   public float moveUD(float current, float end, float smoothSpeed, float minSpeed) {
      float movement = 0.0F;
      if (this.timerUtil.delay(20.0F, true)) {
         movement = (end - current) * smoothSpeed;
         if (movement > 0.0F) {
            movement = Math.max(minSpeed, movement);
            movement = Math.min(end - current, movement);
         } else if (movement < 0.0F) {
            movement = Math.min(-minSpeed, movement);
            movement = Math.max(end - current, movement);
         }
      }

      return current + movement;
   }
}
