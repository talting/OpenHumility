package cn.hanabi.modules.modules.render;

import cn.hanabi.Hanabi;
import cn.hanabi.Wrapper;
import cn.hanabi.events.EventRender;
import cn.hanabi.events.EventRender2D;
import cn.hanabi.gui.font.noway.ttfr.HFontRenderer;
import cn.hanabi.injection.interfaces.IEntityRenderer;
import cn.hanabi.injection.interfaces.IRenderManager;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.Colors;
import cn.hanabi.utils.RenderUtil;
import cn.hanabi.utils.rotation.RotationUtil;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Nametags extends Mod {
   public static Map<EntityLivingBase, double[]> entityPositions = new HashMap();
   public Value invis = new Value("Nametags", "Invisible", false);
   public Value armor = new Value("Nametags", "Armor", false);

   public Nametags() {
      super("Nametags", Category.RENDER);
   }

   @EventTarget
   public void update(EventRender event) {
      try {
         this.updatePositions();
      } catch (Exception var3) {
         ;
      }

   }

   @EventTarget
   public void onRender2D(EventRender2D event) {
      ScaledResolution scaledRes = new ScaledResolution(mc);

      try {
         for(EntityLivingBase ent : entityPositions.keySet()) {
            if (ent != mc.thePlayer && (((Boolean)this.invis.getValue()).booleanValue() || !ent.isInvisible())) {
               GlStateManager.pushMatrix();
               if (ent instanceof EntityPlayer) {
                  double[] renderPositions = (double[])entityPositions.get(ent);
                  if (renderPositions[3] < 0.0D || renderPositions[3] >= 1.0D) {
                     GlStateManager.popMatrix();
                     continue;
                  }

                  HFontRenderer font = Hanabi.INSTANCE.fontManager.wqy16;
                  GlStateManager.translate(renderPositions[0] / (double)scaledRes.getScaleFactor(), renderPositions[1] / (double)scaledRes.getScaleFactor(), 0.0D);
                  GlStateManager.scale(1.0F, 1.0F, 1.0F);
                  GlStateManager.translate(0.0D, -2.5D, 0.0D);
                  String str = ent.getName();
                  float allWidth = (float)(font.getStringWidth(str.replaceAll("§.", "")) + 14);
                  RenderUtil.drawRect(-allWidth / 2.0F, -14.0F, allWidth / 2.0F, 0.0F, Colors.getColor(0, 150));
                  font.drawString(str.replaceAll("§.", ""), -allWidth / 2.0F + 5.5F, -13.0F, Colors.WHITE.c);
                  float nowhealth = (float)Math.ceil((double)(ent.getHealth() + ent.getAbsorptionAmount()));
                  float maxHealth = ent.getMaxHealth() + ent.getAbsorptionAmount();
                  float healthP = nowhealth / maxHealth;
                  int color = Colors.RED.c;
                  String text = ent.getDisplayName().getFormattedText();
                  text = text.replaceAll(text.contains("[") && text.contains("]") ? "§7" : "", "");

                  for(int i = 0; i < text.length(); ++i) {
                     if (text.charAt(i) == 167 && i + 1 < text.length()) {
                        char oneMore = Character.toLowerCase(text.charAt(i + 1));
                        int colorCode = "0123456789abcdefklmnorg".indexOf(oneMore);
                        if (colorCode < 16) {
                           try {
                              color = RenderUtil.reAlpha(mc.fontRendererObj.getColorCode(oneMore), 1.0F);
                           } catch (ArrayIndexOutOfBoundsException var26) {
                              ;
                           }
                        }
                     }
                  }

                  RenderUtil.drawRect(-allWidth / 2.0F, -2.0F, allWidth / 2.0F - allWidth / 2.0F * (1.0F - healthP) * 2.0F, 0.0F, RenderUtil.reAlpha(color, 0.8F));
                  boolean armors = ((Boolean)this.armor.getValue()).booleanValue();
                  if (armors) {
                     List<ItemStack> itemsToRender = new ArrayList();

                     for(int i = 0; i < 5; ++i) {
                        ItemStack stack = ent.getEquipmentInSlot(i);
                        if (stack != null) {
                           itemsToRender.add(stack);
                        }
                     }

                     int x = -(itemsToRender.size() * 9) - 3;

                     for(ItemStack stack : itemsToRender) {
                        GlStateManager.pushMatrix();
                        RenderHelper.enableGUIStandardItemLighting();
                        GlStateManager.disableAlpha();
                        GlStateManager.clear(256);
                        mc.getRenderItem().zLevel = -150.0F;
                        this.fixGlintShit();
                        mc.getRenderItem().renderItemIntoGUI(stack, x + 6, -32);
                        mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, x + 6, -32);
                        mc.getRenderItem().zLevel = 0.0F;
                        x += 6;
                        GlStateManager.enableAlpha();
                        RenderHelper.disableStandardItemLighting();
                        GlStateManager.popMatrix();
                        if (stack != null) {
                           int y = 0;
                           int sLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
                           int fLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
                           int kLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
                           if (sLevel > 0) {
                              this.drawEnchantTag("Sh" + this.getColor(sLevel) + sLevel, (float)x, (float)y);
                              y += Hanabi.INSTANCE.fontManager.wqy13.FONT_HEIGHT - 2;
                           }

                           if (fLevel > 0) {
                              this.drawEnchantTag("Fir" + this.getColor(fLevel) + fLevel, (float)x, (float)y);
                              y += Hanabi.INSTANCE.fontManager.wqy13.FONT_HEIGHT - 2;
                           }

                           if (kLevel > 0) {
                              this.drawEnchantTag("Kb" + this.getColor(kLevel) + kLevel, (float)x, (float)y);
                           } else if (stack.getItem() instanceof ItemArmor) {
                              int pLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack);
                              int tLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack);
                              int uLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
                              if (pLevel > 0) {
                                 this.drawEnchantTag("P" + this.getColor(pLevel) + pLevel, (float)x, (float)y);
                                 y += Hanabi.INSTANCE.fontManager.wqy13.FONT_HEIGHT - 2;
                              }

                              if (tLevel > 0) {
                                 this.drawEnchantTag("Th" + this.getColor(tLevel) + tLevel, (float)x, (float)y);
                                 y += Hanabi.INSTANCE.fontManager.wqy13.FONT_HEIGHT - 2;
                              }

                              if (uLevel > 0) {
                                 this.drawEnchantTag("Unb" + this.getColor(uLevel) + uLevel, (float)x, (float)y);
                              }
                           } else if (stack.getItem() instanceof ItemBow) {
                              int powLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
                              int punLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
                              int fireLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack);
                              if (powLevel > 0) {
                                 this.drawEnchantTag("Pow" + this.getColor(powLevel) + powLevel, (float)x, (float)y);
                                 y += Hanabi.INSTANCE.fontManager.wqy13.FONT_HEIGHT - 2;
                              }

                              if (punLevel > 0) {
                                 this.drawEnchantTag("Pun" + this.getColor(punLevel) + punLevel, (float)x, (float)y);
                                 y += Hanabi.INSTANCE.fontManager.wqy13.FONT_HEIGHT - 2;
                              }

                              if (fireLevel > 0) {
                                 this.drawEnchantTag("Fir" + this.getColor(fireLevel) + fireLevel, (float)x, (float)y);
                              }
                           } else if (stack.getRarity() == EnumRarity.EPIC) {
                              this.drawEnchantTag("§6§lGod", (float)x - 0.5F, (float)(y + 12));
                           }

                           x += 12;
                        }
                     }
                  }
               }

               GlStateManager.popMatrix();
            }
         }
      } catch (Exception var27) {
         var27.printStackTrace();
      }

   }

   private void fixGlintShit() {
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();
      GlStateManager.disableBlend();
      GlStateManager.enableLighting();
      GlStateManager.enableDepth();
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();
      GlStateManager.disableTexture2D();
      GlStateManager.disableAlpha();
      GlStateManager.disableBlend();
      GlStateManager.enableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
      GlStateManager.enableLighting();
      GlStateManager.enableDepth();
   }

   private String getColor(int level) {
      if (level == 2) {
         return "§a";
      } else if (level == 3) {
         return "§3";
      } else if (level == 4) {
         return "§4";
      } else {
         return level >= 5 ? "§6" : "§f";
      }
   }

   private void drawEnchantTag(String text, float x, float y) {
      GlStateManager.pushMatrix();
      GlStateManager.disableDepth();
      x = (float)((int)((double)x * 1.05D));
      y = y - 6.0F;
      Hanabi.INSTANCE.fontManager.wqy13.drawString(text, x, -44.0F - y, Colors.WHITE.c);
      GlStateManager.enableDepth();
      GlStateManager.popMatrix();
   }

   private void updatePositions() {
      entityPositions.clear();
      float pTicks = Wrapper.getTimer().renderPartialTicks;

      for(Entity entity : mc.theWorld.loadedEntityList) {
         if (entity != mc.thePlayer && entity instanceof EntityPlayer && (!entity.isInvisible() || ((Boolean)this.invis.getValue()).booleanValue())) {
            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)pTicks - ((IRenderManager)mc.getRenderManager()).getRenderPosX();
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)pTicks - ((IRenderManager)mc.getRenderManager()).getRenderPosY();
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)pTicks - ((IRenderManager)mc.getRenderManager()).getRenderPosZ();
            y = y + (double)entity.height + 0.25D;
            if (((double[])Objects.requireNonNull(this.convertTo2D(x, y, z)))[2] >= 0.0D && ((double[])Objects.requireNonNull(this.convertTo2D(x, y, z)))[2] < 1.0D) {
               entityPositions.put((EntityPlayer)entity, new double[]{((double[])Objects.requireNonNull(this.convertTo2D(x, y, z)))[0], ((double[])Objects.requireNonNull(this.convertTo2D(x, y, z)))[1], Math.abs(this.convertTo2D(x, y + 1.0D, z, entity)[1] - this.convertTo2D(x, y, z, entity)[1]), ((double[])Objects.requireNonNull(this.convertTo2D(x, y, z)))[2]});
            }
         }
      }

   }

   private double[] convertTo2D(double x, double y, double z, Entity ent) {
      float pTicks = Wrapper.getTimer().renderPartialTicks;
      float prevYaw = mc.thePlayer.rotationYaw;
      float prevPrevYaw = mc.thePlayer.prevRotationYaw;
      float[] rotations = RotationUtil.getRotationFromPosition(ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * (double)pTicks, ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * (double)pTicks, ent.lastTickPosY + (ent.posY - ent.lastTickPosY) * (double)pTicks - 1.6D);
      mc.getRenderViewEntity().rotationYaw = mc.getRenderViewEntity().prevRotationYaw = rotations[0];
      ((IEntityRenderer)Minecraft.getMinecraft().entityRenderer).runSetupCameraTransform(pTicks, 0);
      double[] convertedPoints = this.convertTo2D(x, y, z);
      mc.getRenderViewEntity().rotationYaw = prevYaw;
      mc.getRenderViewEntity().prevRotationYaw = prevPrevYaw;
      ((IEntityRenderer)Minecraft.getMinecraft().entityRenderer).runSetupCameraTransform(pTicks, 0);
      return convertedPoints;
   }

   private double[] convertTo2D(double x, double y, double z) {
      FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
      IntBuffer viewport = BufferUtils.createIntBuffer(16);
      FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
      FloatBuffer projection = BufferUtils.createFloatBuffer(16);
      GL11.glGetFloat(2982, modelView);
      GL11.glGetFloat(2983, projection);
      GL11.glGetInteger(2978, viewport);
      boolean result = GLU.gluProject((float)x, (float)y, (float)z, modelView, projection, viewport, screenCoords);
      return result ? new double[]{(double)screenCoords.get(0), (double)((float)Display.getHeight() - screenCoords.get(1)), (double)screenCoords.get(2)} : null;
   }
}
