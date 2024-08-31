package cn.hanabi.injection.mixins;

import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin({BlockLilyPad.class})
public abstract class MixinLilyPad {
   public MixinLilyPad() {
      super();
   }

   @Overwrite
   public AxisAlignedBB getCollisionBoundingBox(World p_getCollisionBoundingBox_1_, BlockPos p_getCollisionBoundingBox_2_, IBlockState p_getCollisionBoundingBox_3_) {
      return new AxisAlignedBB((double)p_getCollisionBoundingBox_2_.getX() + 0.0625D, (double)p_getCollisionBoundingBox_2_.getY(), (double)p_getCollisionBoundingBox_2_.getZ() + 0.0625D, (double)p_getCollisionBoundingBox_2_.getX() + 0.9375D, (double)p_getCollisionBoundingBox_2_.getY() + 0.09375D, (double)p_getCollisionBoundingBox_2_.getZ() + 0.9375D);
   }
}
