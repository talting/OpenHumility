package cn.hanabi.events;

import com.darkmagician6.eventapi.events.Event;

public class EventMoveInput implements Event {
   public float moveStrafe;
   public float moveForward;
   public boolean jump;
   public boolean sneak;

   public EventMoveInput(float moveStrafe, float moveForward, boolean jump, boolean sneak) {
      super();
      this.moveStrafe = moveStrafe;
      this.moveForward = moveForward;
      this.jump = jump;
      this.sneak = sneak;
   }
}
