package cn.hanabi.modules.modules.world;

import cn.hanabi.events.EventPacket;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.Levenshtein;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import net.minecraft.network.play.server.S02PacketChat;

public class AntiSpammer extends Mod {
   public static final Levenshtein lt = new Levenshtein();
   public Value history = new Value("AntiSpammer", "History Message", 10.0D, 3.0D, 30.0D, 1.0D);
   public Value ratio = new Value("AntiSpammer", "Ratio", 0.6D, 0.1D, 1.0D, 0.01D);
   public ArrayList<String> historyChat = new ArrayList();

   public AntiSpammer() {
      super("AntiSpammer", Category.WORLD);
   }

   @EventTarget
   public void onFuckingPacket(EventPacket motherfucker) {
      if (mc.thePlayer != null && mc.theWorld != null && motherfucker.getPacket() instanceof S02PacketChat) {
         S02PacketChat packet = (S02PacketChat)motherfucker.getPacket();
         String message = packet.getChatComponent().getFormattedText().replaceAll("§.", "").replaceAll("[.*?]", "").replaceAll("<.*?>", "");
         StringBuilder result = new StringBuilder();
         int isSpace = 0;

         for(char c : message.toCharArray()) {
            if (c == ' ') {
               ++isSpace;
            }

            if (isSpace >= 2) {
               result.append(c);
            }
         }

         result = new StringBuilder(result.toString().replace(" ", ""));

         for(String history : this.historyChat) {
            double similar = (double)lt.getSimilarityRatio(result.toString(), history);
            if (similar > ((Double)this.ratio.getValueState()).doubleValue()) {
               if (this.getDisplayName() != null) {
                  this.setDisplayName(Integer.parseInt(this.getDisplayName()) + 1 + "");
               } else {
                  this.setDisplayName("1");
               }

               motherfucker.setCancelled(true);
               break;
            }
         }

         this.historyChat.add(result.toString());
         if (this.historyChat.size() > ((Double)this.history.getValueState()).intValue()) {
            this.historyChat.remove(0);
         }
      }

   }
}
