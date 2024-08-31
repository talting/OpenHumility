package cn.hanabi.injection.mixins;

import cn.hanabi.events.EventMoveInput;
import com.darkmagician6.eventapi.EventManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({MovementInputFromOptions.class})
public class MixinMovementInputFromOptions extends MixinMovementInput {
   @Shadow
   @Final
   private GameSettings gameSettings;

   public MixinMovementInputFromOptions() {
      super();
   }

   @Overwrite
   public void updatePlayerMoveState() {
      this.moveStrafe = 0.0F;
      this.moveForward = 0.0F;
      if (this.gameSettings.keyBindForward.isKeyDown()) {
         ++this.moveForward;
      }

      if (this.gameSettings.keyBindBack.isKeyDown()) {
         --this.moveForward;
      }

      if (this.gameSettings.keyBindLeft.isKeyDown()) {
         ++this.moveStrafe;
      }

      if (this.gameSettings.keyBindRight.isKeyDown()) {
         --this.moveStrafe;
      }

      this.jump = this.gameSettings.keyBindJump.isKeyDown();
      this.sneak = this.gameSettings.keyBindSneak.isKeyDown();
      EventMoveInput event = new EventMoveInput(this.moveStrafe, this.moveForward, this.jump, this.sneak);
      EventManager.call(event);
      this.moveForward = event.moveForward;
      this.moveStrafe = event.moveStrafe;
      this.jump = event.jump;
      this.sneak = event.sneak;
      if (this.sneak) {
         this.moveStrafe = (float)((double)this.moveStrafe * 0.3D);
         this.moveForward = (float)((double)this.moveForward * 0.3D);
      }

   }
}
