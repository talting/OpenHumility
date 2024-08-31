package cn.hanabi.modules.modules.movement.Speed;

import cn.hanabi.events.EventPreMotion;
import cn.hanabi.events.EventPullback;
import cn.hanabi.events.EventWorldChange;
import cn.hanabi.gui.notifications.Notification;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.ClientUtil;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;

public class Speed extends Mod {
   public static Value mode = (new Value("Speed", "Mode", 1)).LoadValue(new String[]{"PushAbout"});
   private final Speed_PushAbout modePushAbout = new Speed_PushAbout();
   public Value lagback = new Value("Speed", "Lag Back Checks", true);
   public Value autodisable = new Value("Speed", "Auto Disable", true);

   public Speed() {
      super("Speed", Category.MOVEMENT);
   }

   @EventTarget
   public void onReload(EventWorldChange e) {
      if (((Boolean)this.autodisable.getValueState()).booleanValue()) {
         this.set(false);
      }

   }

   @EventTarget
   public void onUpdate(EventPreMotion e) {
      this.setDisplayName(mode.getModeAt(mode.getCurrentMode()));
      if (mode.isCurrentMode("PushAbout")) {
         this.modePushAbout.onUpdate(e);
      }

   }

   @EventTarget
   public void onPullback(EventPullback e) {
      if (((Boolean)this.lagback.getValueState()).booleanValue()) {
         ClientUtil.sendClientMessage("(LagBackCheck) Speed Disabled", Notification.Type.WARNING);
         this.set(false);
      }

   }
}
