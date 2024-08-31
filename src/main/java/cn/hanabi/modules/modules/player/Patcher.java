package cn.hanabi.modules.modules.player;

import cn.hanabi.Hanabi;
import cn.hanabi.Wrapper;
import cn.hanabi.events.EventPacket;
import cn.hanabi.events.EventPreMotion;
import cn.hanabi.events.EventTick;
import cn.hanabi.events.EventWorldChange;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.PlayerUtil;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.utils.random.Random;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class Patcher extends Mod {
   private final Value mode = (new Value("Patcher", "Mode", 0)).LoadValue(new String[]{"Hypixel", "AACv4LessFlag"});
   private final Value pong = new Value("Patcher", "Pong", true);
   byte[] uuid = UUID.randomUUID().toString().getBytes();
   int count;
   private final Queue<TimestampedPacket> queue = new ConcurrentLinkedDeque();
   private final LinkedList list = new LinkedList();
   private int bypassValue = 0;
   private long lastTransaction = 0L;
   int biqiling;
   private int lastUid;
   private boolean checkReset;
   private boolean active;
   private double x;
   private double y;
   private double z;
   float cacheYaw;
   float cachePitch;
   private boolean collect = false;
   private int serverPosPacket = 0;
   private final TimeHelper generation = new TimeHelper();
   private final TimeHelper tick = new TimeHelper();
   private final TimeHelper collecttimer = new TimeHelper();
   private final TimeHelper choke = new TimeHelper();
   private final TimeHelper giga = new TimeHelper();

   public Patcher() {
      super("Patcher", Category.PLAYER);
   }

   @EventTarget
   public void onChangeWorld(EventWorldChange e) {
   }

   @EventTarget
   public void onPre(EventTick e) {
   }

   @EventTarget
   public void onPreUpdate(EventPreMotion e) {
      if (this.mode.isCurrentMode("Hypixel")) {
         this.base((EventPacket)null);
      }

   }

   @EventTarget
   public void onPacket(EventPacket event) {
      Packet packet = event.getPacket();
      if (this.mode.isCurrentMode("AACv4LessFlag")) {
         if (packet instanceof S08PacketPlayerPosLook) {
            S08PacketPlayerPosLook packetS08 = (S08PacketPlayerPosLook)packet;
            double x = packetS08.getX() - mc.thePlayer.posX;
            double y = packetS08.getY() - mc.thePlayer.posY;
            double z = packetS08.getZ() - mc.thePlayer.posZ;
            double diff = Math.sqrt(x * x + y * y + z * z);
            if (diff <= 8.0D) {
               event.setCancelled(true);
               mc.getNetHandler().getNetworkManager().sendPacket(new C06PacketPlayerPosLook(packetS08.getX(), packetS08.getY(), packetS08.getZ(), packetS08.getYaw(), packetS08.getPitch(), true));
            }
         }
      } else if (this.mode.isCurrentMode("HypixelSlime")) {
         if (event.getPacket() instanceof C03PacketPlayer) {
            PlayerCapabilities capabilities = new PlayerCapabilities();
            capabilities.disableDamage = false;
            capabilities.isFlying = true;
            capabilities.allowFlying = false;
            capabilities.isCreativeMode = false;
            mc.getNetHandler().addToSendQueue(new C13PacketPlayerAbilities(capabilities));
         }
      } else if (this.mode.isCurrentMode("Hypixel")) {
         this.base(event);
      }

   }

   public void onEnable() {
      super.onEnable();
      this.uuid = UUID.randomUUID().toString().getBytes();
      this.list.clear();
      if (mc.thePlayer.ticksExisted > 1) {
         PlayerUtil.tellPlayer("Login again to disable watchdog.");
      }

   }

   public void onDisable() {
   }

   private void checkUidVaild(EventPacket event) {
      if (event.getPacket() instanceof C0FPacketConfirmTransaction) {
         C0FPacketConfirmTransaction C0F = (C0FPacketConfirmTransaction)event.getPacket();
         int windowId = C0F.getWindowId();
         int uid = C0F.getUid();
         if (windowId == 0 && uid < 0) {
            int predictedUid = this.lastUid - 1;
            if (!this.checkReset) {
               if (uid == predictedUid) {
                  if (!this.active) {
                     this.active = true;
                  }
               } else {
                  this.active = false;
               }
            } else {
               if (uid != predictedUid) {
                  this.active = false;
               }

               this.checkReset = false;
            }

            this.lastUid = uid;
         }
      }

   }

   public void hypixel(EventPacket event) {
      if (!mc.isSingleplayer()) {
         if (event != null) {
            if (event.getPacket() instanceof S01PacketJoinGame) {
               this.setDisplayName("Hypixel");
               this.generation.reset();
               this.tick.reset();
               this.count = 0;
               this.lastTransaction = 0L;
               this.collect = true;
               this.queue.clear();
               this.list.clear();
               PlayerUtil.debugChat("Clear");
               this.giga.reset();
               this.bypassValue = 2000;
            }

            if (event.getPacket() instanceof S08PacketPlayerPosLook) {
               S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook)event.getPacket();
               if (mc.currentScreen instanceof GuiDownloadTerrain) {
                  mc.currentScreen = null;
               }

               if (packet.getYaw() == 0.0F && packet.getPitch() == 0.0F) {
                  event.setCancelled(true);
               } else if (this.count < 1) {
                  event.setCancelled(true);
                  this.biqiling = 0;
                  ++this.count;
               } else {
                  this.x = packet.getX();
                  this.y = packet.getY();
                  this.z = packet.getZ();
                  if (this.giga.isDelayComplete(2000L)) {
                     this.biqiling = 6;
                     this.giga.reset();
                  }
               }
            }

            if (event.getPacket() instanceof C00PacketKeepAlive) {
               event.setCancelled(true);
               if (((Boolean)this.pong.getValue()).booleanValue()) {
                  this.queue.add(new Patcher.TimestampedPacket(event.getPacket(), System.currentTimeMillis()));
               } else {
                  this.list.add(event.getPacket());
               }
            }

            if (event.getPacket() instanceof C0FPacketConfirmTransaction && ((C0FPacketConfirmTransaction)event.getPacket()).getUid() < 0 && ((C0FPacketConfirmTransaction)event.getPacket()).getWindowId() == 0) {
               this.lastTransaction = System.currentTimeMillis();
               event.setCancelled(true);
               if (((Boolean)this.pong.getValue()).booleanValue()) {
                  this.queue.add(new Patcher.TimestampedPacket(event.getPacket(), System.currentTimeMillis()));
               } else {
                  this.list.add(event.getPacket());
               }
            }

            if (event.getPacket() instanceof C03PacketPlayer) {
               C03PacketPlayer packet = (C03PacketPlayer)event.getPacket();
               if (this.collect && !packet.isMoving() && !packet.getRotating()) {
                  event.setCancelled(true);
               }

               if (this.biqiling > 0) {
                  event.setCancelled(true);
                  --this.biqiling;
               }

               if (!event.isCancelled()) {
                  if (((Boolean)this.pong.getValue()).booleanValue()) {
                     this.queue.add(new Patcher.TimestampedPacket(event.getPacket(), System.currentTimeMillis()));
                     event.setCancelled(true);
                  } else {
                     this.list.add(event.getPacket());
                     event.setCancelled(true);
                  }
               }
            }
         } else {
            if (mc.thePlayer == null || mc.theWorld == null) {
               return;
            }

            if (this.tick.isDelayComplete(10000L) || !this.generation.isDelayComplete(5000L)) {
               this.collect = true;
               this.tick.reset();
               this.choke.reset();
               this.collecttimer.reset();
            }

            if (this.list.isEmpty() && this.queue.isEmpty()) {
               return;
            }

            long collectvalue = this.collect ? (long)(!this.generation.isDelayComplete(5000L) ? 2000 : 500 + this.randomInt(50)) : 180L;
            if (((Boolean)this.pong.getValue()).booleanValue()) {
               if (Math.abs(this.lastTransaction - System.currentTimeMillis()) <= 200L && this.choke.isDelayComplete(750L) && this.collect) {
                  PlayerUtil.debugChat("Reset");
                  this.bypassValue = 0;
                  this.collect = false;
               }

               for(Patcher.TimestampedPacket timestampedPacket : this.queue) {
                  long timestamp = timestampedPacket.timestamp;
                  if (Math.abs(timestamp - System.currentTimeMillis()) >= (long)this.bypassValue) {
                     Wrapper.sendPacketNoEvent(timestampedPacket.packet);
                     this.queue.remove(timestampedPacket);
                  }
               }
            } else if (this.collecttimer.isDelayComplete(collectvalue)) {
               while(!this.list.isEmpty()) {
                  Wrapper.sendPacketNoEvent((Packet)this.list.poll());
               }

               this.collect = false;
               this.collecttimer.reset();
            }
         }

      }
   }

   public void base(EventPacket event) {
      if (!mc.isSingleplayer()) {
         if (event != null) {
            if (event.getPacket() instanceof S07PacketRespawn) {
               this.queue.clear();
               this.checkReset = true;
            }

            if (event.getPacket() instanceof S08PacketPlayerPosLook) {
               S08PacketPlayerPosLook serverSidePosition = (S08PacketPlayerPosLook)event.getPacket();
               if (mc.currentScreen instanceof GuiDownloadTerrain) {
                  mc.currentScreen = null;
               }

               float serverPitch = serverSidePosition.getPitch();
               float serverYaw = serverSidePosition.getYaw();
               if (serverPitch == 0.0F && serverYaw == 0.0F) {
                  event.setCancelled(true);
               }
            }

            if (event.getPacket() instanceof C0FPacketConfirmTransaction) {
               this.lastTransaction = System.currentTimeMillis();
               this.checkUidVaild(event);
               if (this.active) {
                  event.setCancelled(true);
                  this.queue.add(new Patcher.TimestampedPacket(event.getPacket(), System.currentTimeMillis()));
               }
            }
         } else {
            if (Math.abs(this.lastTransaction - System.currentTimeMillis()) <= 200L && this.tick.isDelayComplete(10000L)) {
               this.bypassValue = 300 + Random.nextInt(40, 60);
               Hanabi.INSTANCE.println("Reset");
               this.tick.reset();
            }

            if (this.collecttimer.isDelayComplete((long)this.bypassValue)) {
               this.collecttimer.reset();
               Hanabi.INSTANCE.println(String.valueOf(this.queue.size()));

               while(this.queue.size() > 1) {
                  Wrapper.sendPacketNoEvent(((Patcher.TimestampedPacket)this.queue.poll()).packet);
               }
            }
         }

      }
   }

   private static class TimestampedPacket {
      private final Packet packet;
      private final long timestamp;

      public TimestampedPacket(Packet packet, long timestamp) {
         super();
         this.packet = packet;
         this.timestamp = timestamp;
      }
   }
}
