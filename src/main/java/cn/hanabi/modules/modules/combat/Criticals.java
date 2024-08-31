package cn.hanabi.modules.modules.combat;

import cn.hanabi.events.EventAttack;
import cn.hanabi.events.EventPacket;
import cn.hanabi.events.EventPreMotion;
import cn.hanabi.events.EventStep;
import cn.hanabi.events.EventUpdate;
import cn.hanabi.events.EventWorldChange;
import cn.hanabi.injection.interfaces.IC03PacketPlayer;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.modules.ModManager;
import cn.hanabi.utils.BlockUtils;
import cn.hanabi.utils.MoveUtils;
import cn.hanabi.utils.PlayerUtil;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import com.darkmagician6.eventapi.types.EventType;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.util.BlockPos;

public class Criticals extends Mod {
   public static boolean isReadyToCritical = false;
   public static Value modes = (new Value("Criticals", "Mode", 0)).LoadValue(new String[]{"Packet", "AACv4", "NoGround", "Jump"});
   public static Value pmode = (new Value("Criticals", "Packet Mode", 0)).LoadValue(new String[]{"Minus", "Drop", "Offest", "Old", "Hover", "Rise", "Abuse1", "Abuse2"});
   public static Value hurttime = new Value("Criticals", "Hurt Time", 15.0D, 1.0D, 20.0D, 1.0D);
   public static Value delay = new Value("Criticals", "Delay", 100.0D, 50.0D, 800.0D, 10.0D);
   public static Value steptick = new Value("Criticals", "Step Timer", 100.0D, 50.0D, 500.0D, 10.0D);
   public static Value editv = new Value("Criticals", "Edit Value", 0.0625D, -0.0725D, 0.0725D, 0.0025D);
   public static Random random = new Random();
   public static Value noti = new Value("Criticals", "Notification", false);
   public static Value prefall = new Value("Criticals", "Pre-FDistance", false);
   public static Value keep = new Value("Criticals", "Keep Packet", false);
   static TimeHelper stepTimer = new TimeHelper();
   static TimeHelper critTimer = new TimeHelper();
   static double[] y1 = new double[]{0.104080378093037D, 0.105454222033912D, 0.102888018147468D, 0.099634532004642D};

   public Criticals() {
      super("Criticals", Category.COMBAT);
   }

