package cn.hanabi.modules.modules.combat;

import cn.hanabi.Wrapper;
import cn.hanabi.events.EventPacket;
import cn.hanabi.events.EventPreMotion;
import cn.hanabi.events.EventTick;
import cn.hanabi.events.EventUpdate;
import cn.hanabi.injection.interfaces.IEntityPlayerSP;
import cn.hanabi.injection.interfaces.IKeyBinding;
import cn.hanabi.injection.interfaces.IS12PacketEntityVelocity;
import cn.hanabi.injection.interfaces.IS27PacketExplosion;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.modules.ModManager;
import cn.hanabi.utils.PlayerUtil;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.network.play.client.C0BPacketEntityAction.Action;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class Velocity extends Mod {
   public static final Value modes = (new Value("Velocity", "Mode", 0)).LoadValue(new String[]{"Cancel", "Packet", "AAC", "AAC4.4.0", "AAC4", "Intave", "RedeSky", "RedeSkyHVH", "RedeSkyPacket", "AAC5", "C02&&Jump"});
   public Value x = new Value("Velocity", "Vertical", 0.0D, -100.0D, 100.0D, 1.0D);
   public Value y = new Value("Velocity", "Horizontal", 0.0D, 0.0D, 100.0D, 1.0D);
   public Value c02s = new Value("Velocity", "C02Swing", true);
   public Value count = new Value("Velocity", "C02Count", Integer.valueOf(5), Integer.valueOf(0), Integer.valueOf(10));
   private final TimeHelper timer = new TimeHelper();
   private final TimeHelper calcTimer = new TimeHelper();
   private final TimeHelper knockBackTimer = new TimeHelper();
   boolean canVelo = false;
   boolean velocityInput = false;
   boolean attacked = false;
   double reduceXZ = 1.0D;
   double reduceY = -1.0D;

   public Velocity() {
      super("Velocity", Category.COMBAT);
   }

   @EventTarget
   private void onPacket(EventPacket e) {
      if (modes.isCurrentMode("C02&&Jump") && e.getPacket() instanceof S12PacketEntityVelocity) {
         S12PacketEntityVelocity packet = (S12PacketEntityVelocity)e.getPacket();
         if (packet.getEntityID() != mc.thePlayer.getEntityId() || mc.thePlayer.isDead || mc.currentScreen instanceof GuiGameOver || mc.thePlayer.isOnLadder() || packet.getMotionX() == 0 && packet.getMotionZ() == 0) {
            return;
         }

         this.velocityInput = true;
         Entity entity = null;
         this.reduceXZ = 1.0D;
         if (ModManager.getModule("KillAura").isEnabled()) {
            Entity target = KillAura.target;
            if (target != null) {
               entity = KillAura.target;
            }
         }

         if (entity == null) {
            MovingObjectPosition mouse = mc.objectMouseOver;
            if (mouse.typeOfHit == MovingObjectType.ENTITY && mouse.entityHit instanceof EntityLivingBase && PlayerUtil.getRotDistanceToEntity(mouse.entityHit) <= ((Double)KillAura.reach.getValueState()).doubleValue()) {
               entity = mouse.entityHit;
            }
         }

         if (entity != null) {
            if (!((IEntityPlayerSP)mc.thePlayer).getServerSprintState()) {
               mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, Action.START_SPRINTING));
            }

            for(int i = 1; i <= ((Integer)this.count.getValueState()).intValue(); ++i) {
               mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(entity, net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK));
               if (((Boolean)this.c02s.getValueState()).booleanValue()) {
                  mc.thePlayer.swingItem();
               } else {
                  mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
               }
            }

            if (!this.state) {
               mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, Action.STOP_SPRINTING));
            }

            this.attacked = true;
            this.reduceXZ = 0.07776D;
            this.reduceY = (double)packet.getMotionY() / 8000.0D;
         }
      }

      if (modes.isCurrentMode("Cancel")) {
         this.setDisplayName("Cancel");
         if (e.getPacket() instanceof S12PacketEntityVelocity || e.getPacket() instanceof S27PacketExplosion) {
            e.setCancelled(true);
         }
      } else if (modes.isCurrentMode("Packet")) {
         this.setDisplayName("Packet");
         if (e.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity packet = (S12PacketEntityVelocity)e.getPacket();
            if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
               ((IS12PacketEntityVelocity)packet).setX((int)((double)packet.getMotionX() * ((Double)this.x.getValue()).doubleValue() / 100.0D));
               ((IS12PacketEntityVelocity)packet).setY((int)((double)packet.getMotionY() * ((Double)this.y.getValue()).doubleValue() / 100.0D));
               ((IS12PacketEntityVelocity)packet).setZ((int)((double)packet.getMotionZ() * ((Double)this.x.getValue()).doubleValue() / 100.0D));
            }
         }

         if (e.getPacket() instanceof S27PacketExplosion) {
            S27PacketExplosion packet = (S27PacketExplosion)e.getPacket();
            ((IS27PacketExplosion)packet).setX(packet.func_149149_c() * ((Double)this.x.getValue()).floatValue() / 100.0F);
            ((IS27PacketExplosion)packet).setY(packet.func_149144_d() * ((Double)this.y.getValue()).floatValue() / 100.0F);
            ((IS27PacketExplosion)packet).setZ(packet.func_149147_e() * ((Double)this.x.getValue()).floatValue() / 100.0F);
         }
      }

      if (modes.isCurrentMode("RedeSky")) {
         S12PacketEntityVelocity veloPacket = null;
         if (e.getPacket() instanceof S12PacketEntityVelocity) {
            if (((S12PacketEntityVelocity)e.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
               veloPacket = (S12PacketEntityVelocity)e.getPacket();
            }
         } else if (e.getPacket() instanceof S27PacketExplosion) {
            S27PacketExplosion explPacket = (S27PacketExplosion)e.getPacket();
            if (explPacket.func_149149_c() != 0.0F || explPacket.func_149144_d() != 0.0F || explPacket.func_149147_e() != 0.0F) {
               veloPacket = new S12PacketEntityVelocity(mc.thePlayer.getEntityId(), (double)explPacket.func_149149_c(), (double)explPacket.func_149144_d(), (double)explPacket.func_149147_e());
            }
         }

         if (veloPacket == null) {
            return;
         }

         boolean near = false;

         for(Entity entity : mc.theWorld.loadedEntityList) {
            if (!entity.equals(mc.thePlayer) && entity.getDistanceToEntity(mc.thePlayer) < 10.0F) {
               near = true;
               break;
            }
         }

         if (!near) {
            return;
         }

         if (veloPacket.getMotionX() == 0 && veloPacket.getMotionZ() == 0) {
            return;
         }

         e.setCancelled(true);
         if (mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.0D;
            mc.thePlayer.motionX = 0.0D;
            mc.thePlayer.motionZ = 0.0D;
            mc.thePlayer.sendQueue.addToSendQueue(new C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.03D, mc.thePlayer.posZ, false));
            mc.thePlayer.sendQueue.addToSendQueue(new C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.onGround));
         }
      } else if (modes.isCurrentMode("RedeSkyHVH")) {
         if (e.getPacket() instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity)e.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
            Wrapper.getTimer().timerSpeed = 0.3F;
            this.canVelo = true;
            this.timer.reset();
         }
      } else if (modes.isCurrentMode("RedeSkyPacket") && e.getPacket() instanceof S12PacketEntityVelocity) {
         S12PacketEntityVelocity packet = (S12PacketEntityVelocity)e.getPacket();
         if (packet.getMotionY() <= 0 || packet.getEntityID() != mc.thePlayer.getEntityId()) {
            return;
         }

         EntityLivingBase target = null;

         for(Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityLivingBase && !entity.equals(mc.thePlayer)) {
               target = (EntityLivingBase)entity;
               break;
            }
         }

         if (target == null) {
            return;
         }

         mc.thePlayer.motionX = 0.0D;
         mc.thePlayer.motionZ = 0.0D;
         mc.thePlayer.motionY = (double)((float)packet.getMotionY() / 8000.0F) * 1.0D;
         e.setCancelled(true);
         if (this.timer.hasReached(500L)) {
            int count = 20;
            if (!this.timer.hasReached(800L)) {
               count = 5;
            } else if (!this.timer.hasReached(1200L)) {
               count = 8;
            }

            for(int i = 0; i < count; ++i) {
               mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(target, net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK));
               mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
            }

            this.calcTimer.reset();
         }
      }

   }

   @EventTarget
   public void onPre(EventPreMotion e) {
      this.setDisplayName(modes.getModeAt(modes.getCurrentMode()));
      if (modes.isCurrentMode("AAC4.4.0")) {
         this.setDisplayName("AAC4.4.0");
         if (!mc.thePlayer.onGround && mc.thePlayer.hurtResistantTime > 0) {
            mc.thePlayer.motionX *= 0.6D;
            mc.thePlayer.motionZ *= 0.6D;
         }
      } else if (modes.isCurrentMode("AAC4")) {
         this.setDisplayName("AAC4");
         if (mc.thePlayer.hurtTime == 9) {
            mc.thePlayer.motionX *= 0.5D;
            mc.thePlayer.motionZ *= 0.5D;
         }

         if (mc.thePlayer.hurtTime == 8) {
            mc.thePlayer.motionX *= 0.4D;
            mc.thePlayer.motionZ *= 0.4D;
         }

         if (mc.thePlayer.hurtTime == 7) {
            mc.thePlayer.motionX *= 0.7D;
            mc.thePlayer.motionZ *= 0.7D;
         }

         if (mc.thePlayer.hurtTime == 6) {
            mc.thePlayer.motionX *= 0.3D;
            mc.thePlayer.motionZ *= 0.3D;
         }

         if (mc.thePlayer.hurtTime == 5) {
            mc.thePlayer.motionX *= 0.1D;
            mc.thePlayer.motionZ *= 0.1D;
         }
      } else if (modes.isCurrentMode("Intave")) {
         this.setDisplayName("Intave");
         if (mc.thePlayer.hurtTime > 1 && mc.thePlayer.hurtTime < 10) {
            mc.thePlayer.motionX *= 0.75D;
            mc.thePlayer.motionZ *= 0.75D;
            if (mc.thePlayer.hurtTime < 4) {
               if (mc.thePlayer.motionY > 0.0D) {
                  mc.thePlayer.motionY *= 0.9D;
               } else {
                  mc.thePlayer.motionY *= 1.1D;
               }
            }
         }
      } else if (modes.isCurrentMode("AAC")) {
         this.setDisplayName("AAC");
         if (mc.thePlayer.hurtTime != 0) {
            mc.thePlayer.onGround = true;
         }
      } else if (modes.isCurrentMode("RedeSkyHVH")) {
         if (this.timer.isDelayComplete(100L) && this.canVelo) {
            this.canVelo = false;
            Wrapper.getTimer().timerSpeed = 1.0F;
            mc.thePlayer.motionX *= 0.6D;
            mc.thePlayer.motionZ *= 0.6D;
         }
      } else if (modes.isCurrentMode("AAC5") && mc.thePlayer.hurtTime > 8) {
         mc.thePlayer.motionX *= 0.6D;
         mc.thePlayer.motionZ *= 0.6D;
      }

   }

   @EventTarget
   private void onUpdate(EventUpdate e) {
      if (modes.isCurrentMode("C02&&Jump")) {
         if (mc.thePlayer.hurtTime > 7 && mc.thePlayer.onGround) {
            if (mc.thePlayer.isSprinting() && this.velocityInput) {
               ((IKeyBinding)mc.gameSettings.keyBindJump).setPress(Boolean.valueOf(true));
            }
         } else {
            ((IKeyBinding)mc.gameSettings.keyBindJump).setPress(Boolean.valueOf(GameSettings.isKeyDown(mc.gameSettings.keyBindJump)));
         }
      }

   }

   @EventTarget
   private void onTick(EventTick e) {
      if (modes.isCurrentMode("C02&&Jump") && this.velocityInput) {
         if (this.attacked) {
            mc.thePlayer.motionX *= this.reduceXZ;
            mc.thePlayer.motionZ *= this.reduceXZ;
            mc.thePlayer.motionY = this.reduceY;
            this.attacked = false;
         }

         if (mc.thePlayer.hurtTime == 0) {
            this.velocityInput = false;
         }
      }

   }
}
