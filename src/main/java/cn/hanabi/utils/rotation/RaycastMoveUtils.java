package cn.hanabi.utils.rotation;

import com.google.common.base.Predicates;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public final class RaycastMoveUtils {
   public RaycastMoveUtils() {
      super();
   }

   public static Entity raycastEntity(double range, float yaw, float pitch) {
      Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
      if (renderViewEntity != null && Minecraft.getMinecraft().theWorld != null) {
         double blockReachDistance = range;
         Vec3 eyePosition = renderViewEntity.getPositionEyes(1.0F);
         float yawCos = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
         float yawSin = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
         float pitchCos = -MathHelper.cos(-pitch * 0.017453292F);
         float pitchSin = MathHelper.sin(-pitch * 0.017453292F);
         Vec3 entityLook = new Vec3((double)(yawSin * pitchCos), (double)pitchSin, (double)(yawCos * pitchCos));
         Vec3 vector = eyePosition.addVector(entityLook.xCoord * range, entityLook.yCoord * range, entityLook.zCoord * range);
         List<Entity> entityList = Minecraft.getMinecraft().theWorld.getEntitiesInAABBexcluding(renderViewEntity, renderViewEntity.getEntityBoundingBox().addCoord(entityLook.xCoord * range, entityLook.yCoord * range, entityLook.zCoord * range).expand(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
         Entity pointedEntity = null;

         for(Entity entity : entityList) {
            if (RaycastMoveUtils.IEntityFilter.canRaycast(entity)) {
               float collisionBorderSize = entity.getCollisionBorderSize();
               AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand((double)collisionBorderSize, (double)collisionBorderSize, (double)collisionBorderSize);
               MovingObjectPosition movingObjectPosition = axisAlignedBB.calculateIntercept(eyePosition, vector);
               if (axisAlignedBB.isVecInside(eyePosition)) {
                  if (blockReachDistance >= 0.0D) {
                     pointedEntity = entity;
                     blockReachDistance = 0.0D;
                  }
               } else if (movingObjectPosition != null) {
                  double eyeDistance = eyePosition.distanceTo(movingObjectPosition.hitVec);
                  if (eyeDistance < blockReachDistance || blockReachDistance == 0.0D) {
                     if (entity == renderViewEntity.ridingEntity && !renderViewEntity.canRiderInteract()) {
                        if (blockReachDistance == 0.0D) {
                           pointedEntity = entity;
                        }
                     } else {
                        pointedEntity = entity;
                        blockReachDistance = eyeDistance;
                     }
                  }
               }
            }
         }

         return pointedEntity;
      } else {
         return null;
      }
   }

   public interface IEntityFilter {
      static boolean canRaycast(Entity entity) {
         return true;
      }
   }
}