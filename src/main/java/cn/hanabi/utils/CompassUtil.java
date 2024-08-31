package cn.hanabi.utils;

import cn.hanabi.Hanabi;
import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class CompassUtil {
   public static List<Degree> degrees = Lists.newArrayList();
   public float innerWidth;
   public float outerWidth;
   public boolean shadow;
   public float scale;
   public int accuracy;

   public CompassUtil(float i, float o, float s, int a, boolean sh) {
      super();
      this.innerWidth = i;
      this.outerWidth = o;
      this.scale = s;
      this.accuracy = a;
      this.shadow = sh;
      degrees.add(new CompassUtil.Degree("N", 1));
      degrees.add(new CompassUtil.Degree("195", 2));
      degrees.add(new CompassUtil.Degree("210", 2));
      degrees.add(new CompassUtil.Degree("NE", 3));
      degrees.add(new CompassUtil.Degree("240", 2));
      degrees.add(new CompassUtil.Degree("255", 2));
      degrees.add(new CompassUtil.Degree("E", 1));
      degrees.add(new CompassUtil.Degree("285", 2));
      degrees.add(new CompassUtil.Degree("300", 2));
      degrees.add(new CompassUtil.Degree("SE", 3));
      degrees.add(new CompassUtil.Degree("330", 2));
      degrees.add(new CompassUtil.Degree("345", 2));
      degrees.add(new CompassUtil.Degree("S", 1));
      degrees.add(new CompassUtil.Degree("15", 2));
      degrees.add(new CompassUtil.Degree("30", 2));
      degrees.add(new CompassUtil.Degree("SW", 3));
      degrees.add(new CompassUtil.Degree("60", 2));
      degrees.add(new CompassUtil.Degree("75", 2));
      degrees.add(new CompassUtil.Degree("W", 1));
      degrees.add(new CompassUtil.Degree("105", 2));
      degrees.add(new CompassUtil.Degree("120", 2));
      degrees.add(new CompassUtil.Degree("NW", 3));
      degrees.add(new CompassUtil.Degree("150", 2));
      degrees.add(new CompassUtil.Degree("165", 2));
   }

   public static void draw(ScaledResolution sr) {
      preRender(sr);
      float center = (float)(sr.getScaledWidth() / 2);
      int count = 0;
      float yaaahhrewindTime = Minecraft.getMinecraft().thePlayer.rotationYaw % 360.0F * 2.0F + 1080.0F;
      GL11.glPushMatrix();
      GL11.glEnable(3089);
      RenderUtil.doGlScissor(RenderUtil.width() / 2 - 100, 22, 200, 25);

      try {
         for(CompassUtil.Degree d : degrees) {
            float location = center + (float)(count * 30) - yaaahhrewindTime;
            float completeLocation = d.type == 1 ? location - (float)(Hanabi.INSTANCE.fontManager.usans28.getStringWidth(d.text) / 2) : (d.type == 2 ? location - (float)(Hanabi.INSTANCE.fontManager.usans28.getStringWidth(d.text) / 2) : location - (float)(Hanabi.INSTANCE.fontManager.usans22.getStringWidth(d.text) / 2));
            int opacity = opacity(sr, completeLocation);
            if (d.type == 1 && opacity != 16777215) {
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Hanabi.INSTANCE.fontManager.usans28.drawString(d.text, completeLocation, 25.0F, opacity(sr, completeLocation));
            }

            if (d.type == 2 && opacity != 16777215) {
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Gui.drawRect((int)((double)location - 0.5D), 29, (int)((double)location + 0.5D), 34, opacity(sr, completeLocation));
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Hanabi.INSTANCE.fontManager.usans14.drawString(d.text, completeLocation, 37.5F, opacity(sr, completeLocation));
            }

            if (d.type == 3 && opacity != 16777215) {
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Hanabi.INSTANCE.fontManager.usans22.drawString(d.text, completeLocation, (float)(25 + Hanabi.INSTANCE.fontManager.usans28.FONT_HEIGHT / 2 - Hanabi.INSTANCE.fontManager.usans22.FONT_HEIGHT / 2), opacity(sr, completeLocation));
            }

            ++count;
         }

         for(CompassUtil.Degree d : degrees) {
            float location = center + (float)(count * 30) - yaaahhrewindTime;
            float completeLocation = d.type == 1 ? location - (float)(Hanabi.INSTANCE.fontManager.usans28.getStringWidth(d.text) / 2) : (d.type == 2 ? location - (float)(Hanabi.INSTANCE.fontManager.usans14.getStringWidth(d.text) / 2) : location - (float)(Hanabi.INSTANCE.fontManager.usans22.getStringWidth(d.text) / 2));
            if (d.type == 1) {
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Hanabi.INSTANCE.fontManager.usans28.drawString(d.text, completeLocation, 25.0F, opacity(sr, completeLocation));
            }

            if (d.type == 2) {
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Gui.drawRect((int)((double)location - 0.5D), 29, (int)((double)location + 0.5D), 34, opacity(sr, completeLocation));
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Hanabi.INSTANCE.fontManager.usans14.drawString(d.text, completeLocation, 37.5F, opacity(sr, completeLocation));
            }

            if (d.type == 3) {
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Hanabi.INSTANCE.fontManager.usans22.drawString(d.text, completeLocation, (float)(25 + Hanabi.INSTANCE.fontManager.usans28.FONT_HEIGHT / 2 - Hanabi.INSTANCE.fontManager.usans22.FONT_HEIGHT / 2), opacity(sr, completeLocation));
            }

            ++count;
         }

         for(CompassUtil.Degree d : degrees) {
            float location = center + (float)(count * 30) - yaaahhrewindTime;
            float completeLocation = d.type == 1 ? location - (float)(Hanabi.INSTANCE.fontManager.usans28.getStringWidth(d.text) / 2) : (d.type == 2 ? location - (float)(Hanabi.INSTANCE.fontManager.usans14.getStringWidth(d.text) / 2) : location - (float)(Hanabi.INSTANCE.fontManager.usans22.getStringWidth(d.text) / 2));
            if (d.type == 1) {
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Hanabi.INSTANCE.fontManager.usans28.drawString(d.text, completeLocation, 25.0F, opacity(sr, completeLocation));
            }

            if (d.type == 2) {
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Gui.drawRect((int)((double)location - 0.5D), 29, (int)((double)location + 0.5D), 34, opacity(sr, completeLocation));
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Hanabi.INSTANCE.fontManager.usans14.drawString(d.text, completeLocation, 37.5F, opacity(sr, completeLocation));
            }

            if (d.type == 3) {
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Hanabi.INSTANCE.fontManager.usans22.drawString(d.text, completeLocation, (float)(25 + Hanabi.INSTANCE.fontManager.usans28.FONT_HEIGHT / 2 - Hanabi.INSTANCE.fontManager.usans22.FONT_HEIGHT / 2), opacity(sr, completeLocation));
            }

            ++count;
         }
      } catch (Exception var9) {
         var9.printStackTrace();
      }

      GL11.glDisable(3089);
      GL11.glPopMatrix();
   }

   public static void preRender(ScaledResolution sr) {
      GlStateManager.disableAlpha();
      GlStateManager.enableBlend();
   }

   public static int opacity(ScaledResolution sr, float offset) {
      int op = 0;
      float offs = 255.0F - Math.abs((float)(sr.getScaledWidth() / 2) - offset) * 1.8F;
      Color c = new Color(255, 255, 255, (int)Math.min(Math.max(0.0F, offs), 255.0F));
      return c.getRGB();
   }

   public static class Degree {
      public String text;
      public int type;

      public Degree(String s, int t) {
         super();
         this.text = s;
         this.type = t;
      }
   }
}
