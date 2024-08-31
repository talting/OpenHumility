package cn.hanabi.modules.modules.movement.LongJump;

import cn.hanabi.events.EventMove;
import cn.hanabi.events.EventPreMotion;
import cn.hanabi.modules.ModManager;
import cn.hanabi.utils.MoveUtils;
import cn.hanabi.utils.PlayerUtil;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.utils.random.Random;
import cn.hanabi.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.potion.Potion;

public class LongJump_DMG {
   final Minecraft mc = Minecraft.getMinecraft();
   private int stage;
   private double speed;
   private double verticalSpeed;
   static TimeHelper timer = new TimeHelper();
   private static final Value flag = new Value("LongJump", "Flags", false);
   private static final Value uhc = new Value("LongJump", "Extra DMG", false);
   public static final Value height = new Value("LongJump", "Jump Height", 1.0D, 0.5D, 1.4D, 0.1D);

   public LongJump_DMG() {
      super();
   }

   public void onPre(EventPreMotion e) {
   }

   public void onMove(EventMove e) {
      if (MoveUtils.isOnGround(0.01D) || this.stage > 0) {
         switch(this.stage) {
         case 0:
            this.mc.thePlayer.setPosition(this.mc.thePlayer.posX, this.mc.thePlayer.posY + 0.004D * Math.random(), this.mc.thePlayer.posZ);
            if (((Boolean)flag.getValue()).booleanValue()) {
               damage2();
            } else {
               fallDistDamage();
            }

            this.verticalSpeed = PlayerUtil.getBaseJumpHeight() * ((Double)height.getValue()).doubleValue();
            this.speed = MoveUtils.getBaseMoveSpeed(0.2877D, 0.2D) * 2.14D;
            break;
         case 1:
            this.speed *= 0.77D;
            break;
         default:
            this.speed *= 0.98D;
         }

         e.setY(this.verticalSpeed);
         if (this.stage > 8) {
            this.verticalSpeed -= 0.032D;
         } else {
            this.verticalSpeed *= 0.87D;
         }

         ++this.stage;
         if (MoveUtils.isOnGround(0.01D) && this.stage > 4) {
            ModManager.getModule("LongJump").set(false);
         }

         MoveUtils.setMotion(e, Math.max(MoveUtils.getBaseMoveSpeed(0.2877D, 0.1D), this.speed));
      }

   }

   public void onEnable() {
      this.stage = 0;
   }

   public void onDisable() {
      this.stage = 0;
   }

   public static void fallDistDamage() {
      double randomOffset = Math.random() * 3.000000142492354E-4D;
      double jumpHeight = 0.0625D - randomOffset;
      int packets = (int)(getMinFallDist() / (jumpHeight - randomOffset) + 1.0D);

      for(int i = 0; i < packets; ++i) {
         Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C04PacketPlayerPosition(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY + jumpHeight, Minecraft.getMinecraft().thePlayer.posZ, false));
         Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C04PacketPlayerPosition(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY + randomOffset, Minecraft.getMinecraft().thePlayer.posZ, false));
      }

      Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C04PacketPlayerPosition(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ, true));
      timer.reset();
   }

   public static void damage2() {
      double packets = Math.ceil(getMinFallDist() / 0.0625D);
      double random = Random.nextDouble(0.00101001D, 0.00607009D);

      for(int i = 0; (double)i < packets; ++i) {
         Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacket(new C04PacketPlayerPosition(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY + 0.0625D + random, Minecraft.getMinecraft().thePlayer.posZ, false));
         Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacket(new C04PacketPlayerPosition(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY + random, Minecraft.getMinecraft().thePlayer.posZ, false));
      }

      Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer(true));
      timer.reset();
   }

   public static double getMotionY() {
      double mY = 0.41999998688697815D;
      if (Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.jump)) {
         mY += (double)(Minecraft.getMinecraft().thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1D;
      }

      return mY;
   }

   public static double getMinFallDist() {
      double baseFallDist = ((Boolean)uhc.getValue()).booleanValue() ? 4.0D : 3.0D;
      if (Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.jump)) {
         baseFallDist += (double)((float)Minecraft.getMinecraft().thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1.0F);
      }

      return baseFallDist;
   }
}
