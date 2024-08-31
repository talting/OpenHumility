package cn.hanabi.events;

import com.darkmagician6.eventapi.events.callables.EventCancellable;

public class EventJump extends EventCancellable {
   public float yaw;

   public EventJump(float yaw) {
      super();
      this.yaw = yaw;
   }
}
