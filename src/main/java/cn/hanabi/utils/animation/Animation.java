package cn.hanabi.utils.animation;

public class Animation {
   private long startTime = 0L;
   private Animation.EnumAnimationState state = Animation.EnumAnimationState.NOT_STARTED;
   private double from;
   private double to;
   private int duration;

   public Animation(double from, double to, int duration) {
      super();
      this.from = from;
      this.to = to;
      this.duration = duration;
   }

   public void start() {
      this.state = Animation.EnumAnimationState.DURING;
      this.startTime = System.currentTimeMillis();
   }

   public double getNow() {
      if (this.state == Animation.EnumAnimationState.NOT_STARTED) {
         return this.from;
      } else if (this.state == Animation.EnumAnimationState.DURING) {
         double percent = (double)(System.currentTimeMillis() - this.startTime) / (double)this.duration;
         if (percent >= 1.0D) {
            this.state = Animation.EnumAnimationState.FINISHED;
            return this.to;
         } else {
            return this.from + (this.to - this.from) * EaseUtils.easeOutQuad(percent);
         }
      } else if (this.state == Animation.EnumAnimationState.FINISHED) {
         return this.to;
      } else {
         throw new IllegalStateException("Unknown animation state");
      }
   }

   public Animation.EnumAnimationState getState() {
      return this.state;
   }

   public double getFrom() {
      return this.from;
   }

   public double getTo() {
      return this.to;
   }

   public static enum EnumAnimationState {
      NOT_STARTED,
      DURING,
      FINISHED;

      private EnumAnimationState() {
      }
   }
}
