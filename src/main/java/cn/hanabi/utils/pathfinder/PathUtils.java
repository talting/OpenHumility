package cn.hanabi.utils.pathfinder;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class PathUtils {
   public PathUtils() {
      super();
   }

   private static boolean canPassThrough(BlockPos pos) {
      Block block = Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ())).getBlock();
      return block.getMaterial() == Material.air || block.getMaterial() == Material.plants || block.getMaterial() == Material.vine || block == Blocks.ladder || block == Blocks.water || block == Blocks.flowing_water || block == Blocks.wall_sign || block == Blocks.standing_sign;
   }

   public static ArrayList computePath(EntityLivingBase fromEntity, EntityLivingBase toEntity) {
      return computePath(new Vec3(fromEntity.posX, fromEntity.posY, fromEntity.posZ), new Vec3(toEntity.posX, toEntity.posY, toEntity.posZ));
   }

   public static ArrayList computePath(Vec3 topFrom, Vec3 to) {
      if (!canPassThrough(new BlockPos(topFrom.mc()))) {
         topFrom = topFrom.addVector(0.0D, 1.0D, 0.0D);
      }

      AStarCustomPathFinder pathfinder = new AStarCustomPathFinder(topFrom, to);
      pathfinder.compute();
      int i = 0;
      Vec3 lastLoc = null;
      Vec3 lastDashLoc = null;
      ArrayList<Vec3> path = new ArrayList();
      ArrayList<Vec3> pathFinderPath = pathfinder.getPath();

      for(Vec3 pathElm : pathFinderPath) {
         if (i != 0 && i != pathFinderPath.size() - 1) {
            boolean canContinue = true;
            if (pathElm.squareDistanceTo(lastDashLoc) > 25.0D) {
               canContinue = false;
            } else {
               double smallX = Math.min(lastDashLoc.getX(), pathElm.getX());
               double smallY = Math.min(lastDashLoc.getY(), pathElm.getY());
               double smallZ = Math.min(lastDashLoc.getZ(), pathElm.getZ());
               double bigX = Math.max(lastDashLoc.getX(), pathElm.getX());
               double bigY = Math.max(lastDashLoc.getY(), pathElm.getY());
               double bigZ = Math.max(lastDashLoc.getZ(), pathElm.getZ());

               label54:
               for(int x = (int)smallX; (double)x <= bigX; ++x) {
                  for(int y = (int)smallY; (double)y <= bigY; ++y) {
                     for(int z = (int)smallZ; (double)z <= bigZ; ++z) {
                        if (!AStarCustomPathFinder.checkPositionValidity(x, y, z, false)) {
                           canContinue = false;
                           break label54;
                        }
                     }
                  }
               }
            }

            if (!canContinue) {
               path.add(lastLoc.addVector(0.5D, 0.0D, 0.5D));
               lastDashLoc = lastLoc;
            }
         } else {
            if (lastLoc != null) {
               path.add(lastLoc.addVector(0.5D, 0.0D, 0.5D));
            }

            path.add(pathElm.addVector(0.5D, 0.0D, 0.5D));
            lastDashLoc = pathElm;
         }

         lastLoc = pathElm;
         ++i;
      }

      return path;
   }
}
