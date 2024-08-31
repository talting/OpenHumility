package cn.hanabi.injection.mixins;

import cn.hanabi.Hanabi;
import cn.hanabi.events.EventPacket;
import cn.hanabi.injection.interfaces.INetworkManager;
import cn.hanabi.modules.modules.player.Disabler;
import cn.hanabi.utils.PacketHelper;
import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.types.EventType;
import com.google.common.collect.Queues;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import java.lang.reflect.Field;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({NetworkManager.class})
public abstract class MixinNetworkManager implements INetworkManager {
   @Shadow
   private INetHandler packetListener;
   private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
   @Shadow
   private Channel channel;
   @Final
   @Shadow
   private final Queue outboundPacketsQueue = Queues.newConcurrentLinkedQueue();

   public MixinNetworkManager() {
      super();
   }

   @Overwrite
   public void channelRead0(ChannelHandlerContext p_channelRead0_1_, Packet p_channelRead0_2_) throws Exception {
      if (this.channel.isOpen()) {
         try {
            if (p_channelRead0_2_ instanceof S3FPacketCustomPayload) {
               EventPacket event = new EventPacket(EventType.RECIEVE, p_channelRead0_2_);
               EventManager.call(event);
               PacketHelper.onPacketReceive(p_channelRead0_2_);
               if (event.isCancelled()) {
                  return;
               }

               p_channelRead0_2_.processPacket(this.packetListener);
            } else if (Disabler.getGrimPost() && Disabler.grimPostDelay(p_channelRead0_2_)) {
               Minecraft.getMinecraft().addScheduledTask(() -> {
                  return Disabler.storedPackets.add(p_channelRead0_2_);
               });
            } else {
               EventPacket event = new EventPacket(EventType.RECIEVE, p_channelRead0_2_);
               EventManager.call(event);
               PacketHelper.onPacketReceive(p_channelRead0_2_);
               if (event.isCancelled()) {
                  return;
               }

               p_channelRead0_2_.processPacket(this.packetListener);
            }
         } catch (ThreadQuickExitException var5) {
            ;
         }
      }

   }

   @Inject(
      method = {"sendPacket(Lnet/minecraft/network/Packet;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sendPacket(Packet packetIn, CallbackInfo ci) {
      try {
         if (packetIn instanceof C00Handshake) {
            C00Handshake handshakePacket = (C00Handshake)packetIn;
            Class clazz = handshakePacket.getClass();

            for(Field field : clazz.getDeclaredFields()) {
               if (field.getType() == String.class) {
                  field.setAccessible(true);
                  String targetIP = field.get(handshakePacket).toString();
                  if (Hanabi.INSTANCE.hypixelBypass) {
                     Hanabi.INSTANCE.println("Redirect to Hypixel");
                     field.set(handshakePacket, "mc.hypixel.net");
                  }
               }
            }
         }
      } catch (Throwable var10) {
         var10.printStackTrace();
      }

      EventPacket event = new EventPacket(EventType.SEND, packetIn);
      EventManager.call(event);
      if (event.isCancelled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"closeChannel(Lnet/minecraft/util/IChatComponent;)V"},
      at = {@At("RETURN")}
   )
   private void onClose(IChatComponent chatComponent, CallbackInfo ci) {
      Logger.getLogger("Closed");
   }

   @Shadow
   public abstract boolean isChannelOpen();

   public void sendPacketNoEvent(Packet packet) {
      if (this.channel != null && this.channel.isOpen()) {
         this.flushOutboundQueue();
         this.dispatchPacket(packet, (GenericFutureListener[])null);
      } else {
         this.outboundPacketsQueue.add(new InboundHandlerTuplePacketListener(packet, (GenericFutureListener[])null));
      }

   }

   @Shadow
   protected abstract void dispatchPacket(Packet var1, GenericFutureListener[] var2);

   @Shadow
   protected abstract void flushOutboundQueue();
}
