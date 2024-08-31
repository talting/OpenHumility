package cn.hanabi.events;

import com.darkmagician6.eventapi.events.Event;

public class EventMouse implements Event {
   private final EventMouse.Button button;

   public EventMouse(EventMouse.Button button) {
      super();
      this.button = button;
   }

   public EventMouse.Button getButton() {
      return this.button;
   }

   public static enum Button {
      Left,
      Right,
      Middle;

      private Button() {
      }
   }
}
