package cn.hanabi.gui.newclickui;

import cn.hanabi.Hanabi;
import cn.hanabi.gui.font.noway.ttfr.HFontRenderer;
import cn.hanabi.gui.newclickui.impl.BoolValue;
import cn.hanabi.gui.newclickui.impl.DoubleValue;
import cn.hanabi.gui.newclickui.impl.ModeValue;
import cn.hanabi.gui.newclickui.impl.Panel;
import cn.hanabi.gui.newclickui.misc.ClickEffect;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.utils.BlurUtil;
import cn.hanabi.utils.RenderUtil;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.utils.TranslateUtil;
import cn.hanabi.utils.fontmanager.HanabiFonts;
import cn.hanabi.value.Value;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class ClickUI extends GuiScreen {
   public Panel combat;
   public Panel movement;
   public Panel player;
   public Panel render;
   public Panel world;
   public Panel ghost;
   List<Panel> panels = new ArrayList();
   List<ClickEffect> clickEffects = new ArrayList();
   public static Mod currentMod;
   public static int real = Mouse.getDWheel();
   public static int settingwheel;
   public static TranslateUtil settingtranslate = new TranslateUtil(0.0F, 0.0F);
   public static TranslateUtil animatranslate = new TranslateUtil(0.0F, 0.0F);
   public ArrayList<Value> modBooleanValue = new ArrayList();
   public ArrayList<Value> modModeValue = new ArrayList();
   public ArrayList<Value> modDoubleValue = new ArrayList();
   public static Map booleanValueMap = new HashMap();
   public static Map doubleValueMap = new HashMap();
   public static Map modeValueMap = new HashMap();
   public TimeHelper timer = new TimeHelper();
   public String input;
   public static String searchcontent;
   public static boolean isSearching;

   public ClickUI() {
      super();
      currentMod = null;

      for(int i = 0; i < Category.values().length; ++i) {
         Panel panel = new Panel((float)(10 + 150 * i), 50.0F, 100L * (long)(i + 1), Category.values()[i]);
         this.panels.add(panel);
      }

      this.input = "";
      searchcontent = "";
      isSearching = false;
   }

   public void initGui() {
      if (searchcontent.equalsIgnoreCase("") || searchcontent == null) {
         isSearching = false;
      }

      this.clickEffects.clear();
      if (currentMod == null && !isSearching) {
         for(Panel panel : this.panels) {
            panel.resetAnimation();
         }

         animatranslate.setXY(0.0F, 0.0F);
      }

      Keyboard.enableRepeatEvents(true);
   }

   public void drawScreen(int mouseX, int mouseY, float par) {
      if (this.timer.isDelayComplete(500L)) {
         this.input = this.input.equals("_") ? "" : "_";
         this.timer.reset();
      }

      RenderUtil.drawRect(0.0F, 0.0F, (float)this.width, (float)this.height, (new Color(255, 255, 255, 30)).getRGB());
      real = Mouse.getDWheel();

      for(Panel panel : this.panels) {
         panel.drawShadow((float)mouseX, (float)mouseY);
      }

      RenderUtil.drawRect((float)(this.width / 2 - 60), 0.0F, (float)(this.width / 2 + 60), 20.0F, (new Color(0, 0, 0, 120)).getRGB());
      if (!searchcontent.equalsIgnoreCase("") && searchcontent != null) {
         isSearching = true;
      }

      HFontRenderer font = Hanabi.INSTANCE.fontManager.wqy18;
      GL11.glPushMatrix();
      GL11.glEnable(3089);
      RenderUtil.startGlScissor(this.width / 2 - 55, 0, 110, 20);
      if (isSearching) {
         font.drawString(searchcontent + this.input, (float)Math.min(this.width / 2 - 55, this.width / 2 + 50 - font.getStringWidth(searchcontent)), 4.0F, (new Color(255, 255, 255, 255)).getRGB());
      } else {
         font.drawString("Search", (float)this.width / 2.0F - 55.0F, 4.0F, (new Color(180, 180, 180, 255)).getRGB());
      }

      RenderUtil.stopGlScissor();
      GL11.glDisable(3089);
      GL11.glPopMatrix();
      RenderUtil.drawRect((float)(this.width / 2 + 80), 0.0F, (float)(this.width / 2 + 130), 20.0F, (new Color(0, 0, 0, 120)).getRGB());
      Hanabi.INSTANCE.fontManager.wqy18.drawCenteredString("Reset Gui", (float)(this.width / 2 + 105), 4.0F, (new Color(255, 255, 255, 255)).getRGB());
      Hanabi.INSTANCE.fontManager.usans30.drawCenteredString("Humility Build 3.30227", (float)(this.width / 2), (float)(this.height - 20), (new Color(0, 0, 0, 255)).getRGB());
      if (this.clickEffects.size() > 0) {
         Iterator clickEffectIterator = this.clickEffects.iterator();

         while(clickEffectIterator.hasNext()) {
            ClickEffect clickEffect = (ClickEffect)clickEffectIterator.next();
            clickEffect.draw();
            if (clickEffect.canRemove()) {
               clickEffectIterator.remove();
            }
         }
      }

      BlurUtil.doBlur(7.0F);
      Hanabi.INSTANCE.fontManager.usans30.drawCenteredString("Humility Build 3.30227", (float)(this.width / 2), (float)(this.height - 20), (new Color(255, 255, 255, 255)).getRGB());

      for(Panel panel : this.panels) {
         panel.draw((float)mouseX, (float)mouseY);
      }

      boolean searchHover = isHover((float)mouseX, (float)mouseY, (float)(this.width / 2 - 60), 0.0F, (float)(this.width / 2 + 60), 20.0F) && currentMod == null;
      RenderUtil.drawRect((float)(this.width / 2 - 60), 0.0F, (float)(this.width / 2 + 60), 20.0F, (new Color(0, 0, 0, searchHover ? 80 : 60)).getRGB());
      GL11.glPushMatrix();
      GL11.glEnable(3089);
      RenderUtil.startGlScissor(this.width / 2 - 55, 0, 110, 20);
      if (isSearching) {
         font.drawString(searchcontent + this.input, (float)Math.min(this.width / 2 - 55, this.width / 2 + 50 - font.getStringWidth(searchcontent)), 4.0F, (new Color(255, 255, 255, 255)).getRGB());
      } else {
         font.drawString("Search", (float)(this.width / 2 - 55), 4.0F, (new Color(180, 180, 180, 255)).getRGB());
      }

      RenderUtil.stopGlScissor();
      GL11.glDisable(3089);
      GL11.glPopMatrix();
      boolean resetHover = isHover((float)mouseX, (float)mouseY, (float)(this.width / 2 + 80), 0.0F, (float)(this.width / 2 + 130), 20.0F) && currentMod == null;
      RenderUtil.drawRect((float)(this.width / 2 + 80), 0.0F, (float)(this.width / 2 + 130), 20.0F, (new Color(0, 0, 0, resetHover ? 80 : 60)).getRGB());
      Hanabi.INSTANCE.fontManager.wqy18.drawCenteredString("Reset Gui", (float)(this.width / 2 + 105), 4.0F, (new Color(255, 255, 255, 255)).getRGB());
      if (currentMod != null) {
         animatranslate.interpolate(0.0F, (float)this.height, 0.2F);
      } else {
         animatranslate.interpolate(0.0F, 0.0F, 0.4F);
      }

      if (animatranslate.getY() > 0.0F) {
         float startX = (float)(this.width / 2 - 120);
         float startY = (float)(this.height + this.height / 2 - 140) - animatranslate.getY();
         RenderUtil.drawRoundRect((double)startX, (double)startY, (double)(startX + 240.0F), (double)(startY + 280.0F), 4, (new Color(30, 30, 30, 255)).getRGB());
         BlurUtil.doBlur(7.0F);
         RenderUtil.drawRoundRect((double)startX, (double)startY, (double)(startX + 240.0F), (double)(startY + 280.0F), 4, (new Color(30, 30, 30, 255)).getRGB());
         if (currentMod != null) {
            Hanabi.INSTANCE.fontManager.usans25.drawString(currentMod.getName(), startX + 15.0F, startY + 12.0F, (new Color(255, 255, 255, 255)).getRGB());
            String iconstr = "";
            String var10 = currentMod.getCategory().toString();
            byte var11 = -1;
            switch(var10.hashCode()) {
            case -1901885695:
               if (var10.equals("Player")) {
                  var11 = 2;
               }
               break;
            case -1850724938:
               if (var10.equals("Render")) {
                  var11 = 3;
               }
               break;
            case -39033649:
               if (var10.equals("Movement")) {
                  var11 = 1;
               }
               break;
            case 68778607:
               if (var10.equals("Ghost")) {
                  var11 = 5;
               }
               break;
            case 83766130:
               if (var10.equals("World")) {
                  var11 = 4;
               }
               break;
            case 2024008468:
               if (var10.equals("Combat")) {
                  var11 = 0;
               }
            }

            switch(var11) {
            case 0:
               iconstr = HanabiFonts.ICON_CLICKGUI_COMBAT;
               break;
            case 1:
               iconstr = HanabiFonts.ICON_CLICKGUI_MOVEMENT;
               break;
            case 2:
               iconstr = HanabiFonts.ICON_CLICKGUI_PLAYER;
               break;
            case 3:
               iconstr = HanabiFonts.ICON_CLICKGUI_RENDER;
               break;
            case 4:
               iconstr = HanabiFonts.ICON_CLICKGUI_WORLD;
               break;
            case 5:
               iconstr = HanabiFonts.ICON_CLICKGUI_GHOST;
            }

            Hanabi.INSTANCE.fontManager.icon30.drawString(iconstr, startX + 210.0F, startY + 12.0F, (new Color(255, 255, 255, 255)).getRGB());
            BlurUtil.doBlur(startX + 10.0F, startY + 5.0F, 220.0F, 270.0F, 7.0F, 0.0F, 1.0F);
            this.drawValue((float)mouseX, (float)mouseY, startX, startY);
         }
      }

      if (this.clickEffects.size() > 0) {
         Iterator clickEffectIterator = this.clickEffects.iterator();

         while(clickEffectIterator.hasNext()) {
            ClickEffect clickEffect = (ClickEffect)clickEffectIterator.next();
            clickEffect.draw();
            if (clickEffect.canRemove()) {
               clickEffectIterator.remove();
            }
         }
      }

   }

   public void mouseClicked(int mouseX, int mouseY, int key) {
      ClickEffect clickEffect = new ClickEffect((float)mouseX, (float)mouseY);
      this.clickEffects.add(clickEffect);

      for(Panel panel : this.panels) {
         panel.handleMouseClicked((float)mouseX, (float)mouseY, key);
      }

      boolean searchHover = isHover((float)mouseX, (float)mouseY, (float)(this.width / 2 - 60), 0.0F, (float)(this.width / 2 + 60), 20.0F) && currentMod == null;
      if (searchHover && key == 0) {
         isSearching = true;
      }

      if (!searchHover && key == 0 && (searchcontent.equalsIgnoreCase("") || searchcontent == null)) {
         isSearching = false;
      }

      boolean resetHover = isHover((float)mouseX, (float)mouseY, (float)(this.width / 2 + 80), 0.0F, (float)(this.width / 2 + 130), 20.0F) && currentMod == null;
      if (resetHover) {
         for(Panel panel : this.panels) {
            panel.setXY((float)(10 + 150 * this.panels.indexOf(panel)), 50.0F);
         }

         for(Panel panel : this.panels) {
            panel.resetAnimation();
         }
      }

      if (currentMod != null && key == 0) {
         float startX = (float)(this.width / 2 - 120);
         float startY = (float)(this.height + this.height / 2 - 140) - animatranslate.getY();
         boolean valueHover = isHover((float)mouseX, (float)mouseY, startX, startY, startX + 240.0F, startY + 280.0F);
         if (!valueHover) {
            currentMod = null;
         }
      }

      if (currentMod != null) {
         for(Value values2 : this.modBooleanValue) {
            if (booleanValueMap.containsKey(values2)) {
               BoolValue o = (BoolValue)booleanValueMap.get(values2);
               o.handleMouse((float)mouseX, (float)mouseY, key);
            }
         }

         for(Value values2 : this.modModeValue) {
            if (modeValueMap.containsKey(values2)) {
               ModeValue o = (ModeValue)modeValueMap.get(values2);
               o.handleMouse((float)mouseX, (float)mouseY, key);
            }
         }

         for(Value values2 : this.modDoubleValue) {
            if (doubleValueMap.containsKey(values2)) {
               DoubleValue o = (DoubleValue)doubleValueMap.get(values2);
               o.handleMouse((float)mouseX, (float)mouseY, key);
            }
         }
      }

   }

   public void mouseReleased(int mouseX, int mouseY, int key) {
      for(Panel panel : this.panels) {
         panel.handleMouseReleased((float)mouseX, (float)mouseY, key);
      }

   }

   protected void keyTyped(char typedChar, int keyCode) {
      if (keyCode == 1) {
         this.mc.displayGuiScreen((GuiScreen)null);
      }

      if (ChatAllowedCharacters.isAllowedCharacter(typedChar) && isSearching) {
         searchcontent = searchcontent + typedChar;

         for(Panel panel : this.panels) {
            panel.resetTranslate();
         }
      }

      if (keyCode == 47 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown() && isSearching) {
         searchcontent = searchcontent + GuiScreen.getClipboardString();

         for(Panel panel : this.panels) {
            panel.resetTranslate();
         }
      }

      if (keyCode == 14 && isSearching) {
         int length = searchcontent.length();
         if (length != 0) {
            searchcontent = searchcontent.substring(0, length - 1);

            for(Panel panel : this.panels) {
               panel.resetTranslate();
            }
         }
      }

   }

   public void onGuiClosed() {
      try {
         Hanabi.INSTANCE.fileManager.save();
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      Keyboard.enableRepeatEvents(false);
      super.onGuiClosed();
   }

   public static boolean isHover(float mouseX, float mouseY, float x1, float y1, float x2, float y2) {
      return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
   }

   public void drawValue(float mouseX, float mouseY, float startX, float startY) {
      float vstartY = startY + 40.0F;
      Hanabi.INSTANCE.fontManager.usans25.drawString(currentMod.getName(), startX + 15.0F, startY + 12.0F, (new Color(255, 255, 255, 255)).getRGB());
      String iconstr = "";
      String valueY = currentMod.getCategory().toString();
      byte moduleHeight = -1;
      switch(valueY.hashCode()) {
      case -1901885695:
         if (valueY.equals("Player")) {
            moduleHeight = 2;
         }
         break;
      case -1850724938:
         if (valueY.equals("Render")) {
            moduleHeight = 3;
         }
         break;
      case -39033649:
         if (valueY.equals("Movement")) {
            moduleHeight = 1;
         }
         break;
      case 68778607:
         if (valueY.equals("Ghost")) {
            moduleHeight = 5;
         }
         break;
      case 83766130:
         if (valueY.equals("World")) {
            moduleHeight = 4;
         }
         break;
      case 2024008468:
         if (valueY.equals("Combat")) {
            moduleHeight = 0;
         }
      }

      switch(moduleHeight) {
      case 0:
         iconstr = HanabiFonts.ICON_CLICKGUI_COMBAT;
         break;
      case 1:
         iconstr = HanabiFonts.ICON_CLICKGUI_MOVEMENT;
         break;
      case 2:
         iconstr = HanabiFonts.ICON_CLICKGUI_PLAYER;
         break;
      case 3:
         iconstr = HanabiFonts.ICON_CLICKGUI_RENDER;
         break;
      case 4:
         iconstr = HanabiFonts.ICON_CLICKGUI_WORLD;
         break;
      case 5:
         iconstr = HanabiFonts.ICON_CLICKGUI_GHOST;
      }

      Hanabi.INSTANCE.fontManager.icon30.drawString(iconstr, startX + 210.0F, startY + 12.0F, (new Color(255, 255, 255, 255)).getRGB());
      float valueY2 = settingtranslate.getX();
      if (!this.modBooleanValue.isEmpty()) {
         this.modBooleanValue.clear();
      }

      if (!this.modModeValue.isEmpty()) {
         this.modModeValue.clear();
      }

      if (!this.modDoubleValue.isEmpty()) {
         this.modDoubleValue.clear();
      }

      for(Value values : Value.list) {
         if (values.getValueName().split("_")[0].equalsIgnoreCase(currentMod.getName())) {
            Mod curMod = currentMod;
            ++curMod.valueSize;
            if (values.isValueDouble) {
               this.modDoubleValue.add(values);
            }

            if (values.isValueMode) {
               this.modModeValue.add(values);
            }

            if (values.isValueBoolean) {
               this.modBooleanValue.add(values);
            }
         }
      }

      GL11.glPushMatrix();
      RenderUtil.startGlScissor((int)startX, (int)vstartY, 240, 235);

      for(Value values4 : this.modModeValue) {
         ModeValue o;
         if (modeValueMap.containsKey(values4)) {
            o = (ModeValue)modeValueMap.get(values4);
         } else {
            o = new ModeValue(values4);
            modeValueMap.put(values4, o);
         }

         o.draw(startX, vstartY + valueY2, mouseX, mouseY);
         valueY2 += o.getLength();
      }

      for(Value values3 : this.modDoubleValue) {
         DoubleValue o;
         if (doubleValueMap.containsKey(values3)) {
            o = (DoubleValue)doubleValueMap.get(values3);
         } else {
            o = new DoubleValue(values3);
            doubleValueMap.put(values3, o);
         }

         o.draw(startX, vstartY + valueY2, mouseX, mouseY);
         o.handleMouseinRender(mouseX, mouseY, 1);
         valueY2 += o.getLength();
      }

      for(Value values2 : this.modBooleanValue) {
         BoolValue o;
         if (booleanValueMap.containsKey(values2)) {
            o = (BoolValue)booleanValueMap.get(values2);
         } else {
            o = new BoolValue(values2);
            booleanValueMap.put(values2, o);
         }

         o.draw(startX, vstartY + valueY2, mouseX, mouseY);
         valueY2 += o.getLength();
      }

      RenderUtil.stopGlScissor();
      GL11.glPopMatrix();
      float moduleHeight2 = valueY2 - settingtranslate.getX() - 1.0F;
      if (Mouse.hasWheel() && isHover(mouseX, mouseY, startX, startY, startX + 240.0F, vstartY + 235.0F) && currentMod != null) {
         if (real > 0 && settingwheel < 0) {
            for(int i = 0; i < 10 && settingwheel < 0; ++i) {
               settingwheel += 5;
            }
         } else {
            for(int i = 0; i < 10 && real < 0 && moduleHeight2 > 235.0F && (float)Math.abs(settingwheel) < moduleHeight2 - 235.0F; ++i) {
               settingwheel -= 5;
            }
         }
      }

      settingtranslate.interpolate((float)settingwheel, 0.0F, 0.2F);
      float sliderh = Math.min(235.0F, 55225.0F / moduleHeight2);
      float slidert = -(235.0F - sliderh) * settingtranslate.getX() / (moduleHeight2 - 235.0F);
      if (sliderh < 235.0F) {
         GL11.glPushMatrix();
         GL11.glEnable(3089);
         RenderUtil.doGlScissor((int)startX + 229, (int)vstartY, 1, 235);
         RenderUtil.drawRect(startX + 229.0F, vstartY + slidert, startX + 230.0F, vstartY + slidert + sliderh, (new Color(255, 255, 255, 255)).getRGB());
         GL11.glDisable(3089);
         GL11.glPopMatrix();
      }

   }
}
