package cn.hanabi.gui.tabgui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class Tab {
   @NotNull
   private final List<SubTab> subTabs = new ArrayList();
   private String text;

   public Tab(String text) {
      super();
      this.text = text;
   }

   public void addSubTab(SubTab subTab) {
      this.subTabs.add(subTab);
   }

   @NotNull
   public List<SubTab> getSubTabs() {
      return this.subTabs;
   }

   public void renderSubTabs(int x, int y, int selectedSubTab) {
      GL11.glTranslated((double)x, (double)y, 0.0D);
      FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
      int height = (font.FONT_HEIGHT + 3) * this.subTabs.size();
      int width = 0;

      for(SubTab tab : this.subTabs) {
         if (font.getStringWidth(tab.getText()) > width) {
            width = font.getStringWidth(tab.getText());
         }
      }

      width = width + 4;
      TabGui.drawRect(7, 0, 0, width, height, TabGui.BACKGROUND.getRGB());
      GL11.glLineWidth(1.0F);
      TabGui.drawRect(2, 0, 0, width, height, TabGui.BORDER.getRGB());
      int offset = 2;
      int i = 0;

      for(SubTab tab : this.subTabs) {
         if (selectedSubTab == i) {
            TabGui.drawRect(7, 0, offset - 2, width, offset + font.FONT_HEIGHT + 3 - 1, TabGui.SELECTED.getRGB());
         }

         font.drawString(tab.getText(), 2, offset, TabGui.FOREGROUND.getRGB());
         offset += font.FONT_HEIGHT + 3;
         ++i;
      }

      GL11.glTranslated((double)(-x), (double)(-y), 0.0D);
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      this.text = text;
   }
}
