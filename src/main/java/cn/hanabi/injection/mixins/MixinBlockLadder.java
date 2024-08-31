package cn.hanabi.injection.mixins;

import net.minecraft.block.BlockLadder;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({BlockLadder.class})
public abstract class MixinBlockLadder extends MixinBlock {
   @Shadow
   @Final
   public static PropertyDirection FACING;

   public MixinBlockLadder() {
      super();
   }

   @Overwrite
   public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      if (iblockstate.getBlock() instanceof BlockLadder) {
         switch((EnumFacing)iblockstate.getValue(FACING)) {
         case NORTH:
            this.setBlockBounds(0.0F, 0.0F, 0.8125F, 1.0F, 1.0F, 1.0F);
            break;
         case SOUTH:
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.1875F);
            break;
         case WEST:
            this.setBlockBounds(0.8125F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
            break;
         default:
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.1875F, 1.0F, 1.0F);
         }
      }

   }
}
