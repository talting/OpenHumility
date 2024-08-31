package cn.hanabi.injection.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({ItemBlock.class})
public abstract class MixinItemBlock {
   @Shadow
   @Final
   public Block block;

   public MixinItemBlock() {
      super();
   }

   @Shadow
   public abstract boolean placeBlockAt(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumFacing var5, float var6, float var7, float var8, IBlockState var9);

   @Overwrite
   public boolean onItemUse(ItemStack p_onItemUse_1_, EntityPlayer p_onItemUse_2_, World p_onItemUse_3_, BlockPos p_onItemUse_4_, EnumFacing p_onItemUse_5_, float p_onItemUse_6_, float p_onItemUse_7_, float p_onItemUse_8_) {
      IBlockState iblockstate = p_onItemUse_3_.getBlockState(p_onItemUse_4_);
      Block block = iblockstate.getBlock();
      Minecraft mc = Minecraft.getMinecraft();
      ItemBlock itemBlock = (ItemBlock)mc.thePlayer.inventory.getCurrentItem().getItem();
      if (!block.isReplaceable(p_onItemUse_3_, p_onItemUse_4_)) {
         p_onItemUse_4_ = p_onItemUse_4_.offset(p_onItemUse_5_);
      }

      if (p_onItemUse_1_.stackSize == 0) {
         return false;
      } else if (!p_onItemUse_2_.canPlayerEdit(p_onItemUse_4_, p_onItemUse_5_, p_onItemUse_1_)) {
         return false;
      } else if (p_onItemUse_3_.canBlockBePlaced(itemBlock.block, p_onItemUse_4_, false, p_onItemUse_5_, (Entity)null, p_onItemUse_1_)) {
         int i = itemBlock.getMetadata(p_onItemUse_1_.getMetadata());
         IBlockState iblockstate2 = itemBlock.block.onBlockPlaced(p_onItemUse_3_, p_onItemUse_4_, p_onItemUse_5_, p_onItemUse_6_, p_onItemUse_7_, p_onItemUse_8_, i, p_onItemUse_2_);
         if (itemBlock.placeBlockAt(p_onItemUse_1_, p_onItemUse_2_, p_onItemUse_3_, p_onItemUse_4_, p_onItemUse_5_, p_onItemUse_6_, p_onItemUse_7_, p_onItemUse_8_, iblockstate2)) {
            if (!Minecraft.getMinecraft().isSingleplayer()) {
               Minecraft.getMinecraft().theWorld.playSoundAtPos(p_onItemUse_4_, this.block.stepSound.getPlaceSound(), (this.block.stepSound.getVolume() + 1.0F) / 2.0F, this.block.stepSound.getFrequency() * 0.8F, false);
            } else {
               p_onItemUse_3_.playSoundEffect((double)((float)p_onItemUse_4_.getX() + 0.5F), (double)((float)p_onItemUse_4_.getY() + 0.5F), (double)((float)p_onItemUse_4_.getZ() + 0.5F), this.block.stepSound.getPlaceSound(), (this.block.stepSound.getVolume() + 1.0F) / 2.0F, this.block.stepSound.getFrequency() * 0.8F);
            }

            --p_onItemUse_1_.stackSize;
         }

         return true;
      } else {
         return false;
      }
   }
}
