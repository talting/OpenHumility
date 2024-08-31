package cn.hanabi.modules.modules.render;

import cn.hanabi.events.EventRender;
import cn.hanabi.events.EventRenderBlock;
import cn.hanabi.injection.interfaces.IRenderManager;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.RenderUtil;
import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.util.BlockPos;

public class ChestESP extends Mod {
   ArrayList<BlockPos> list = new ArrayList();

   public ChestESP() {
      super("ChestESP", Category.RENDER);
   }

   public void onEnable() {
      mc.renderGlobal.loadRenderers();
      this.list.clear();
   }

   @EventTarget
   public void onRenderBlock(EventRenderBlock e) {
      BlockPos pos = new BlockPos(e.x, e.y, e.z);
      if (!this.list.contains(pos) && (e.block instanceof BlockChest || e.block instanceof BlockEnderChest)) {
         this.list.add(pos);
      }

   }

   @EventTarget
   public void onRender(EventRender e) {
      for(BlockPos pos : this.list) {
         if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockChest) && !(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockEnderChest)) {
            this.list.remove(pos);
         }

         RenderUtil.drawSolidBlockESP((double)pos.getX() - ((IRenderManager)mc.getRenderManager()).getRenderPosX(), (double)pos.getY() - ((IRenderManager)mc.getRenderManager()).getRenderPosY(), (double)pos.getZ() - ((IRenderManager)mc.getRenderManager()).getRenderPosZ(), 1.0F, 1.0F, 1.0F, 0.2F);
      }

   }
}
