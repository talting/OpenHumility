package cn.hanabi.modules.modules.player;

import cn.hanabi.Wrapper;
import cn.hanabi.events.EventPacket;
import cn.hanabi.events.EventPostMotion;
import cn.hanabi.events.EventPreMotion;
import cn.hanabi.injection.interfaces.IC03PacketPlayer;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.BlockUtils;
import cn.hanabi.utils.MoveUtils;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public class NoFall extends Mod {
   public static Value falls = new Value("NoFall", "Fall Distance", 2.5D, 1.5D, 4.0D, 0.01D);
   public static Value caneled = new Value("NoFall", "Canceled Distance", 1.0D, 0.0D, 3.0D, 0.1D);
   public static Value spoof = new Value("NoFall", "Spoof Location", 0.0325D, 0.01D, 0.05D, 0.0025D);
   private final float b = 1.0F;
   private final boolean c = true;
   private final Value mode = (new Value("NoFall", "Mode", 0)).LoadValue(new String[]{"Hypixel", "Mineplex", "AAC", "OnGround", "NoGround", "Verus", "Edit"});
   private final Value hypixelMode = (new Value("NoFall", "Hypixel Mode", 0)).LoadValue(new String[]{"Spoof", "Packet", "Edit", "Async"});
   private final Value aacMode = (new Value("NoFall", "AAC Mode", 0)).LoadValue(new String[]{"AACv4", "None"});
   private final ArrayList<C03PacketPlayer> aac4Packets = new ArrayList();
   public Value tryspoof = new Value("NoFall", "More Packet", true);
   public Value reset = new Value("NoFall", "F-Distance Reset", true);
   public Value cancel = new Value("NoFall", "Cancel Packet", true);
   public double fallDist = 0.0D;
   public TimeHelper timer = new TimeHelper();
   double lastFall;
   int times;
   boolean showed;
   double fall;
   double fallDist1;
   private int hypixel1;
   private int d;
   private int state;
   private double actualFallDistance;
   private boolean damage;
   private BlockPos startPos;
   private boolean aac4Fakelag = false;
   private boolean aac4PacketModify = false;
   private boolean needSpoof = false;

   public NoFall() {
      super("NoFall", Category.PLAYER);
   }

   public static boolean isBlockUnder() {
      if (mc.thePlayer.posY < 0.0D) {
         return false;
      } else {
         for(int off = 0; off < (int)mc.thePlayer.posY + 2; off += 2) {
            AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0.0D, (double)(-off), 0.0D);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
               return true;
            }
         }

         return false;
      }
   }

   public static int getJumpEffect() {
      return mc.thePlayer.isPotionActive(Potion.jump) ? mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1 : 0;
   }

   private BlockPos getGroundPos(BlockPos startPos) {
      for(int i = 0; i < startPos.getY(); ++i) {
         BlockPos newPos = startPos.down(i);
         if (BlockUtils.getBlock(newPos) != Blocks.air) {
            return newPos;
         }
      }

      return null;
   }

   @EventTarget
   public void onUpdate(EventPreMotion event) {
      this.setDisplayName(this.mode.getModeAt(this.mode.getCurrentMode()));
      if (this.mode.isCurrentMode("NoGround") && MoveUtils.isOnGround(0.001D)) {
         event.setY(event.getY() + 1.0E-4D);
      }

      if (!mc.thePlayer.isSpectator() && !mc.thePlayer.capabilities.isFlying && !mc.thePlayer.capabilities.disableDamage && !mc.thePlayer.onGround) {
         if (((Boolean)this.tryspoof.getValue()).booleanValue() && (double)mc.thePlayer.fallDistance > ((Double)falls.getValue()).doubleValue() - 1.0D && (double)mc.thePlayer.fallDistance % ((Double)spoof.getValue()).doubleValue() == 0.0D) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
         }

         if (this.mode.isCurrentMode("Hypixel")) {
            if (this.hypixelMode.isCurrentMode("Spoof")) {
               if ((double)mc.thePlayer.fallDistance > ((Double)falls.getValue()).doubleValue() && isBlockUnder()) {
                  if (((Boolean)this.reset.getValue()).booleanValue()) {
                     this.sync();
                     mc.thePlayer.fallDistance = (float)((double)mc.thePlayer.fallDistance * 0.1D);
                  } else {
                     this.sync();
                  }
               }
            } else if (this.hypixelMode.isCurrentMode("Packet")) {
               if (this.fallDist > (double)mc.thePlayer.fallDistance) {
                  this.fallDist = 0.0D;
               }

               if (mc.thePlayer.motionY < 0.0D && (double)mc.thePlayer.fallDistance > ((Double)falls.getValue()).doubleValue() && isBlockUnder() && !mc.thePlayer.capabilities.allowFlying) {
                  double motionY = mc.thePlayer.motionY;
                  double fallingDist = (double)mc.thePlayer.fallDistance - this.fallDist;
                  double realDist = fallingDist + -((motionY - 0.08D) * 0.9800000190734863D);
                  if (realDist >= 3.0D) {
                     if (!((Boolean)this.reset.getValue()).booleanValue()) {
                        this.sync();
                     } else {
                        for(int i = 0; i < (int)mc.thePlayer.fallDistance; ++i) {
                           this.sync();
                        }

                        mc.thePlayer.fallDistance = (float)((double)mc.thePlayer.fallDistance * 0.1D);
                     }

                     this.fallDist = (double)mc.thePlayer.fallDistance;
                  }
               }
            } else if (this.hypixelMode.isCurrentMode("ASync") && (double)mc.thePlayer.fallDistance > ((Double)falls.getValue()).doubleValue() && isBlockUnder()) {
               this.sync();
            }
         } else if (this.mode.isCurrentMode("Mineplex")) {
            this.setDisplayName("Mineplex");
            if (mc.thePlayer.fallDistance > 2.5F) {
               mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer(true));
               mc.thePlayer.fallDistance = 0.5F;
            }
         } else if (this.mode.isCurrentMode("AAC") && this.aacMode.isCurrentMode("AACv4")) {
            if (!this.inVoid()) {
               if (this.aac4Fakelag) {
                  this.aac4Fakelag = false;
                  if (this.aac4Packets.size() > 0) {
                     for(C03PacketPlayer packet : this.aac4Packets) {
                        mc.thePlayer.sendQueue.addToSendQueue(packet);
                     }

                     this.aac4Packets.clear();
                  }
               }

               return;
            }

            if (mc.thePlayer.onGround && this.aac4Fakelag) {
               this.aac4Fakelag = false;
               if (this.aac4Packets.size() > 0) {
                  for(C03PacketPlayer packet : this.aac4Packets) {
                     mc.thePlayer.sendQueue.addToSendQueue(packet);
                  }

                  this.aac4Packets.clear();
               }

               return;
            }

            if (mc.thePlayer.fallDistance > 3.0F && this.aac4Fakelag) {
               this.aac4PacketModify = true;
               mc.thePlayer.fallDistance = 0.0F;
            }

            if (this.inAir(4.0D, 1.0D)) {
               return;
            }

            if (!this.aac4Fakelag) {
               this.aac4Fakelag = true;
            }
         } else if (this.mode.isCurrentMode("Verus") && (double)mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3.0D) {
            mc.thePlayer.motionY = 0.0D;
            mc.thePlayer.fallDistance = 0.0F;
            mc.thePlayer.motionX *= 0.6D;
            mc.thePlayer.motionZ *= 0.6D;
            this.needSpoof = true;
         }

      }
   }

   @EventTarget
   public void onPost(EventPostMotion e) {
      if (!mc.thePlayer.capabilities.isFlying && !mc.thePlayer.capabilities.disableDamage && mc.thePlayer.motionY < 0.0D) {
         if (this.mode.isCurrentMode("Hypixel") && this.hypixelMode.isCurrentMode("Async") && (double)mc.thePlayer.fallDistance > ((Double)falls.getValue()).doubleValue() && isBlockUnder()) {
            if (((Boolean)this.reset.getValue()).booleanValue()) {
               for(int i = 0; i < (int)mc.thePlayer.fallDistance; ++i) {
                  this.sync();
               }

               mc.thePlayer.fallDistance = (float)((double)mc.thePlayer.fallDistance * 0.1D);
            } else {
               this.sync();
            }
         }

      }
   }

   private void sync() {
      mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer(true));
   }

   private boolean inVoid() {
      if (mc.thePlayer.posY < 0.0D) {
         return false;
      } else {
         for(int off = 0; (double)off < mc.thePlayer.posY + 2.0D; off += 2) {
            AxisAlignedBB bb = new AxisAlignedBB(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.posX, (double)off, mc.thePlayer.posZ);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean inAir(double height, double plus) {
      if (mc.thePlayer.posY < 0.0D) {
         return false;
      } else {
         for(int off = 0; (double)off < height; off = (int)((double)off + plus)) {
            AxisAlignedBB bb = new AxisAlignedBB(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.posX, mc.thePlayer.posY - (double)off, mc.thePlayer.posZ);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
               return true;
            }
         }

         return false;
      }
   }

   public void onEnable() {
      this.fallDist = (double)mc.thePlayer.fallDistance;
      this.aac4Fakelag = false;
      this.aac4PacketModify = false;
      this.aac4Packets.clear();
      super.onEnable();
   }

   public void onDisable() {
      super.onDisable();
   }

   @EventTarget
   public void onPacket(EventPacket e) {
      if (this.mode.isCurrentMode("NoGround")) {
         IC03PacketPlayer packet = (IC03PacketPlayer)e.getPacket();
         packet.setOnGround(false);
      } else if (this.mode.isCurrentMode("OnGround")) {
         IC03PacketPlayer packet = (IC03PacketPlayer)e.getPacket();
         if ((double)mc.thePlayer.fallDistance > 2.0D) {
            packet.setOnGround(true);
         }
      } else if (this.mode.isCurrentMode("AAC") && this.aacMode.isCurrentMode("AACv4") && this.aac4Fakelag) {
         e.setCancelled(true);
         if (this.aac4PacketModify) {
            ((IC03PacketPlayer)e.getPacket()).setOnGround(true);
            this.aac4PacketModify = false;
         }

         this.aac4Packets.add((C03PacketPlayer)e.getPacket());
      } else if (this.mode.isCurrentMode("Hypixel")) {
         C03PacketPlayer look = (C03PacketPlayer)e.getPacket();
         if (((Boolean)this.cancel.getValue()).booleanValue() && look.getRotating() && look.isMoving() && (double)mc.thePlayer.fallDistance > ((Double)falls.getValue()).doubleValue() - ((Double)caneled.getValue()).doubleValue() && isBlockUnder()) {
            Wrapper.sendPacketNoEvent(new C04PacketPlayerPosition(look.getPositionX(), look.getPositionY(), look.getPositionZ(), look.isOnGround()));
            e.setCancelled(true);
         }

         if (this.hypixelMode.isCurrentMode("Edit")) {
            C03PacketPlayer C03 = (C03PacketPlayer)e.getPacket();
            double[] packetPosition = new double[]{C03.getPositionX(), C03.getPositionY(), C03.getPositionZ()};
            double[] myPosition = new double[]{mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ};
            boolean same = packetPosition[0] == myPosition[0] && packetPosition[1] == myPosition[1] && packetPosition[2] == myPosition[2];
            if (!mc.thePlayer.isSpectator() && !mc.thePlayer.capabilities.isFlying && !mc.thePlayer.capabilities.disableDamage && !mc.thePlayer.onGround && mc.thePlayer.fallDistance >= 2.5F && same && !C03.isOnGround()) {
               ((IC03PacketPlayer)C03).setOnGround(true);
            }
         }
      } else if (this.mode.isCurrentMode("Verus")) {
         if (this.needSpoof && !MoveUtils.isOverVoid()) {
            ((IC03PacketPlayer)e.getPacket()).setOnGround(true);
            this.needSpoof = false;
         }
      } else if (this.mode.isCurrentMode("Edit") && mc.thePlayer.fallDistance > 2.0F && mc.thePlayer.fallDistance % 3.0F == 0.0F) {
         ((IC03PacketPlayer)e.getPacket()).setOnGround(true);
      }

   }
}
