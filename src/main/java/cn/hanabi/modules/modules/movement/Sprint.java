package cn.hanabi.modules.modules.movement;

import cn.hanabi.events.EventJump;
import cn.hanabi.events.EventMoveInput;
import cn.hanabi.events.EventStrafe;
import cn.hanabi.events.EventUpdate;
import cn.hanabi.injection.interfaces.IKeyBinding;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.modules.ModManager;
import cn.hanabi.modules.modules.combat.KillAura;
import cn.hanabi.utils.PlayerUtil;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.util.MathHelper;

public class Sprint extends Mod {
   public static boolean isSprinting;
   public Value ommi = new Value("Sprint", "Omni", false);
   public Value allowOnly = new Value("Sprint", "OnlyCanSprint", false);
   public Value movefix = new Value("Sprint", "RotMoveFix", false);
   public Value sprintfix = new Value("Sprint", "SprintFix(in RotMove)", false);
   private float fixedYaw = 0.0F;
   private boolean fixed = false;
   private static boolean tickForceForward = false;

   public Sprint() {
      super("Sprint", Category.MOVEMENT);
      this.setState(true);
   }

   @EventTarget
   public void onUpdate(EventUpdate event) {
      if (!ModManager.getModule("Scaffold").getState()) {
         label45: {
            boolean canSprint = (float)mc.thePlayer.getFoodStats().getFoodLevel() > 6.0F || mc.thePlayer.capabilities.allowFlying || ((Boolean)this.allowOnly.getValueState()).booleanValue();
            if (((Boolean)this.ommi.getValue()).booleanValue()) {
               if (PlayerUtil.MovementInput()) {
                  break label45;
               }
            } else if (((IKeyBinding)mc.gameSettings.keyBindForward).getPress() && canSprint) {
               break label45;
            }

            isSprinting = false;
            return;
         }

         isSprinting = true;
         mc.thePlayer.setSprinting(true);
      }
   }

   public void onDisable() {
      isSprinting = false;
      mc.thePlayer.setSprinting(false);
      super.onDisable();
   }

   @EventTarget
   public void onStrafe(EventMoveInput event) {
      this.fixed = false;
      if (((Boolean)this.movefix.getValueState()).booleanValue() && ModManager.getModule("KillAura").isEnabled() && KillAura.target != null) {
         float forward = event.moveForward;
         float strafe = event.moveStrafe;
         float yaw = this.fixedYaw = KillAura.serverRotation.getYaw();
         this.fixed = true;
         double angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(direction(mc.thePlayer.rotationYaw, (double)forward, (double)strafe)));
         if (forward != 0.0F || strafe != 0.0F) {
            float closestForward = 0.0F;
            float closestStrafe = 0.0F;
            float closestDifference = Float.MAX_VALUE;

            for(float predictedForward = -1.0F; predictedForward <= 1.0F; ++predictedForward) {
               if (tickForceForward) {
                  predictedForward = 1.0F;
                  tickForceForward = false;
               }

               for(float predictedStrafe = -1.0F; predictedStrafe <= 1.0F; ++predictedStrafe) {
                  if (predictedStrafe != 0.0F || predictedForward != 0.0F) {
                     double predictedAngle = MathHelper.wrapAngleTo180_double(Math.toDegrees(direction(yaw, (double)predictedForward, (double)predictedStrafe)));
                     double difference = Math.abs(angle - predictedAngle);
                     if (difference < (double)closestDifference) {
                        closestDifference = (float)difference;
                        closestForward = predictedForward;
                        closestStrafe = predictedStrafe;
                     }
                  }
               }
            }

            event.moveForward = closestForward;
            event.moveStrafe = closestStrafe;
            if (((Boolean)this.sprintfix.getValueState()).booleanValue()) {
               mc.thePlayer.setSprinting(closestForward > 0.0F);
            }

         }
      }
   }

   @EventTarget
   public void onStrafe(EventStrafe event) {
      if (this.fixed) {
         event.yaw = this.fixedYaw;
      }

   }

   @EventTarget
   public void onJump(EventJump event) {
      if (this.fixed) {
         event.yaw = this.fixedYaw;
      }

   }

   private static double direction(float rotationYaw, double moveForward, double moveStrafing) {
      if (moveForward < 0.0D) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (moveForward < 0.0D) {
         forward = -0.5F;
      } else if (moveForward > 0.0D) {
         forward = 0.5F;
      }

      if (moveStrafing > 0.0D) {
         rotationYaw -= 90.0F * forward;
      }

      if (moveStrafing < 0.0D) {
         rotationYaw += 90.0F * forward;
      }

      return Math.toRadians((double)rotationYaw);
   }
}
