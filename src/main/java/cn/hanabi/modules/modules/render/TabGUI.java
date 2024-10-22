package cn.hanabi.modules.modules.render;

import cn.hanabi.Hanabi;
import cn.hanabi.events.EventKey;
import cn.hanabi.events.EventRender2D;
import cn.hanabi.gui.font.noway.ttfr.HFontRenderer;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.modules.ModManager;
import cn.hanabi.utils.RenderUtil;
import cn.hanabi.utils.TimeHelper;
import cn.hanabi.utils.fontmanager.HanabiFonts;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventTarget;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class TabGUI extends Mod {
   private final List<Category> categoryArrayList = Arrays.asList(Category.values());
   private final TimeHelper timer = new TimeHelper();
   public int screen = -1;
   int MAIN = (new Color(47, 116, 253)).getRGB();
   int SECONDARY = (new Color(23, 23, 23)).getRGB();
   double startY = 3.0D;
   private int currentCategoryIndex = 0;
   private int currentModIndex = 0;
   private int currentSettingIndex = 0;
   private boolean editMode = false;

   public TabGUI() {
      super("TabGUI", Category.RENDER);
   }

   public static double roundToPlace(double value, int places) {
      if (places < 0) {
         throw new IllegalArgumentException();
      } else {
         BigDecimal bd = new BigDecimal(value);
         bd = bd.setScale(places, RoundingMode.HALF_UP);
         return bd.doubleValue();
      }
   }

   @EventTarget
   public void renderTabgui(EventRender2D e) {
      this.SECONDARY = (new Color(23, 23, 23)).getRGB();
      if (this.timer.hasReached(5000L) && this.screen != -1) {
         this.screen = -1;
      }

      if (this.screen == -1) {
         this.startY = RenderUtil.getAnimationStateSmooth(-20.0D, this.startY, (double)(10.0F / (float)Minecraft.getDebugFPS()));
      } else {
         this.startY = RenderUtil.getAnimationStateSmooth(70.0D, this.startY, (double)(10.0F / (float)Minecraft.getDebugFPS()));
      }

      GlStateManager.pushMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      int startX = 3;

      for(Category c : this.categoryArrayList) {
         RenderUtil.drawRect((float)startX, (float)this.startY, (float)(startX + 15), (float)this.startY + 15.0F, this.SECONDARY);
         if (this.getCurrentCategory() == c) {
            RenderUtil.drawRect((float)(startX + 1), (float)this.startY + 1.0F, (float)(startX + 14), (float)this.startY + 14.0F, this.MAIN);
         }

         String needDraw = "";
         if (c == Category.COMBAT) {
            needDraw = HanabiFonts.ICON_CLICKGUI_COMBAT;
         }

         if (c == Category.GHOST) {
            needDraw = HanabiFonts.ICON_CLICKGUI_GHOST;
         }

         if (c == Category.MOVEMENT) {
            needDraw = HanabiFonts.ICON_CLICKGUI_MOVEMENT;
         }

         if (c == Category.PLAYER) {
            needDraw = HanabiFonts.ICON_CLICKGUI_PLAYER;
         }

         if (c == Category.RENDER) {
            needDraw = HanabiFonts.ICON_CLICKGUI_RENDER;
         }

         if (c == Category.WORLD) {
            needDraw = HanabiFonts.ICON_CLICKGUI_WORLD;
         }

         HFontRenderer font = Hanabi.INSTANCE.fontManager.icon20;
         font.drawString(needDraw, (float)(startX - 2) + (float)font.getStringWidth(needDraw) / 2.0F, (float)this.startY + 3.0F, -1);
         startX += 16;
      }

      HFontRenderer font = Hanabi.INSTANCE.fontManager.raleway16;
      if (this.screen == 1 || this.screen == 2) {
         int startXMods = 3;
         int startYMods = (int)this.startY + 17;
         RenderUtil.drawRect((float)startXMods, (float)startYMods, (float)(startXMods + this.getWidestMod()), (float)(startYMods + this.getModsForCurrentCategory().size() * 12), this.SECONDARY);
         RenderUtil.drawRect((float)(startXMods + this.currentCategoryIndex * 16), (float)this.startY + 15.0F, (float)(startXMods + this.currentCategoryIndex * 16 + 15), (float)startYMods, this.SECONDARY);

         for(Mod m : this.getModsForCurrentCategory()) {
            int x = startXMods + this.getWidestMod() - 7;
            int y = startYMods + 3 + 1;
            RenderUtil.drawRect((float)x, (float)y, (float)(x + 5), (float)(y + 5), Color.BLACK.getRGB());
            if (this.getCurrentModule() == m) {
               RenderUtil.drawRect((float)startXMods, (float)startYMods, (float)(startXMods + font.getStringWidth(m.getName()) + 9), (float)(startYMods + 12), this.MAIN);
            }

            font.drawString(m.getName(), (float)(startXMods + 5), (float)(startYMods + 1 + 1), -1);
            if (m.getState()) {
               RenderUtil.drawRect((float)(x + 1), (float)(y + 1), (float)(x + 4), (float)(y + 4), this.MAIN);
            }

            startYMods += 12;
         }
      }

      if (this.screen == 2) {
         int startXSettings = 3 + (this.getWidestMod() - 7) + 9;
         int startYSettings = (int)this.startY + 17 + this.currentModIndex * 12;
         RenderUtil.drawRect((float)startXSettings, (float)startYSettings, (float)(startXSettings + this.getWidestSetting() + 2), (float)(startYSettings + this.getSettingsForCurrentModule().size() * 12 - 2), this.SECONDARY);

         for(Value s : this.getSettingsForCurrentModule()) {
            if (this.getCurrentSetting() == s) {
               if (s.isValueMode) {
                  RenderUtil.drawRect((float)startXSettings, (float)startYSettings, (float)(startXSettings + font.getStringWidth(s.getModeTitle() + ": ") + 3), (float)(startYSettings + 9 + 2 - 1), this.MAIN);
               } else {
                  RenderUtil.drawRect((float)startXSettings, (float)startYSettings, (float)(startXSettings + font.getStringWidth(s.getName() + ": ") + 3), (float)(startYSettings + 9 + 2 - 1), this.MAIN);
               }
            }

            if (s.isValueDouble) {
               font.drawString(s.getName() + ": " + (this.editMode && this.getCurrentSetting() == s ? ChatFormatting.WHITE : ChatFormatting.GRAY) + roundToPlace(((Double)s.getValueState()).doubleValue(), 2), (float)(1 + startXSettings + 3), (float)(startYSettings + 1), -1);
            } else if (s.isValueBoolean) {
               font.drawString(s.getName() + ": " + (this.editMode && this.getCurrentSetting() == s ? ChatFormatting.WHITE : ChatFormatting.GRAY) + s.getValueState(), (float)(1 + startXSettings + 3), (float)(startYSettings + 1), -1);
            } else {
               font.drawString(s.getModeTitle() + ": " + (this.editMode && this.getCurrentSetting() == s ? ChatFormatting.WHITE : ChatFormatting.GRAY) + s.mode.get(s.getCurrentMode()), (float)(1 + startXSettings + 3), (float)(startYSettings + 1), -1);
            }

            startYSettings += 12;
         }
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
   }

   @EventTarget
   public void onKeyPress(EventKey eventKeyboard) {
      if (this.renderTabGUI()) {
         switch(eventKeyboard.getKey()) {
         case 28:
            this.timer.reset();
            this.enter();
            break;
         case 200:
            this.timer.reset();
            this.up();
            break;
         case 203:
            this.timer.reset();
            this.left();
            break;
         case 205:
            this.timer.reset();
            this.right();
            break;
         case 208:
            this.timer.reset();
            this.down();
         }

      }
   }

   private void left() {
      if (this.screen == 0) {
         if (this.currentCategoryIndex > 0) {
            --this.currentCategoryIndex;
         } else if (this.currentCategoryIndex == 0) {
            this.currentCategoryIndex = this.categoryArrayList.size() - 1;
         }
      } else if (this.screen == 2) {
         this.currentSettingIndex = 0;
         this.editMode = false;
         this.screen = 1;
      }

   }

   private void right() {
      if (this.screen == 0) {
         if (this.currentCategoryIndex < this.categoryArrayList.size() - 1) {
            ++this.currentCategoryIndex;
         } else {
            this.currentCategoryIndex = 0;
         }
      } else if (this.screen == 1 && !this.getSettingsForCurrentModule().isEmpty()) {
         this.screen = 2;
      }

   }

   private void down() {
      if (this.editMode) {
         Value s = this.getCurrentSetting();
         if (s.isValueDouble) {
            s.setValueState(Double.valueOf(((Double)s.getValueState()).doubleValue() - s.getSteps()));
         } else if (s.isValueMode) {
            s.setCurrentMode(s.getCurrentMode() + 1);
         } else if (s.isValueBoolean) {
            s.setValueState(Boolean.valueOf(!((Boolean)s.getValueState()).booleanValue()));
         }
      } else if (this.screen == -1) {
         this.screen = 0;
      } else if (this.screen == 0) {
         this.screen = 1;
      } else if (this.screen == 1 && this.currentModIndex < this.getModsForCurrentCategory().size() - 1) {
         ++this.currentModIndex;
      } else if (this.screen == 1 && this.currentModIndex == this.getModsForCurrentCategory().size() - 1) {
         this.currentModIndex = 0;
      } else if (this.screen == 2 && this.currentSettingIndex < this.getSettingsForCurrentModule().size() - 1) {
         ++this.currentSettingIndex;
      } else if (this.screen == 2 && this.currentSettingIndex == this.getSettingsForCurrentModule().size() - 1) {
         this.currentSettingIndex = 0;
      }

   }

   private void up() {
      if (this.editMode) {
         Value s = this.getCurrentSetting();
         if (s.isValueDouble) {
            s.setValueState(Double.valueOf(((Double)s.getValueState()).doubleValue() + s.getSteps()));
         } else if (s.isValueMode) {
            s.setCurrentMode(s.getCurrentMode() - 1);
            if (s.getCurrentMode() < 0) {
               s.setCurrentMode(s.mode.size() - 1);
            }
         } else if (s.isValueBoolean) {
            s.setValueState(Boolean.valueOf(!((Boolean)s.getValueState()).booleanValue()));
         }
      } else if (this.screen == 0) {
         this.screen = -1;
      } else if (this.screen == 1 && this.currentModIndex == 0) {
         this.screen = 0;
      } else if (this.screen == 1 && this.currentModIndex > 0) {
         --this.currentModIndex;
      } else if (this.screen == 2 && this.currentSettingIndex > 0) {
         --this.currentSettingIndex;
      } else if (this.screen == 2 && this.currentSettingIndex == 0) {
         this.currentSettingIndex = this.getSettingsForCurrentModule().size() - 1;
      }

   }

   private void enter() {
      if (this.screen == 1) {
         this.getCurrentModule().set(!this.getCurrentModule().getState());
      } else if (this.screen == 2) {
         this.editMode = !this.editMode;
      }

   }

   private boolean renderTabGUI() {
      return !mc.gameSettings.showDebugInfo;
   }

   private Category getCurrentCategory() {
      return (Category)this.categoryArrayList.get(this.currentCategoryIndex);
   }

   private List<Mod> getModsForCurrentCategory() {
      return ModManager.getModules(this.getCurrentCategory());
   }

   private Mod getCurrentModule() {
      return (Mod)this.getModsForCurrentCategory().get(this.currentModIndex);
   }

   private List<Value> getSettingsForCurrentModule() {
      return Value.getValue(this.getCurrentModule());
   }

   private Value getCurrentSetting() {
      return (Value)this.getSettingsForCurrentModule().get(this.currentSettingIndex);
   }

   private int getWidestSetting() {
      int maxWidth = 0;

      for(Value s : this.getSettingsForCurrentModule()) {
         if (s.isValueDouble) {
            int width = mc.fontRendererObj.getStringWidth(s.getName() + ": " + roundToPlace(((Double)s.getValueState()).doubleValue(), 2));
            if (width > maxWidth) {
               maxWidth = width;
            }
         } else if (s.isValueMode) {
            int width = mc.fontRendererObj.getStringWidth(s.getModeTitle() + ": " + s.mode.get(s.getCurrentMode()));
            if (width > maxWidth) {
               maxWidth = width;
            }
         } else {
            int width = mc.fontRendererObj.getStringWidth(s.getName() + ": " + s.getValueState());
            if (width > maxWidth) {
               maxWidth = width;
            }
         }
      }

      return maxWidth;
   }

   private int getWidestMod() {
      int width = this.categoryArrayList.size() * 16;

      for(Mod m : this.getModsForCurrentCategory()) {
         if (mc.fontRendererObj.getStringWidth(m.getName()) + 14 > width) {
            width = mc.fontRendererObj.getStringWidth(m.getName()) + 14;
         }
      }

      return width;
   }
}
