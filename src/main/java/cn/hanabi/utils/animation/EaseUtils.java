package cn.hanabi.utils.animation;

public class EaseUtils {
   public EaseUtils() {
      super();
   }

   public static double easeInSine(double x) {
      return 1.0D - Math.cos(x * 3.141592653589793D / 2.0D);
   }

   public static double easeOutSine(double x) {
      return Math.sin(x * 3.141592653589793D / 2.0D);
   }

   public static double easeInOutSine(double x) {
      return -(Math.cos(3.141592653589793D * x) - 1.0D) / 2.0D;
   }

   public static double easeInQuad(double x) {
      return x * x;
   }

   public static double easeOutQuad(double x) {
      return 1.0D - (1.0D - x) * (1.0D - x);
   }

   public static double easeInOutQuad(double x) {
      return x < 0.5D ? 2.0D * x * x : 1.0D - Math.pow(-2.0D * x + 2.0D, 2.0D) / 2.0D;
   }
}
