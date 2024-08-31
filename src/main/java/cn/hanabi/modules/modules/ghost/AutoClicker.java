package cn.hanabi.modules.modules.ghost;

import cn.hanabi.events.EventMouse;
import cn.hanabi.events.EventUpdate;
import cn.hanabi.injection.interfaces.IEntityLivingBase;
import cn.hanabi.injection.interfaces.IKeyBinding;
import cn.hanabi.injection.interfaces.IMinecraft;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class AutoClicker extends Mod {
   public static boolean isClicking = false;
   private final TimeHelper left = new TimeHelper();
   private final TimeHelper right = new TimeHelper();
   public boolean isDone = true;
   public int timer;
   public Value maxCps = new Value("AutoClicker", "Max CPS", 12.0D, 1.0D, 20.0D, 1.0D);
   public Value minCps = new Value("AutoClicker", "MinC PS", 8.0D, 1.0D, 20.0D, 1.0D);
   public Value blockHit = new Value("AutoClicker", "Block Hit", false);
   public Value jitter = new Value("AutoClicker", "Jitter", false);
   Random random = new Random();

   public AutoClicker() {
      super("AutoClicker", Category.GHOST);
   }

   public void onEnable() {
      this.isDone = true;
      this.timer = 0;
      super.onEnable();
   }

   public void onDisable() {
      this.isDone = true;
      super.onDisable();
   }

   private long getDelay() {
      return (long)((double)((Double)this.maxCps.getValueState()).intValue() + this.random.nextDouble() * (double)(((Double)this.minCps.getValueState()).intValue() - ((Double)this.maxCps.getValueState()).intValue()));
   }

   @EventTarget
   public void onUpdate(EventUpdate event) {
      if (mc.thePlayer != null) {
         isClicking = false;
         if (((Double)this.minCps.getValueState()).intValue() > ((Double)this.maxCps.getValueState()).intValue()) {
            this.minCps.setValueState(this.maxCps.getValueState());
         }

         if (((IKeyBinding)mc.gameSettings.keyBindAttack).getPress() && mc.thePlayer.isUsingItem()) {
            this.swingItemNoPacket();
         }

         if (((IKeyBinding)mc.gameSettings.keyBindAttack).getPress() && !mc.thePlayer.isUsingItem() && this.left.isDelayComplete(Double.valueOf(1000.0D / (double)this.getDelay()))) {
            if (((Boolean)this.jitter.getValueState()).booleanValue()) {
               this.jitter(this.random);
            }

            ((IMinecraft)mc).setClickCounter(0);
            ((IMinecraft)mc).runCrinkMouse();
            isClicking = true;
            this.left.reset();
         }
      }

      if (!this.isDone) {
         switch(this.timer) {
         case 0:
            ((IKeyBinding)mc.gameSettings.keyBindUseItem).setPress(Boolean.valueOf(false));
            break;
         case 1:
         case 2:
            ((IKeyBinding)mc.gameSettings.keyBindUseItem).setPress(Boolean.valueOf(true));
            break;
         case 3:
            ((IKeyBinding)mc.gameSettings.keyBindUseItem).setPress(Boolean.valueOf(false));
            this.isDone = true;
            this.timer = -1;
         }

         ++this.timer;
      }

   }

   public void swingItemNoPacket() {
      if (!mc.thePlayer.isSwingInProgress || mc.thePlayer.swingProgressInt >= ((IEntityLivingBase)mc.thePlayer).runGetArmSwingAnimationEnd() / 2 || mc.thePlayer.swingProgressInt < 0) {
         mc.thePlayer.swingProgressInt = -1;
         mc.thePlayer.isSwingInProgress = true;
      }

   }

   @EventTarget
   public void onCrink(EventMouse event) {
      ItemStack stack = mc.thePlayer.getCurrentEquippedItem();
      if (stack != null && ((Boolean)this.blockHit.getValueState()).booleanValue() && stack.getItem() instanceof ItemSword && !mc.thePlayer.isUsingItem()) {
         if (!this.isDone || this.timer > 0) {
            return;
         }

         this.isDone = false;
      }

   }

   public void jitter(Random rand) {
      if (rand.nextBoolean()) {
         if (rand.nextBoolean()) {
            mc.thePlayer.rotationPitch -= (float)((double)rand.nextFloat() * 0.6D);
         } else {
            mc.thePlayer.rotationPitch += (float)((double)rand.nextFloat() * 0.6D);
         }
      } else if (rand.nextBoolean()) {
         mc.thePlayer.rotationYaw -= (float)((double)rand.nextFloat() * 0.6D);
      } else {
         mc.thePlayer.rotationYaw += (float)((double)rand.nextFloat() * 0.6D);
      }

   }
}
