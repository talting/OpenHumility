package cn.hanabi.modules.modules.player;

import cn.hanabi.events.EventTick;
import cn.hanabi.injection.interfaces.IItemSword;
import cn.hanabi.injection.interfaces.IItemTools;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.InvUtils;
import cn.hanabi.utils.MoveUtils;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import java.util.Iterator;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class InvCleaner extends Mod {
   public static Value keepTools = new Value("InvCleaner", "Tools", false);
   public static Value keepArmor = new Value("InvCleaner", "Armor", false);
   public static Value keepBow = new Value("InvCleaner", "Bow", false);
   public static Value keepBucket = new Value("InvCleaner", "Bucket", false);
   public static Value keepArrow = new Value("InvCleaner", "Arrow", false);
   public static Value inInv = new Value("InvCleaner", "OnlyInv", false);
   private final Value noMove = new Value("InvCleaner", "No Move", false);
   private final Value sort = new Value("InvCleaner", "Sort", false);
   public static TimeHelper delayTimer = new TimeHelper();
   private final Value delay = new Value("InvCleaner", "Delay", 80.0D, 0.0D, 1000.0D, 10.0D);
   public Value toggle = new Value("InvCleaner", "Auto Toggle", false);
   private double handitemAttackValue;
   private int currentSlot = 9;

   public InvCleaner() {
      super("InvCleaner", Category.PLAYER);
   }

   public static boolean isShit(int slot) {
      ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();
      if (itemStack == null) {
         return false;
      } else if (itemStack.getItem() == Items.stick) {
         return true;
      } else if (itemStack.getItem() == Items.egg) {
         return true;
      } else if (itemStack.getItem() == Items.bone) {
         return true;
      } else if (itemStack.getItem() == Items.bowl) {
         return true;
      } else if (itemStack.getItem() == Items.glass_bottle) {
         return true;
      } else if (itemStack.getItem() == Items.string) {
         return true;
      } else if (itemStack.getItem() == Items.flint && getItemAmount(Items.flint) > 1) {
         return true;
      } else if (itemStack.getItem() == Items.compass && getItemAmount(Items.compass) > 1) {
         return true;
      } else if (itemStack.getItem() == Items.feather) {
         return true;
      } else if (itemStack.getItem() == Items.fishing_rod) {
         return true;
      } else if (itemStack.getItem() == Items.bucket && !((Boolean)keepBucket.getValue()).booleanValue()) {
         return true;
      } else if (itemStack.getItem() == Items.lava_bucket && !((Boolean)keepBucket.getValue()).booleanValue()) {
         return true;
      } else if (itemStack.getItem() == Items.water_bucket && !((Boolean)keepBucket.getValue()).booleanValue()) {
         return true;
      } else if (itemStack.getItem() == Items.milk_bucket && !((Boolean)keepBucket.getValue()).booleanValue()) {
         return true;
      } else if (itemStack.getItem() == Items.arrow && !((Boolean)keepArrow.getValue()).booleanValue()) {
         return true;
      } else if (itemStack.getItem() == Items.snowball) {
         return true;
      } else if (itemStack.getItem() == Items.fish) {
         return true;
      } else if (itemStack.getItem() == Items.experience_bottle) {
         return true;
      } else if (!(itemStack.getItem() instanceof ItemTool) || ((Boolean)keepTools.getValue()).booleanValue() && isBestTool(itemStack)) {
         if (!(itemStack.getItem() instanceof ItemSword) || ((Boolean)keepTools.getValue()).booleanValue() && isBestSword(itemStack)) {
            if (!(itemStack.getItem() instanceof ItemArmor) || ((Boolean)keepArmor.getValue()).booleanValue() && isBestArmor(itemStack)) {
               if (!(itemStack.getItem() instanceof ItemBow) || ((Boolean)keepBow.getValue()).booleanValue() && isBestBow(itemStack)) {
                  return itemStack.getItem().getUnlocalizedName().contains("potion") ? isBadPotion(itemStack) : false;
               } else {
                  return true;
               }
            } else {
               return true;
            }
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   private static int getItemAmount(Item shit) {
      int result = 0;

      for(Slot item : mc.thePlayer.inventoryContainer.inventorySlots) {
         if (item.getHasStack() && item.getStack().getItem() == shit) {
            ++result;
         }
      }

      return result;
   }

   private static boolean isBestTool(ItemStack input) {
      Iterator var1 = InvUtils.getAllInventoryContent().iterator();

      while(true) {
         if (!var1.hasNext()) {
            return true;
         }

         ItemStack itemStack = (ItemStack)var1.next();
         if (itemStack != null && itemStack.getItem() instanceof ItemTool && itemStack != input && (!(itemStack.getItem() instanceof ItemPickaxe) || input.getItem() instanceof ItemPickaxe) && (!(itemStack.getItem() instanceof ItemAxe) || input.getItem() instanceof ItemAxe) && (!(itemStack.getItem() instanceof ItemSpade) || input.getItem() instanceof ItemSpade) && getToolEffencly(itemStack) >= getToolEffencly(input)) {
            break;
         }
      }

      return false;
   }

   private static boolean isBestSword(ItemStack input) {
      for(ItemStack itemStack : InvUtils.getAllInventoryContent()) {
         if (itemStack != null && itemStack.getItem() instanceof ItemSword && itemStack != input && getSwordAttackDamage(itemStack) >= getSwordAttackDamage(input)) {
            return false;
         }
      }

      return true;
   }

   private static boolean isBestBow(ItemStack input) {
      for(ItemStack itemStack : InvUtils.getAllInventoryContent()) {
         if (itemStack != null && itemStack.getItem() instanceof ItemBow && itemStack != input && getBowAttackDamage(itemStack) >= getBowAttackDamage(input)) {
            return false;
         }
      }

      return true;
   }

   private static boolean isBestArmor(ItemStack input) {
      for(ItemStack itemStack : InvUtils.getAllInventoryContent()) {
         if (itemStack != null && itemStack.getItem() instanceof ItemArmor && itemStack != input && ((ItemArmor)itemStack.getItem()).armorType == ((ItemArmor)input.getItem()).armorType && InvUtils.getArmorScore(itemStack) >= InvUtils.getArmorScore(input)) {
            return false;
         }
      }

      for(ItemStack itemStack : mc.thePlayer.inventory.armorInventory) {
         if (itemStack != null && itemStack.getItem() instanceof ItemArmor && itemStack != input && ((ItemArmor)itemStack.getItem()).armorType == ((ItemArmor)input.getItem()).armorType && InvUtils.getArmorScore(itemStack) >= InvUtils.getArmorScore(input)) {
            return false;
         }
      }

      return true;
   }

   private static boolean isBadPotion(ItemStack stack) {
      if (stack != null && stack.getItem() instanceof ItemPotion) {
         ItemPotion potion = (ItemPotion)stack.getItem();

         for(PotionEffect o : potion.getEffects(stack)) {
            if (o.getPotionID() == Potion.poison.getId() || o.getPotionID() == Potion.moveSlowdown.getId() || o.getPotionID() == Potion.harm.getId()) {
               return true;
            }
         }
      }

      return false;
   }

   private static double getSwordAttackDamage(ItemStack itemStack) {
      if (itemStack != null && itemStack.getItem() instanceof ItemSword) {
         ItemSword sword = (ItemSword)itemStack.getItem();
         return (double)((IItemSword)sword).getAttackDamage() + (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) * 1.25D + (double)(EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, itemStack) * 1);
      } else {
         return 0.0D;
      }
   }

   private static double getBowAttackDamage(ItemStack itemStack) {
      return itemStack != null && itemStack.getItem() instanceof ItemBow ? (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, itemStack) + (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, itemStack) * 0.1D + (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, itemStack) * 0.1D : 0.0D;
   }

   private static double getToolEffencly(ItemStack itemStack) {
      if (itemStack != null && itemStack.getItem() instanceof ItemTool) {
         IItemTools sword = (IItemTools)itemStack.getItem();
         return (double)((float)EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack) + sword.getEfficiencyOnProperMaterial());
      } else {
         return 0.0D;
      }
   }

   public void onEnable() {
      super.onEnable();
      this.currentSlot = 9;
      this.handitemAttackValue = getSwordAttackDamage(mc.thePlayer.getHeldItem());
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (this.isEnabled() && !(mc.currentScreen instanceof GuiChest)) {
         if (!((Boolean)this.noMove.getValue()).booleanValue() || !MoveUtils.isMoving()) {
            if (this.currentSlot >= 45) {
               this.currentSlot = 9;
               if (mc.thePlayer.ticksExisted % 40 == 0 || ((Boolean)this.toggle.getValueState()).booleanValue()) {
                  InvUtils.getBestAxe();
                  InvUtils.getBestPickaxe();
                  InvUtils.getBestShovel();
               }

               if (((Boolean)this.toggle.getValueState()).booleanValue()) {
                  this.set(false);
                  return;
               }
            }

            if (!((Boolean)inInv.getValueState()).booleanValue() || mc.currentScreen instanceof GuiInventory) {
               this.handitemAttackValue = getSwordAttackDamage(mc.thePlayer.getHeldItem());
               ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(this.currentSlot).getStack();
               if (delayTimer.isDelayComplete((Double)this.delay.getValue())) {
                  if (isShit(this.currentSlot) && getSwordAttackDamage(itemStack) <= this.handitemAttackValue && itemStack != mc.thePlayer.getHeldItem()) {
                     mc.playerController.windowClick(0, this.currentSlot, 1, 4, mc.thePlayer);
                     delayTimer.reset();
                  }

                  ++this.currentSlot;
               }
            }

         }
      }
   }
}