   public static void doCrit() {
      boolean toggled = ModManager.getModule("Criticals").isEnabled();
      boolean notoggled = !ModManager.getModule("Speed").isEnabled() && !ModManager.getModule("Fly").isEnabled() && !ModManager.getModule("Scaffold").isEnabled();
      double[] packet = new double[]{0.051D * y1[(new Random()).nextInt(y1.length)] + ThreadLocalRandom.current().nextDouble(0.005D), (!MoveUtils.isMoving() ? 0.001D : ThreadLocalRandom.current().nextDouble(3.0E-4D, 5.0E-4D)) + ThreadLocalRandom.current().nextDouble(1.0E-4D), 0.031D * y1[(new Random()).nextInt(y1.length)] + ThreadLocalRandom.current().nextDouble(0.001D), (!MoveUtils.isMoving() ? 0.008D : ThreadLocalRandom.current().nextDouble(1.0E-4D, 7.0E-4D)) + ThreadLocalRandom.current().nextDouble(1.0E-4D)};
      double[] hover = new double[]{-0.0091165721D * y1[(new Random()).nextInt(y1.length)] * 10.0D, 0.0679999D + (double)mc.thePlayer.ticksExisted % 0.0215D, 0.0176063469198817D * y1[(new Random()).nextInt(y1.length)] * 10.0D};
      double[] edit = new double[]{0.0D, 0.075D + ThreadLocalRandom.current().nextDouble(0.008D) * ((new Random()).nextBoolean() ? 0.98D : 0.99D) + (double)mc.thePlayer.ticksExisted % 0.0215D * 0.94D, ((new Random()).nextBoolean() ? 0.01063469198817D : 0.013999999D) * ((new Random()).nextBoolean() ? 0.98D : 0.99D) * y1[(new Random()).nextInt(y1.length)] * 10.0D};
      double[] test = new double[]{0.06D + ThreadLocalRandom.current().nextDouble(1.0E-4D), (0.06D + ThreadLocalRandom.current().nextDouble(1.0E-4D)) / 2.0D, (0.06D + ThreadLocalRandom.current().nextDouble(1.0E-4D)) / 4.0D, ThreadLocalRandom.current().nextDouble(1.5E-4D, 1.63166800276E-4D)};
      double[] old = new double[]{0.0D, -0.0075D, (MoveUtils.isMoving() ? 4.5E-4D : 0.0055D) + ThreadLocalRandom.current().nextDouble(1.0E-4D)};
      double[] offest = new double[]{-ThreadLocalRandom.current().nextDouble(1.5E-4D, 1.63166800276E-4D), 0.011D * y1[(new Random()).nextInt(y1.length)] + ThreadLocalRandom.current().nextDouble(0.005D), 0.001D + ThreadLocalRandom.current().nextDouble(1.0E-4D), 1.0E-4D, (!MoveUtils.isMoving() ? 0.002D : ThreadLocalRandom.current().nextDouble(5.0E-4D, 8.0E-4D)) + ThreadLocalRandom.current().nextDouble(1.0E-5D)};
      double[] morgan = new double[]{0.00124D + ThreadLocalRandom.current().nextDouble(1.0E-4D, 9.0E-4D), MoveUtils.isMoving() ? 8.5E-4D : 0.005D + ThreadLocalRandom.current().nextDouble(1.0E-4D)};
      double[] morganfork = new double[]{0.012D + ThreadLocalRandom.current().nextDouble(1.0E-4D, 9.0E-4D), (MoveUtils.isMoving() ? 8.5E-4D : 0.005D) + ThreadLocalRandom.current().nextDouble(0.001D)};
      if (((Boolean)keep.getValue()).booleanValue()) {
         double i1 = 0.0319D * y1[(new Random()).nextInt(y1.length)] + ThreadLocalRandom.current().nextDouble(0.005D);
         double i2 = (!MoveUtils.isMoving() ? 0.008D : ThreadLocalRandom.current().nextDouble(1.0E-4D, 7.0E-4D)) + ThreadLocalRandom.current().nextDouble(1.0E-4D);
         packet = new double[]{i1, i2, i1, i2};
      }

      if (toggled) {
         isReadyToCritical = !isReadyToCritical && (double)KillAura.target.hurtResistantTime <= ((Double)hurttime.getValue()).doubleValue() && mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround && !BlockUtils.isInLiquid() && notoggled;
         if (!critTimer.isDelayComplete((Double)delay.getValueState()) || !stepTimer.isDelayComplete((Double)steptick.getValue()) && isReadyToCritical) {
            isReadyToCritical = false;
         }

         if (isReadyToCritical) {
            EntityPlayerSP p = mc.thePlayer;
            double[] i = null;
            if (modes.isCurrentMode("Packet")) {
               String var18 = pmode.getModeAt(pmode.getCurrentMode());
               byte var13 = -1;
               switch(var18.hashCode()) {
               case -1935925801:
                  if (var18.equals("Offest")) {
                     var13 = 5;
                  }
                  break;
               case 79367:
                  if (var18.equals("Old")) {
                     var13 = 4;
                  }
                  break;
               case 2138895:
                  if (var18.equals("Drop")) {
                     var13 = 3;
                  }
                  break;
               case 2547433:
                  if (var18.equals("Rise")) {
                     var13 = 2;
                  }
                  break;
               case 69916956:
                  if (var18.equals("Hover")) {
                     var13 = 1;
                  }
                  break;
               case 74348624:
                  if (var18.equals("Minus")) {
                     var13 = 0;
                  }
                  break;
               case 1954999115:
                  if (var18.equals("Abuse1")) {
                     var13 = 6;
                  }
                  break;
               case 1954999116:
                  if (var18.equals("Abuse2")) {
                     var13 = 7;
                  }
               }

               switch(var13) {
               case 0:
                  i = packet;
                  break;
               case 1:
                  i = hover;
                  break;
               case 2:
                  i = edit;
                  break;
               case 3:
                  i = test;
                  break;
               case 4:
                  i = old;
                  break;
               case 5:
                  i = offest;
                  break;
               case 6:
                  i = morgan;
                  break;
               case 7:
                  i = morganfork;
               }
            }

            if (i != null) {
               if (MoveUtils.isOnGround(-1.0D)) {
                  mc.thePlayer.jump();
               } else {
                  if (((Boolean)prefall.getValue()).booleanValue()) {
                     mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C04PacketPlayerPosition(p.posX, p.posY + ((Double)editv.getValue()).doubleValue(), p.posZ, true));
                  }

                  for(double offset : i) {
                     mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C04PacketPlayerPosition(p.posX, p.posY + offset, p.posZ, false));
                  }
               }
            }

            if (((Boolean)noti.getValue()).booleanValue()) {
               PlayerUtil.tellPlayer("Crit: " + randomNumber(-9999, 9999));
            }

            critTimer.reset();
         }
      }

   }

   private static int randomNumber(int max, int min) {
      return (int)(Math.random() * (double)(max - min)) + min;
   }

   @EventTarget
   public void onStep(EventStep e) {
      isReadyToCritical = false;
      if (e.getEventType() == EventType.POST) {
         stepTimer.reset();
      }

   }

   @EventTarget
   public void onChangeWorld(EventWorldChange e) {
      stepTimer.reset();
   }

   @EventTarget
   public void onPre(EventPreMotion e) {
      if (modes.isCurrentMode("Packet") && BlockUtils.isReallyOnGround() && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0D, mc.thePlayer.posZ)).getBlock().isFullBlock() && !BlockUtils.isInLiquid() && ModManager.getModule("Speed").isEnabled() && ModManager.getModule("KillAura").isEnabled()) {
         EntityLivingBase entity = KillAura.target;
         int ht = entity.hurtResistantTime;
         switch(ht) {
         case 17:
         case 19:
            e.setOnGround(false);
            e.setY(mc.thePlayer.posY + ThreadLocalRandom.current().nextDouble(1.5E-4D, 1.63166800276E-4D));
            break;
         case 18:
            e.setOnGround(false);
            e.setY(mc.thePlayer.posY + ThreadLocalRandom.current().nextDouble(0.0019D, 0.0091921599284565D));
            break;
         case 20:
            e.setOnGround(false);
            e.setY(mc.thePlayer.posY + ThreadLocalRandom.current().nextDouble(0.0099D, 0.011921599284565D));
         }
      }

   }

   public void onEnable() {
   }

   @EventTarget
   public void onUpdate(EventUpdate e) {
      this.setDisplayName(modes.getModeAt(modes.getCurrentMode()));
   }

   @EventTarget
   public void onPacket(EventPacket event) {
      Packet packet = event.getPacket();
      if (packet instanceof C03PacketPlayer) {
         IC03PacketPlayer packet1 = (IC03PacketPlayer)packet;
         if (modes.isCurrentMode("NoGround")) {
            packet1.setOnGround(false);
         }
      }

   }

   @EventTarget
   public void onAttack(EventAttack event) {
      if (mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava() && event.entity instanceof EntityLivingBase && mc.thePlayer.ridingEntity == null) {
         double x = mc.thePlayer.posX;
         double y = mc.thePlayer.posY;
         double z = mc.thePlayer.posZ;
         if (modes.isCurrentMode("Jump")) {
            mc.thePlayer.jump();
         }

         if (modes.isCurrentMode("AACv4")) {
            mc.getNetHandler().addToSendQueue(new C04PacketPlayerPosition(x, y + 3.6E-15D, z, false));
            mc.getNetHandler().addToSendQueue(new C04PacketPlayerPosition(x, y, z, false));
         }

      }
   }
}
