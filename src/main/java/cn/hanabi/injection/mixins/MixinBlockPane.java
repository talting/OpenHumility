package cn.hanabi.injection.mixins;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({BlockPane.class})
public abstract class MixinBlockPane extends MixinBlock {
   public MixinBlockPane() {
      super();
   }

   @Shadow
   @Final
   public abstract boolean canPaneConnectToBlock(Block var1);

   @Shadow
   public abstract boolean canPaneConnectTo(IBlockAccess var1, BlockPos var2, EnumFacing var3);

   @Overwrite
   public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
      float f = 0.4375F;
      float f2 = 0.5625F;
      float f3 = 0.4375F;
      float f4 = 0.5625F;
      boolean flag = this.canPaneConnectToBlock(worldIn.getBlockState(pos.north()).getBlock());
      boolean flag2 = this.canPaneConnectToBlock(worldIn.getBlockState(pos.south()).getBlock());
      boolean flag3 = this.canPaneConnectToBlock(worldIn.getBlockState(pos.west()).getBlock());
      boolean flag4 = this.canPaneConnectToBlock(worldIn.getBlockState(pos.east()).getBlock());
      if ((!flag3 || !flag4) && (flag3 || flag4 || flag || flag2)) {
         if (flag3) {
            f = 0.0F;
         }
      } else if (flag3) {
         f = 0.0F;
         f2 = 1.0F;
      }

      if ((!flag || !flag2) && (flag3 || flag4 || flag || flag2)) {
         if (flag) {
            f3 = 0.0F;
         } else if (flag2) {
            f4 = 1.0F;
         }
      } else if (flag) {
         f3 = 0.0F;
         f4 = 1.0F;
      }

      this.setBlockBounds(f, 0.0F, f3, f2, 1.0F, f4);
   }

   @Overwrite
   public void addCollisionBoxesToList(World p_addCollisionBoxesToList_1_, BlockPos p_addCollisionBoxesToList_2_, IBlockState p_addCollisionBoxesToList_3_, AxisAlignedBB p_addCollisionBoxesToList_4_, List p_addCollisionBoxesToList_5_, Entity p_addCollisionBoxesToList_6_) {
      float f = 0.4375F;
      float f2 = 0.5625F;
      float f3 = 0.4375F;
      float f4 = 0.5625F;
      boolean flag = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.NORTH);
      boolean flag2 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.SOUTH);
      boolean flag3 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.WEST);
      boolean flag4 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.EAST);
      if ((!flag3 || !flag4) && (flag3 || flag4 || flag || flag2)) {
         if (flag3) {
            f = 0.0F;
         }
      } else if (flag3) {
         f = 0.0F;
         f2 = 1.0F;
      }

      if ((!flag || !flag2) && (flag3 || flag4 || flag || flag2)) {
         if (flag) {
            f3 = 0.0F;
         } else if (flag2) {
            f4 = 1.0F;
         }
      } else if (flag) {
         f3 = 0.0F;
         f4 = 1.0F;
      }

      this.setBlockBounds(f, 0.0F, f3, f2, 1.0F, f4);
      super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
   }
}
