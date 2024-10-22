package cn.hanabi.injection.mixins;

import cn.hanabi.modules.modules.combat.KillAura;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@SideOnly(Side.CLIENT)
@Mixin({LayerHeldItem.class})
public class MixinLayerHeldItem {
   @Final
   @Shadow
   private Minecraft mc;
   @Shadow
   @Final
   private RendererLivingEntity livingEntityRenderer;

   public MixinLayerHeldItem() {
      super();
   }

   @Overwrite
   public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
      ItemStack itemstack = entitylivingbaseIn.getHeldItem();
      if (itemstack != null) {
         GlStateManager.pushMatrix();
         if (this.livingEntityRenderer.getMainModel().isChild) {
            float f = 0.5F;
            GlStateManager.translate(0.0F, 0.625F, 0.0F);
            GlStateManager.rotate(-20.0F, -1.0F, 0.0F, 0.0F);
            GlStateManager.scale(f, f, f);
         }

         UUID uuid = entitylivingbaseIn.getUniqueID();
         EntityPlayer entityplayer = this.mc.theWorld.getPlayerEntityByUUID(uuid);
         if (entityplayer != null && (entityplayer.isBlocking() || entityplayer instanceof EntityPlayerSP && itemstack.getItem() instanceof ItemSword && KillAura.renderBlock)) {
            if (entitylivingbaseIn.isSneaking()) {
               ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0325F);
               GlStateManager.translate(-0.58F, 0.3F, -0.2F);
               GlStateManager.rotate(-24390.0F, 137290.0F, -2009900.0F, -2054900.0F);
            } else {
               ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0325F);
               GlStateManager.translate(-0.48F, 0.2F, -0.2F);
               GlStateManager.rotate(-24390.0F, 137290.0F, -2009900.0F, -2054900.0F);
            }
         } else {
            ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625F);
         }

         GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);
         if (entitylivingbaseIn instanceof EntityPlayer && ((EntityPlayer)entitylivingbaseIn).fishEntity != null) {
            itemstack = new ItemStack(Items.fishing_rod, 0);
         }

         Item item = itemstack.getItem();
         if (item instanceof ItemBlock && Block.getBlockFromItem(item).getRenderType() == 2) {
            GlStateManager.translate(0.0F, 0.1875F, -0.3125F);
            GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
            float f1 = 0.375F;
            GlStateManager.scale(-f1, -f1, f1);
         }

         if (entitylivingbaseIn.isSneaking()) {
            GlStateManager.translate(0.0F, 0.203125F, 0.0F);
         }

         this.mc.getItemRenderer().renderItem(entitylivingbaseIn, itemstack, TransformType.THIRD_PERSON);
         GlStateManager.popMatrix();
      }

   }
}
