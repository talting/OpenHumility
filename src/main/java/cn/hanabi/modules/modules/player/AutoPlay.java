package cn.hanabi.modules.modules.player;

import cn.hanabi.events.EventChat;
import cn.hanabi.events.EventWorldChange;
import cn.hanabi.gui.notifications.Notification;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.ClientUtil;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.IChatComponent;

public class AutoPlay extends Mod {
   public Value delay = new Value("AutoPlay", "Delay", 5.0D, 1.0D, 10.0D, 1.0D);

   public AutoPlay() {
      super("AutoPlay", Category.PLAYER);
   }

   protected void onDisable() {
   }

   @EventTarget
   public void onPacket(EventChat event) {
      for(IChatComponent cc : event.getChatComponent().getSiblings()) {
         ClickEvent ce = cc.getChatStyle().getChatClickEvent();
         if (ce != null && ce.getAction() == Action.RUN_COMMAND && ce.getValue().contains("/play")) {
            ClientUtil.sendClientMessage("Play again in " + this.delay.getValue() + "s", Notification.Type.SUCCESS);
            (new Thread(() -> {
               try {
                  Thread.sleep(((Double)this.delay.getValue()).longValue() * 1000L);
               } catch (InterruptedException var3) {
                  var3.printStackTrace();
               }

               mc.thePlayer.sendQueue.addToSendQueue(new C01PacketChatMessage(ce.getValue()));
            })).start();
            event.setCancelled(true);
         }
      }

   }

   @EventTarget
   public void onWorld(EventWorldChange event) {
   }
}
