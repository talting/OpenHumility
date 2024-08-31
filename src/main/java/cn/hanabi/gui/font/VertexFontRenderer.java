package cn.hanabi.gui.font;

import cn.hanabi.utils.RenderUtil;
import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class VertexFontRenderer {
   private final Font font;
   private final FontMetrics fontMetrics;
   public final int fontHeight;
   private Map<Character, VertexCache> charMap = new HashMap();

   public VertexFontRenderer(Font font) {
      super();
      this.font = font;
      this.fontMetrics = (new Canvas()).getFontMetrics(font);
      this.fontHeight = ((this.fontMetrics.getHeight() < 0 ? font.getSize() : this.fontMetrics.getHeight() + 3) - 8) / 2;
   }

   public void drawString(String text, float x, float y, int color) {
      this.drawString(text, x, y, color, true);
   }

   public void drawString(String text, float x, float y, int color, boolean matrix) {
      if (matrix) {
         GL11.glPushMatrix();
      }

      GL11.glScalef(0.5F, 0.5F, 0.5F);
      GL11.glTranslated((double)x, (double)y, 0.0D);
      RenderUtil.color(color);

      for(char c : text.toCharArray()) {
         GL11.glTranslatef((float)this.drawChar(c), 0.0F, 0.0F);
      }

      if (matrix) {
         GL11.glPopMatrix();
      }

   }

   public int drawChar(char c) {
      if (this.charMap.containsKey(Character.valueOf(c))) {
         VertexCache vc = (VertexCache)this.charMap.get(Character.valueOf(c));
         vc.render();
         return vc.getWidth();
      } else {
         String charAsString = String.valueOf(c);
         int list = GL11.glGenLists(1);
         int width = this.fontMetrics.stringWidth(charAsString);
         GL11.glNewList(list, 4865);
         RenderUtil.drawAWTShape(this.font.createGlyphVector(new FontRenderContext(new AffineTransform(), true, false), charAsString).getOutline(0.0F, (float)this.fontMetrics.getAscent()), 0.5D);
         GL11.glEndList();
         this.charMap.put(Character.valueOf(c), new VertexCache(c, list, width));
         return width;
      }
   }

   public int getStringWidth(String text) {
      return this.fontMetrics.stringWidth(text) / 2;
   }

   public void gcTick() {
      for(VertexCache cache : (VertexCache[])this.charMap.values().toArray(new VertexCache[0])) {
         if (cache.checkTimeNotUsed(30000L)) {
            cache.destroy();
            this.charMap.remove(Character.valueOf(cache.getChar()));
         }
      }

   }

   public void destroy() {
      for(VertexCache cache : this.charMap.values()) {
         cache.destroy();
      }

      this.charMap.clear();
   }

   public void preGlHint() {
      GlStateManager.enableColorMaterial();
      GlStateManager.enableAlpha();
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GL11.glHint(3155, 4353);
      GL11.glEnable(2881);
      GL11.glDisable(2884);
   }

   public void postGlHint() {
      GL11.glDisable(2881);
      GL11.glEnable(2884);
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();
   }

   public Font getFont() {
      return this.font;
   }
}
