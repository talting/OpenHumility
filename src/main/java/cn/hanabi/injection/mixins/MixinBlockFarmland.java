package cn.hanabi.injection.mixins;

import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin({BlockFarmland.class})
public abstract class MixinBlockFarmland {
   public MixinBlockFarmland() {
      super();
   }

   @Overwrite
   public AxisAlignedBB getCollisionBoundingBox(World p_getCollisionBoundingBox_1_, BlockPos p_getCollisionBoundingBox_2_, IBlockState p_getCollisionBoundingBox_3_) {
      return new AxisAlignedBB((double)p_getCollisionBoundingBox_2_.getX(), (double)p_getCollisionBoundingBox_2_.getY(), (double)p_getCollisionBoundingBox_2_.getZ(), (double)(p_getCollisionBoundingBox_2_.getX() + 1), (double)((float)p_getCollisionBoundingBox_2_.getY() + 0.9375F), (double)(p_getCollisionBoundingBox_2_.getZ() + 1));
   }
}
