package cn.hanabi.injection.mixins;

import cn.hanabi.modules.modules.player.Disabler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({NetHandlerPlayClient.class})
public abstract class MixinNetHandlerPlayClient {
   @Shadow
   public int currentServerMaxPlayers;
   @Shadow
   @Final
   private NetworkManager netManager;
   @Shadow
   private Minecraft gameController;
   @Shadow
   private WorldClient clientWorldController;
   @Shadow
   private boolean doneLoadingTerrain;

   public MixinNetHandlerPlayClient() {
      super();
   }

   @Shadow
   public abstract void addToSendQueue(Packet var1);

   @Overwrite
   public void handleConfirmTransaction(S32PacketConfirmTransaction p_handleConfirmTransaction_1_) {
      PacketThreadUtil.checkThreadAndEnqueue(p_handleConfirmTransaction_1_, (NetHandlerPlayClient)(Object)this, this.gameController);
      Container container = null;
      EntityPlayer entityPlayer = this.gameController.thePlayer;
      if (p_handleConfirmTransaction_1_.getWindowId() == 0) {
         container = entityPlayer.inventoryContainer;
      } else if (p_handleConfirmTransaction_1_.getWindowId() == entityPlayer.openContainer.windowId) {
         container = entityPlayer.openContainer;
      }

      if (container != null && !p_handleConfirmTransaction_1_.func_148888_e()) {
         C0FPacketConfirmTransaction packet = new C0FPacketConfirmTransaction(p_handleConfirmTransaction_1_.getWindowId(), p_handleConfirmTransaction_1_.getActionNumber(), true);
         if (Disabler.getGrimPost()) {
            Disabler.fixC0F(packet);
         } else {
            this.addToSendQueue(packet);
         }
      }

   }
}
