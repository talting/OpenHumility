package cn.hanabi.utils;

public class AnimationUtil {
   public AnimationUtil() {
      super();
   }

   public static float moveUD(float current, float end, float smoothSpeed, float minSpeed) {
      float movement = (end - current) * smoothSpeed;
      if (movement > 0.0F) {
         movement = Math.max(minSpeed, movement);
         movement = Math.min(end - current, movement);
      } else if (movement < 0.0F) {
         movement = Math.min(-minSpeed, movement);
         movement = Math.max(end - current, movement);
      }

      return current + movement;
   }

   public static float lstransition(float now, float desired, double speed) {
      double dif = (double)Math.abs(desired - now);
      float a = (float)Math.abs((double)(desired - (desired - Math.abs(desired - now))) / (100.0D - speed * 10.0D));
      float x = now;
      if (dif != 0.0D && dif < (double)a) {
         a = (float)dif;
      }

      if (dif > 0.0D) {
         if (now < desired) {
            x = now + a * RenderUtil.delta;
         } else if (now > desired) {
            x = now - a * RenderUtil.delta;
         }
      } else {
         x = desired;
      }

      if ((double)Math.abs(desired - x) < 0.05D && x != desired) {
         x = desired;
      }

      return x;
   }

   public static float calculateCompensation(float target, float current, long delta, int speed) {
      float diff = current - target;
      if (delta < 1L) {
         delta = 1L;
      }

      if (diff > (float)speed) {
         double xD = (double)((long)speed * delta / 16L) < 0.25D ? 0.5D : (double)((long)speed * delta / 16L);
         current = (float)((double)current - xD);
         if (current < target) {
            current = target;
         }
      } else if (diff < (float)(-speed)) {
         double xD = (double)((long)speed * delta / 16L) < 0.25D ? 0.5D : (double)((long)speed * delta / 16L);
         current = (float)((double)current + xD);
         if (current > target) {
            current = target;
         }
      } else {
         current = target;
      }

      return current;
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
         double xD = Math.max(speed * (double)delta / 16.0D, 0.5D);
         current = (float)((double)current - xD);
         if (current < target) {
            current = target;
         }
      } else if ((double)diff < -speed) {
         double xD = Math.max(speed * (double)delta / 16.0D, 0.5D);
         current = (float)((double)current + xD);
         if (current > target) {
            current = target;
         }
      } else {
         current = target;
      }

      return current;
   }
}
