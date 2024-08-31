package cn.hanabi.modules.modules.movement.Speed;

import cn.hanabi.events.EventPreMotion;
import cn.hanabi.modules.ModManager;
import cn.hanabi.modules.modules.combat.KillAura;
import cn.hanabi.utils.PlayerUtil;
import cn.hanabi.utils.rotation.Rotation;
import cn.hanabi.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;

public class Speed_PushAbout {
   static Value mode = new Value("Speed", "PushAbout_Mode", 0);
   public Value speedDouble = new Value("Speed", "PASpeed", 0.9D, 0.0D, 2.0D, 0.01D);
   public Value boxSize = new Value("Speed", "PABoxXZSizeExpand", 1.1D, 0.0D, 3.0D, 0.01D);
   public Value boxSize2 = new Value("Speed", "PABoxYSizeExpand", 1.1D, 0.0D, 3.0D, 0.01D);
   public Value noStrafe = new Value("Speed", "noStrafe", false);
   public Value needSeen = new Value("Speed", "OnlySeen", false);
   Minecraft mc = Minecraft.getMinecraft();

   public Speed_PushAbout() {
      super();
      mode.addValue("Mix");
      mode.addValue("BHop");
      mode.addValue("Boost");
   }

   public void onUpdate(EventPreMotion e) {
      if (PlayerUtil.isMoving2() && !ModManager.getModule("Blink").isEnabled()) {
         AxisAlignedBB playerBox = this.mc.thePlayer.getEntityBoundingBox().expand(((Double)this.boxSize.getValueState()).doubleValue(), ((Double)this.boxSize2.getValueState()).doubleValue(), ((Double)this.boxSize.getValueState()).doubleValue());
         int collisions = 0;

         for(Entity entity : this.mc.theWorld.loadedEntityList) {
            if (entity != this.mc.thePlayer && entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand) && playerBox.intersectsWith(entity.getEntityBoundingBox()) && (!((Boolean)this.needSeen.getValueState()).booleanValue() || this.mc.thePlayer.canEntityBeSeen(entity))) {
               ++collisions;
            }
         }

         if (collisions != 0) {
            Rotation rotation = new Rotation(this.mc.thePlayer.rotationYaw, this.mc.thePlayer.rotationPitch);
            if (!((Boolean)this.noStrafe.getValueState()).booleanValue() && ModManager.getModule("KillAura").isEnabled() && KillAura.target != null) {
               rotation = KillAura.serverRotation;
            }

            float yaw = this.getRawDirection(rotation.getYaw());
            double boost = ((Double)this.speedDouble.getValueState()).doubleValue() * (double)collisions * 0.1D;
            if (mode.isCurrentMode("Boost")) {
               this.mc.thePlayer.addVelocity(-Math.sin((double)yaw) * boost, 0.0D, Math.cos((double)yaw) * boost);
            }

            if (mode.isCurrentMode("BHop")) {
               this.mc.thePlayer.jumpMovementFactor = (float)boost;
            }

            if (mode.isCurrentMode("Mix")) {
               if (!this.mc.thePlayer.onGround && !this.mc.thePlayer.isSprinting()) {
                  this.mc.thePlayer.jumpMovementFactor = (float)boost;
               } else {
                  this.mc.thePlayer.addVelocity(-Math.sin((double)yaw) * boost, 0.0D, Math.cos((double)yaw) * boost);
               }
            }

         }
      }
   }

   private double getDirectionRotation(float yaw, float strafe, float forward) {
      return Math.toRadians((double)this.getRawDirection(yaw, strafe, forward));
   }

   public float getRawDirection(float yaw) {
      return (float)this.getDirectionRotation(yaw, this.mc.thePlayer.moveStrafing, this.mc.thePlayer.moveForward);
   }

   private float getRawDirection(float yaw, float strafe, float forward) {
      float rotationYaw = yaw;
      if (forward < 0.0F) {
         rotationYaw = yaw + 180.0F;
      }

      forward = 1.0F;
      if (forward < 0.0F) {
         forward = -0.5F;
      } else if (forward > 0.0F) {
         forward = 0.5F;
      }

      if (strafe > 0.0F) {
         rotationYaw -= 90.0F * forward;
      }

      if (strafe < 0.0F) {
         rotationYaw += 90.0F * forward;
      }

      return rotationYaw;
   }
}
