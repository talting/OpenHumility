package cn.hanabi.utils;

import cn.hanabi.Wrapper;
import cn.hanabi.events.EventMove;
import cn.hanabi.events.EventPreMotion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWall;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class MoveUtils {
   public static final double BUNNY_SLOPE = 0.6625D;
   public static final double WATCHDOG_BUNNY_SLOPE = 0.636D;
   public static final double SPRINTING_MOD = 1.3D;
   public static final double ICE_MOD = 2.5D;
   public static final List frictionValues = new ArrayList();
   public static final double MIN_DIF = 1.0E-4D;
   public static final double MAX_DIST = 2.1498999999999997D;
   public static final double WALK_SPEED = 0.221D;
   public static final double SWIM_MOD = 0.5203619909502263D;
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final Minecraft MC = Minecraft.getMinecraft();

   public MoveUtils() {
      super();
   }

   public static double getJumpHeight() {
      double baseJumpHeight = 0.41999998688697815D;
      if (PlayerUtil.isInLiquid()) {
         return 0.135D;
      } else {
         return mc.thePlayer.isPotionActive(Potion.jump) ? baseJumpHeight + 0.1D * (double)mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() : baseJumpHeight;
      }
   }

   public static boolean isInLiquid() {
      return Wrapper.getPlayer().isInWater() || Wrapper.getPlayer().isInLava();
   }

   public static double getJumpHeight(double baseJumpHeight) {
      if (isInLiquid()) {
         return 0.13499999955296516D;
      } else {
         return Wrapper.getPlayer().isPotionActive(Potion.jump) ? baseJumpHeight + (double)(((float)Wrapper.getPlayer().getActivePotionEffect(Potion.jump).getAmplifier() + 1.0F) * 0.1F) : baseJumpHeight;
      }
   }

   public static double getBaseSpeed(double v1, double v3) {
      if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
         int a1 = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 - (mc.thePlayer.isPotionActive(Potion.moveSlowdown) ? mc.thePlayer.getActivePotionEffect(Potion.moveSlowdown).getAmplifier() + 1 : 0);
         v1 *= 1.0D + v3 * (double)a1;
      }

      return v1;
   }

   public static double defaultSpeed() {
      double baseSpeed = 0.2873D;
      if (Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.moveSpeed)) {
         int amplifier = Minecraft.getMinecraft().thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
         baseSpeed *= 1.0D + 0.2D * (double)(amplifier + 1);
      }

      return baseSpeed;
   }

   public static void setMotion(EventMove event, double speed) {
      double forward = (double)mc.thePlayer.movementInput.moveForward;
      double strafe = (double)mc.thePlayer.movementInput.moveStrafe;
      float yaw = mc.thePlayer.rotationYaw;
      if (forward == 0.0D && strafe == 0.0D) {
         event.setX(mc.thePlayer.motionX = 0.0D);
         event.setZ(mc.thePlayer.motionZ = 0.0D);
      } else {
         if (forward != 0.0D) {
            if (strafe > 0.0D) {
               yaw += (float)(forward > 0.0D ? -45 : 45);
            } else if (strafe < 0.0D) {
               yaw += (float)(forward > 0.0D ? 45 : -45);
            }

            strafe = 0.0D;
            if (forward > 0.0D) {
               forward = 1.0D;
            } else if (forward < 0.0D) {
               forward = -1.0D;
            }
         }

         event.setX(mc.thePlayer.motionX = forward * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F))) + strafe * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F))));
         event.setZ(mc.thePlayer.motionZ = forward * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F))) - strafe * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F))));
      }

   }

   public static boolean isBlockNearBy(double distance) {
      double smallX = Math.min(mc.thePlayer.posX - distance, mc.thePlayer.posX + distance);
      double smallY = Math.min(mc.thePlayer.posY, mc.thePlayer.posY);
      double smallZ = Math.min(mc.thePlayer.posZ - distance, mc.thePlayer.posZ + distance);
      double bigX = Math.max(mc.thePlayer.posX - distance, mc.thePlayer.posX + distance);
      double bigY = Math.max(mc.thePlayer.posY, mc.thePlayer.posY);
      double bigZ = Math.max(mc.thePlayer.posZ - distance, mc.thePlayer.posZ + distance);

      for(int x = (int)smallX; (double)x <= bigX; ++x) {
         for(int y = (int)smallY; (double)y <= bigY; ++y) {
            for(int z = (int)smallZ; (double)z <= bigZ; ++z) {
               if (!checkPositionValidity(new Vec3((double)x, (double)y, (double)z)) && checkPositionValidity(new Vec3((double)x, (double)(y + 1), (double)z))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean checkPositionValidity(Vec3 vec3) {
      BlockPos pos = new BlockPos(vec3);
      return !isBlockSolid(pos) && !isBlockSolid(pos.add(0, 1, 0)) ? isSafeToWalkOn(pos.add(0, -1, 0)) : false;
   }

   private static boolean isBlockSolid(BlockPos pos) {
      Block block = mc.theWorld.getBlockState(pos).getBlock();
      return block instanceof BlockSlab || block instanceof BlockStairs || block instanceof BlockCactus || block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSkull || block instanceof BlockPane || block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockGlass || block instanceof BlockPistonBase || block instanceof BlockPistonExtension || block instanceof BlockPistonMoving || block instanceof BlockStainedGlass || block instanceof BlockTrapDoor;
   }

   private static boolean isSafeToWalkOn(BlockPos pos) {
      Block block = mc.theWorld.getBlockState(pos).getBlock();
      return !(block instanceof BlockFence) && !(block instanceof BlockWall);
   }

   public static void setMotion(double speed, float directionInYaw) {
      double forward = (double)mc.thePlayer.movementInput.moveForward;
      double strafe = (double)mc.thePlayer.movementInput.moveStrafe;
      float yaw = directionInYaw;
      if (forward == 0.0D && strafe == 0.0D) {
         mc.thePlayer.motionX = 0.0D;
         mc.thePlayer.motionZ = 0.0D;
      } else {
         if (forward != 0.0D) {
            if (strafe > 0.0D) {
               yaw = directionInYaw + (float)(forward > 0.0D ? -45 : 45);
            } else if (strafe < 0.0D) {
               yaw = directionInYaw + (float)(forward > 0.0D ? 45 : -45);
            }

            strafe = 0.0D;
            if (forward > 0.0D) {
               forward = 1.0D;
            } else if (forward < 0.0D) {
               forward = -1.0D;
            }
         }

         mc.thePlayer.motionX = forward * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F))) + strafe * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F)));
         mc.thePlayer.motionZ = forward * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F))) - strafe * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F)));
      }

   }

   public static boolean checkTeleport(double x, double y, double z, double distBetweenPackets) {
      double var10000 = mc.thePlayer.posX - x;
      var10000 = mc.thePlayer.posY - y;
      var10000 = mc.thePlayer.posZ - z;
      double dist = Math.sqrt(mc.thePlayer.getDistanceSq(x, y, z));
      double nbPackets = (double)(Math.round(dist / distBetweenPackets + 0.49999999999D) - 1L);
      double xtp = mc.thePlayer.posX;
      double ytp = mc.thePlayer.posY;
      double ztp = mc.thePlayer.posZ;

      for(int i = 1; (double)i < nbPackets; ++i) {
         double xdi = (x - mc.thePlayer.posX) / nbPackets;
         xtp += xdi;
         double zdi = (z - mc.thePlayer.posZ) / nbPackets;
         ztp += zdi;
         double ydi = (y - mc.thePlayer.posY) / nbPackets;
         ytp += ydi;
         AxisAlignedBB bb = new AxisAlignedBB(xtp - 0.3D, ytp, ztp - 0.3D, xtp + 0.3D, ytp + 1.8D, ztp + 0.3D);
         if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public static boolean isOnGround(double height) {
      return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, -height, 0.0D)).isEmpty();
   }

   public static int getJumpEffect() {
      return mc.thePlayer.isPotionActive(Potion.jump) ? mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1 : 0;
   }

   public static int getSpeedEffect() {
      return mc.thePlayer.isPotionActive(Potion.moveSpeed) ? mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 : 0;
   }

   public static Block getBlockUnderPlayer(EntityPlayer inPlayer, double height) {
      return Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(inPlayer.posX, inPlayer.posY - height, inPlayer.posZ)).getBlock();
   }

   public static Block getBlockAtPosC(double x, double y, double z) {
      EntityPlayer inPlayer = Minecraft.getMinecraft().thePlayer;
      return Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(inPlayer.posX + x, inPlayer.posY + y, inPlayer.posZ + z)).getBlock();
   }

   public static float getDistanceToGround(Entity e) {
      if (mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround) {
         return 0.0F;
      } else {
         for(float a = (float)e.posY; a > 0.0F; --a) {
            int[] stairs = new int[]{53, 67, 108, 109, 114, 128, 134, 135, 136, 156, 163, 164, 180};
            int[] exemptIds = new int[]{6, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 63, 65, 66, 68, 69, 70, 72, 75, 76, 77, 83, 92, 93, 94, 104, 105, 106, 115, 119, 131, 132, 143, 147, 148, 149, 150, 157, 171, 175, 176, 177};
            Block block = mc.theWorld.getBlockState(new BlockPos(e.posX, (double)(a - 1.0F), e.posZ)).getBlock();
            if (!(block instanceof BlockAir)) {
               if (Block.getIdFromBlock(block) != 44 && Block.getIdFromBlock(block) != 126) {
                  for(int id : stairs) {
                     if (Block.getIdFromBlock(block) == id) {
                        return Math.max((float)(e.posY - (double)a - 1.0D), 0.0F);
                     }
                  }

                  for(int id : exemptIds) {
                     if (Block.getIdFromBlock(block) == id) {
                        return Math.max((float)(e.posY - (double)a), 0.0F);
                     }
                  }

                  return (float)(e.posY - (double)a + block.getBlockBoundsMaxY() - 1.0D);
               }

               return Math.max((float)(e.posY - (double)a - 0.5D), 0.0F);
            }
         }

         return 0.0F;
      }
   }

   public static float[] getRotationsBlock(BlockPos block, EnumFacing face) {
      double x = (double)block.getX() + 0.5D - mc.thePlayer.posX + (double)face.getFrontOffsetX() / 2.0D;
      double z = (double)block.getZ() + 0.5D - mc.thePlayer.posZ + (double)face.getFrontOffsetZ() / 2.0D;
      double y = (double)block.getY() + 0.5D;
      double d1 = mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight() - y;
      double d3 = (double)MathHelper.sqrt_double(x * x + z * z);
      float yaw = (float)(Math.atan2(z, x) * 180.0D / 3.141592653589793D) - 90.0F;
      float pitch = (float)(Math.atan2(d1, d3) * 180.0D / 3.141592653589793D);
      if (yaw < 0.0F) {
         yaw += 360.0F;
      }

      return new float[]{yaw, pitch};
   }

   public static boolean isBlockAboveHead() {
      AxisAlignedBB bb = new AxisAlignedBB(mc.thePlayer.posX - 0.3D, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ + 0.3D, mc.thePlayer.posX + 0.3D, mc.thePlayer.posY + 2.5D, mc.thePlayer.posZ - 0.3D);
      return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty();
   }

   public static boolean isOnGround(Entity entity, double height) {
      return !mc.theWorld.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox().offset(0.0D, -height, 0.0D)).isEmpty();
   }

   public static boolean isCollidedH(double dist) {
      AxisAlignedBB bb = new AxisAlignedBB(mc.thePlayer.posX - 0.3D, mc.thePlayer.posY + 2.0D, mc.thePlayer.posZ + 0.3D, mc.thePlayer.posX + 0.3D, mc.thePlayer.posY + 3.0D, mc.thePlayer.posZ - 0.3D);
      if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0.3D + dist, 0.0D, 0.0D)).isEmpty()) {
         return true;
      } else if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(-0.3D - dist, 0.0D, 0.0D)).isEmpty()) {
         return true;
      } else if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0.0D, 0.0D, 0.3D + dist)).isEmpty()) {
         return true;
      } else {
         return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0.0D, 0.0D, -0.3D - dist)).isEmpty();
      }
   }

   public static double getArBaseMoveSpeed() {
      double baseSpeed = 0.2875D;
      if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
         baseSpeed *= 1.0D + 0.2D * (double)(mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
      }

      return baseSpeed;
   }

   public static float getSpeed() {
      return (float)Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
   }

   public static void strafe() {
      strafe((double)getSpeed());
   }

   public static void strafe(EventMove e) {
      strafe(e, (double)getSpeed());
   }

   public static boolean isMoving() {
      return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0.0F || mc.thePlayer.movementInput.moveStrafe != 0.0F);
   }

   public static boolean hasMotion() {
      return mc.thePlayer.motionX != 0.0D && mc.thePlayer.motionZ != 0.0D && mc.thePlayer.motionY != 0.0D;
   }

   public static double calculateFriction(double moveSpeed, double lastDist, double baseMoveSpeedRef) {
      frictionValues.clear();
      frictionValues.add(Double.valueOf(lastDist - lastDist / 159.9999985D));
      frictionValues.add(Double.valueOf(lastDist - (moveSpeed - lastDist) / 33.3D));
      double materialFriction = Wrapper.getPlayer().isInWater() ? 0.8899999856948853D : (Wrapper.getPlayer().isInLava() ? 0.5350000262260437D : 0.9800000190734863D);
      frictionValues.add(Double.valueOf(lastDist - baseMoveSpeedRef * (1.0D - materialFriction)));
      return ((Double)Collections.min(frictionValues)).doubleValue();
   }

   public static void strafe(double d) {
      if (isMoving()) {
         double yaw = getDirection();
         mc.thePlayer.motionX = -Math.sin(yaw) * d;
         mc.thePlayer.motionZ = Math.cos(yaw) * d;
      }
   }

   public static void strafe(EventMove e, double d) {
      if (isMoving()) {
         double yaw = getDirection();
         e.setX(mc.thePlayer.motionX = -Math.sin(yaw) * d);
         e.setZ(mc.thePlayer.motionZ = Math.cos(yaw) * d);
      }
   }

   public static void forward(double length) {
      double yaw = Math.toRadians((double)mc.thePlayer.rotationYaw);
      mc.thePlayer.setPosition(mc.thePlayer.posX + -Math.sin(yaw) * length, mc.thePlayer.posY, mc.thePlayer.posZ + Math.cos(yaw) * length);
   }

   public static double getDirection() {
      float rotationYaw = mc.thePlayer.rotationYaw;
      if (mc.thePlayer.moveForward < 0.0F) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (mc.thePlayer.moveForward < 0.0F) {
         forward = -0.5F;
      } else if (mc.thePlayer.moveForward > 0.0F) {
         forward = 0.5F;
      }

      if (mc.thePlayer.moveStrafing > 0.0F) {
         rotationYaw -= 90.0F * forward;
      }

      if (mc.thePlayer.moveStrafing < 0.0F) {
         rotationYaw += 90.0F * forward;
      }

      return Math.toRadians((double)rotationYaw);
   }

   public static void setSpeed(EventMove moveEvent, double moveSpeed) {
      setSpeed(moveEvent, moveSpeed, mc.thePlayer.rotationYaw, (double)mc.thePlayer.movementInput.moveStrafe * 0.9D, (double)mc.thePlayer.movementInput.moveForward);
   }

   public static void setSpeed(EventMove moveEvent, double moveSpeed, float pseudoYaw, double pseudoStrafe, double pseudoForward) {
      double forward = pseudoForward;
      double strafe = pseudoStrafe;
      float yaw = pseudoYaw;
      if (pseudoForward == 0.0D && pseudoStrafe == 0.0D) {
         moveEvent.setZ(0.0D);
         moveEvent.setX(0.0D);
      } else {
         if (pseudoForward != 0.0D) {
            if (pseudoStrafe > 0.0D) {
               yaw = pseudoYaw + (float)(pseudoForward > 0.0D ? -45 : 45);
            } else if (pseudoStrafe < 0.0D) {
               yaw = pseudoYaw + (float)(pseudoForward > 0.0D ? 45 : -45);
            }

            strafe = 0.0D;
            if (pseudoForward > 0.0D) {
               forward = 1.0D;
            } else if (pseudoForward < 0.0D) {
               forward = -1.0D;
            }
         }

         double cos = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
         double sin = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
         moveEvent.setX(forward * moveSpeed * cos + strafe * moveSpeed * sin);
         moveEvent.setZ(forward * moveSpeed * sin - strafe * moveSpeed * cos);
      }

   }

   public static void setSpeed(EventPreMotion moveEvent, double moveSpeed, float pseudoYaw, double pseudoStrafe, double pseudoForward) {
      double forward = pseudoForward;
      double strafe = pseudoStrafe;
      float yaw = pseudoYaw;
      if (pseudoForward == 0.0D && pseudoStrafe == 0.0D) {
         moveEvent.setZ(0.0D);
         moveEvent.setX(0.0D);
      } else {
         if (pseudoForward != 0.0D) {
            if (pseudoStrafe > 0.0D) {
               yaw = pseudoYaw + (float)(pseudoForward > 0.0D ? -45 : 45);
            } else if (pseudoStrafe < 0.0D) {
               yaw = pseudoYaw + (float)(pseudoForward > 0.0D ? 45 : -45);
            }

            strafe = 0.0D;
            if (pseudoForward > 0.0D) {
               forward = 1.0D;
            } else if (pseudoForward < 0.0D) {
               forward = -1.0D;
            }
         }

         double cos = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
         double sin = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
         moveEvent.setX(forward * moveSpeed * cos + strafe * moveSpeed * sin);
         moveEvent.setZ(forward * moveSpeed * sin - strafe * moveSpeed * cos);
      }

   }

   public static boolean isBlockAbovePlayer() {
      return !(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().maxY + 0.41999998688697815D, mc.thePlayer.posZ)).getBlock() instanceof BlockAir);
   }

   public static double getBaseMoveSpeed(double value, double effect) {
      double baseSpeed = value;
      if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
         baseSpeed = value * (1.0D + effect * (double)(mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1));
      }

      return baseSpeed;
   }

   public static boolean isOverVoid() {
      for(int i = (int)(mc.thePlayer.posY - 1.0D); i > 0; --i) {
         BlockPos pos = new BlockPos(mc.thePlayer.posX, (double)i, mc.thePlayer.posZ);
         if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir)) {
            return false;
         }
      }

      return true;
   }
}
