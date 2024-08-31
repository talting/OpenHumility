package cn.hanabi.modules.modules.combat;

import cn.hanabi.events.EventTick;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;
import net.minecraft.network.play.client.C16PacketClientStatus.EnumState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class AutoSoup extends Mod {
   public static Value slot = new Value("AutoSoup", "Slot", 7.0D, 1.0D, 9.0D, 1.0D);
   public static Value delay = new Value("AutoSoup", "Delay", 0.0D, 0.0D, 2.0D, 0.05D);
   public static Value health = new Value("AutoSoup", "Health", 14.0D, 1.0D, 20.0D, 0.5D);
   public static Value bowlMode = new Value("AutoSoup", "bowlMode", 0);
   public static Value openInvValue = new Value("AutoSoup", "OpenInv", false);
   public static Value simInvValue = new Value("AutoSoup", "simulateInv", false);
   TimeHelper timer = new TimeHelper();

   public AutoSoup() {
      super("AutoSoup", Category.COMBAT);
      bowlMode.LoadValue(new String[]{"Drop", "Move", "Stay"});
   }

   @EventTarget
   private void onUpdate(EventTick event) {
      if (this.timer.isDelayComplete(Double.valueOf(1000.0D * ((Double)delay.getValueState()).doubleValue()))) {
         int soupInHotbar = this.findItem(36, 45, Items.mushroom_stew).intValue();
         if ((double)mc.thePlayer.getHealth() <= ((Double)health.getValueState()).doubleValue() && soupInHotbar != -1) {
            mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange(soupInHotbar - 36));
            mc.getNetHandler().getNetworkManager().sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventoryContainer.getSlot(soupInHotbar).getStack()));
            if (bowlMode.isCurrentMode("Drop")) {
               mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            }

            mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            this.timer.reset();
         } else {
            int bowlInHotbar = this.findItem(36, 45, Items.bowl).intValue();
            if (bowlMode.isCurrentMode("Move") && bowlInHotbar != -1) {
               if (((Boolean)openInvValue.getValueState()).booleanValue() && !(mc.currentScreen instanceof GuiInventory)) {
                  return;
               }

               boolean bowlMovable = false;

               for(int i = 9; i <= 36; ++i) {
                  ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                  if (itemStack == null) {
                     bowlMovable = true;
                     break;
                  }

                  if (itemStack.getItem() == Items.bowl && itemStack.stackSize < 64) {
                     bowlMovable = true;
                     break;
                  }
               }

               if (bowlMovable) {
                  boolean openInventory = !(mc.currentScreen instanceof GuiInventory) && ((Boolean)simInvValue.getValueState()).booleanValue();
                  if (openInventory) {
                     mc.getNetHandler().getNetworkManager().sendPacket(new C16PacketClientStatus(EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                  }

                  mc.playerController.windowClick(0, bowlInHotbar, 0, 1, mc.thePlayer);
               }
            }

            int soupInInventory = this.findItem(9, 36, Items.mushroom_stew).intValue();
            if (soupInInventory != -1 && this.hasSpaceInHotbar()) {
               if (((Boolean)openInvValue.getValueState()).booleanValue() && !(mc.currentScreen instanceof GuiInventory)) {
                  return;
               }

               boolean openInventory = !(mc.currentScreen instanceof GuiInventory) && ((Boolean)simInvValue.getValueState()).booleanValue();
               if (openInventory) {
                  mc.getNetHandler().getNetworkManager().sendPacket(new C16PacketClientStatus(EnumState.OPEN_INVENTORY_ACHIEVEMENT));
               }

               mc.playerController.windowClick(0, soupInInventory, 0, 1, mc.thePlayer);
               if (openInventory) {
                  mc.getNetHandler().getNetworkManager().sendPacket(new C0DPacketCloseWindow());
               }

               this.timer.reset();
            }

         }
      }
   }

   public boolean hasSpaceInHotbar() {
      for(int i = 36; i <= 44; ++i) {
         if (mc.thePlayer.openContainer.getSlot(i).getStack() == null) {
            return true;
         }
      }

      return false;
   }

   public Integer findItem(int startInclusive, int endInclusive, Item item) {
      for(int i = startInclusive; i <= endInclusive; ++i) {
         if (mc.thePlayer.openContainer.getSlot(i).getStack() != null && mc.thePlayer.openContainer.getSlot(i).getStack().getItem() == item) {
            return i;
         }
      }

      return null;
   }
}
