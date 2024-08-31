package cn.hanabi.modules.modules.player;

import cn.hanabi.Wrapper;
import cn.hanabi.events.EventRender;
import cn.hanabi.events.EventUpdate;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.MoveUtils;
import cn.hanabi.utils.NukerUtil;
import cn.hanabi.utils.PlayerUtil;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.utils.pathfinder.PathUtils;
import cn.hanabi.utils.pathfinder.Vec3;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.block.BlockBed;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.util.BlockPos;

public class TeleportBedFucker extends Mod {
   public BlockPos playerBed;
   public BlockPos fuckingBed;
   public ArrayList<BlockPos> posList;
   public Value delay = new Value("TP2Bed", "Delay", 600.0D, 200.0D, 3000.0D, 100.0D);
   TimeHelper timer = new TimeHelper();
   private ArrayList<Vec3> path = new ArrayList();

   public TeleportBedFucker() {
      super("TP2Bed", Category.PLAYER);
   }

   public void onEnable() {
      try {
         this.posList = new ArrayList(NukerUtil.list);
         this.posList.sort((o1, o2) -> {
            double distance1 = this.getDistanceToBlock(o1);
            double distance2 = this.getDistanceToBlock(o2);
            return (int)(distance1 - distance2);
         });
         if (this.posList.size() < 3) {
            this.set(false);
         }

         ArrayList<BlockPos> posListFor = new ArrayList(this.posList);
         int index = 1;

         for(BlockPos kid : posListFor) {
            ++index;
            if (index % 2 == 1) {
               this.posList.remove(kid);
            }
         }

         this.playerBed = (BlockPos)this.posList.get(0);
         this.posList.remove(0);
         if (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically && MoveUtils.isOnGround(0.01D)) {
            for(int i = 0; i < 49; ++i) {
               mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.06249D, mc.thePlayer.posZ, false));
               mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
            }

            mc.thePlayer.onGround = false;
            mc.thePlayer.jumpMovementFactor = 0.0F;
         }

         this.fuckingBed = (BlockPos)this.posList.get(0);
      } catch (Throwable var5) {
         this.set(false);
      }

   }

   @EventTarget
   public void onRender(EventRender e) {
   }

   protected void onDisable() {
      Wrapper.canSendMotionPacket = true;
      super.onDisable();
   }

   @EventTarget
   public void onUpdate(EventUpdate e) {
      for(BlockPos pos : this.posList) {
         if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockBed)) {
            PlayerUtil.tellPlayer("Destory!" + pos);
            this.posList.remove(pos);
            this.posList.sort((o1, o2) -> {
               double distance1 = this.getDistanceToBlock(o1);
               double distance2 = this.getDistanceToBlock(o2);
               return (int)(distance1 - distance2);
            });
            this.fuckingBed = (BlockPos)this.posList.get(0);
         }
      }

      if (mc.thePlayer.getDistance((double)this.fuckingBed.getX(), (double)this.fuckingBed.getY(), (double)this.fuckingBed.getZ()) < 4.0D) {
         Wrapper.canSendMotionPacket = true;
         PlayerUtil.tellPlayer("Teleported! :3");
         this.set(false);
      }

      if (this.timer.isDelayComplete((Double)this.delay.getValueState())) {
         Vec3 topFrom = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
         Vec3 to = new Vec3((double)(this.fuckingBed.getX() + 1), (double)this.fuckingBed.getY(), (double)(this.fuckingBed.getZ() + 1));
         this.path = PathUtils.computePath(topFrom, to);
         if (mc.thePlayer.getDistance((double)this.fuckingBed.getX(), (double)this.fuckingBed.getY(), (double)this.fuckingBed.getZ()) > 4.0D) {
            PlayerUtil.tellPlayer("Trying to teleport...");
            Wrapper.canSendMotionPacket = false;

            for(Vec3 pathElm : this.path) {
               mc.thePlayer.sendQueue.addToSendQueue(new C04PacketPlayerPosition(pathElm.getX(), pathElm.getY(), pathElm.getZ(), true));
            }
         }

         this.timer.reset();
      }

      if (this.posList.size() == 0) {
         this.set(false);
      }

   }

   public double getDistanceToBlock(BlockPos pos) {
      return mc.thePlayer.getDistance((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
   }
}
