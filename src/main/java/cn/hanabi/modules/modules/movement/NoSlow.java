package cn.hanabi.modules.modules.movement;

import cn.hanabi.events.EventPacket;
import cn.hanabi.events.EventPostMotion;
import cn.hanabi.events.EventPreMotion;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.modules.ModManager;
import cn.hanabi.modules.modules.combat.KillAura;
import cn.hanabi.utils.MathUtils;
import cn.hanabi.utils.PlayerUtil;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S30PacketWindowItems;

public class NoSlow extends Mod {
   public Value mode = (new Value("NoSlow", "Mode", 0)).LoadValue(new String[]{"Vanilla", "NCP", "GAC"});
   TimeHelper ms = new TimeHelper();

   public NoSlow() {
      super("NoSlow", Category.MOVEMENT);
   }

   @EventTarget
   public void onPre(EventPreMotion e) {
      if (mc.thePlayer.isUsingItem() || mc.thePlayer.isBlocking() || KillAura.blockState && ModManager.getModule("KillAura").isEnabled()) {
         if (PlayerUtil.isMoving2()) {
            if (this.mode.isCurrentMode("GAC")) {
               if (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                  mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                  mc.getNetHandler().getNetworkManager().sendPacket(new C0FPacketConfirmTransaction(MathUtils.getRandom(102, 1000024123), (short)MathUtils.getRandom(102, 1000024123), true));
                  mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
               }

               if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
                  mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                  mc.getNetHandler().getNetworkManager().sendPacket(new C0FPacketConfirmTransaction(MathUtils.getRandom(102, 1000024123), (short)MathUtils.getRandom(102, 1000024123), true));
                  mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
               }
            }

            if (this.mode.isCurrentMode("NCP")) {
               ;
            }

         }
      }
   }

   @EventTarget
   public void onPost(EventPostMotion e) {
      if (mc.thePlayer.isUsingItem() || mc.thePlayer.isBlocking() || KillAura.blockState && ModManager.getModule("KillAura").isEnabled()) {
         if (PlayerUtil.isMoving2()) {
            if (this.mode.isCurrentMode("GAC") && (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow || mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
               mc.getNetHandler().getNetworkManager().sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
            }

            if (this.mode.isCurrentMode("NCP")) {
               mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
            }

         }
      }
   }

   @EventTarget
   public void onPacket(EventPacket event) {
      if (mc.thePlayer.isUsingItem()) {
         if (event.getPacket() instanceof S30PacketWindowItems) {
            event.setCancelled(true);
            PlayerUtil.debug("Gay Sir");
         }

      }
   }
}
