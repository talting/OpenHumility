package cn.hanabi.modules.modules.render;

import cn.hanabi.Hanabi;
import cn.hanabi.events.EventKey;
import cn.hanabi.events.EventRender2D;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.modules.ModManager;
import cn.hanabi.utils.ClientUtil;
import cn.hanabi.utils.Colors;
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
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class EnvyHUD extends Mod {
   public static final int MAIN = (new Color(33, 170, 47)).getRGB();
   public static final int SECONDARY = (new Color(23, 23, 23)).getRGB();
   private final List<Category> categoryArrayList = Arrays.asList(Category.values());
   private final TimeHelper timer = new TimeHelper();
   public Gui guiInstance = new Gui();
   private int currentCategoryIndex = 0;
   private int currentModIndex = 0;
   private int currentSettingIndex = 0;
   private int screen = -1;
   private boolean editMode = false;

   public EnvyHUD() {
      super("EHUD", Category.RENDER);
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

   public static void drawHorizontalLine(int startX, int endX, int y, int color) {
      if (endX < startX) {
         int i = startX;
         startX = endX;
         endX = i;
      }

      Gui.drawRect(startX, y, endX + 1, y + 1, color);
   }

   public static void drawVerticalLine(int x, int startY, int endY, int color) {
      if (endY < startY) {
         int i = startY;
         startY = endY;
         endY = i;
      }

      Gui.drawRect(x, startY + 1, x + 1, endY, color);
   }

   @EventTarget
   public void onRender(EventRender2D e) {
      if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) {
         GlStateManager.pushMatrix();
         int yStart = 1;
         ScaledResolution sr = new ScaledResolution(mc);
         List<Mod> mods = ModManager.getEnabledModList();
         mods.sort((o1, o2) -> {
            return mc.fontRendererObj.getStringWidth(o2.getDisplayName() == null ? o2.getName() : o2.getName() + "," + o2.getDisplayName()) - mc.fontRendererObj.getStringWidth(o1.getDisplayName() == null ? o1.getName() : o1.getName() + "," + o1.getDisplayName());
         });

         for(Mod module : mods) {
            int startX = sr.getScaledWidth() - mc.fontRendererObj.getStringWidth(module.getDisplayName() == null ? module.getName() : module.getName() + "," + module.getDisplayName()) - 5;
            Gui.drawRect(startX, yStart - 1, sr.getScaledWidth(), yStart + 10, SECONDARY);
            Gui.drawRect(sr.getScaledWidth() - 1, yStart - 1, sr.getScaledWidth(), yStart + 10, MAIN);
            drawVerticalLine(startX - 1, yStart - 2, yStart + 10, MAIN);
            drawHorizontalLine(startX - 1, sr.getScaledWidth() + 1, yStart + 10, MAIN);
            mc.fontRendererObj.drawStringWithShadow(module.getName(), (float)(startX + 3), (float)yStart, MAIN);
            mc.fontRendererObj.drawStringWithShadow(module.getDisplayName(), (float)(startX + 3 + mc.fontRendererObj.getStringWidth(module.getName() + ",")), (float)yStart, Color.WHITE.darker().darker().getRGB());
            yStart += 11;
         }

         GlStateManager.popMatrix();
         RenderUtil.drawRect((float)(sr.getScaledWidth() / 2 - 91 + mc.thePlayer.inventory.currentItem * 20 + 1), (float)(sr.getScaledHeight() - 20), (float)(sr.getScaledWidth() / 2 + 90 - 20 * (8 - mc.thePlayer.inventory.currentItem)), (float)sr.getScaledHeight(), ClientUtil.reAlpha(Colors.WHITE.c, 0.5F));
         RenderHelper.enableGUIStandardItemLighting();

         for(int j = 0; j < 9; ++j) {
            if (j != mc.thePlayer.inventory.currentItem) {
               RenderUtil.drawRect((float)(sr.getScaledWidth() / 2 - 91 + j * 20 + 1), (float)(sr.getScaledHeight() - 20), (float)(sr.getScaledWidth() / 2 + 90 - 20 * (8 - j)), (float)sr.getScaledHeight(), ClientUtil.reAlpha(Colors.BLACK.c, 0.5F));
            }

            int k = sr.getScaledWidth() / 2 - 90 + j * 20 + 2;
            int l = sr.getScaledHeight() - 16 - 2;
            ((HUD)ModManager.getModule("HUD")).customRenderHotbarItem(j, k, l, e.partialTicks, mc.thePlayer);
         }

         GlStateManager.disableBlend();
         GlStateManager.color(1.0F, 1.0F, 1.0F);
         RenderHelper.disableStandardItemLighting();
         ClientUtil.INSTANCE.drawNotifications();
      }
   }

   @EventTarget
   public void renderTabgui(EventRender2D e) {
      if (this.timer.hasReached(5000L) && this.screen == 0) {
         this.screen = -1;
      }

      if (this.screen == -1) {
         Hanabi.INSTANCE.fontManager.icon130.drawStringWithShadow(HanabiFonts.ICON_HANABI_LOGO, 2.0F, 2.0F, MAIN);
      } else {
         GlStateManager.pushMatrix();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         int startX = 3;
         int startY = 3;

         for(Category c : this.categoryArrayList) {
            Gui.drawRect(startX, startY, startX + 15, startY + 15, SECONDARY);
            if (this.getCurrentCategory() == c) {
               Gui.drawRect(startX + 1, startY + 1, startX + 14, startY + 14, MAIN);
            }

            mc.fontRendererObj.drawStringWithShadow(c.name().substring(0, 1), (float)(startX + 8 - mc.fontRendererObj.getStringWidth(c.name().substring(0, 1)) / 2), (float)(startY + 4), -1);
            startX += 16;
         }

         if (this.screen == 1 || this.screen == 2) {
            int startXMods = 3;
            int startYMods = 20;
            Gui.drawRect(startXMods, startYMods, startXMods + this.getWidestMod(), startYMods + this.getModsForCurrentCategory().size() * 12, SECONDARY);
            Gui.drawRect(startXMods + this.currentCategoryIndex * 16, startY + 15, startXMods + this.currentCategoryIndex * 16 + 15, startYMods, SECONDARY);

            for(Mod m : this.getModsForCurrentCategory()) {
               if (this.getCurrentModule() == m) {
                  Gui.drawRect(startXMods, startYMods, startXMods + mc.fontRendererObj.getStringWidth(m.getName()) + 4, startYMods + 12, MAIN);
               }

               int x = startXMods + this.getWidestMod() - 7;
               int y = startYMods + 3 + 1;
               Gui.drawRect(x, y, x + 5, y + 5, -16777216);
               if (m.getState()) {
                  Gui.drawRect(x + 1, y + 1, x + 4, y + 4, MAIN);
               }

               mc.fontRendererObj.drawStringWithShadow(m.getName(), (float)(startXMods + 2), (float)(startYMods + 1 + 1), -1);
               startYMods += 12;
            }
         }

         if (this.screen == 2) {
            int startXSettings = 3 + (this.getWidestMod() - 7) + 9;
            int startYSettings = 20 + this.currentModIndex * 12;
            Gui.drawRect(startXSettings, startYSettings, startXSettings + this.getWidestSetting() + 2, startYSettings + this.getSettingsForCurrentModule().size() * 12 - 2, SECONDARY);

            for(Value s : this.getSettingsForCurrentModule()) {
               if (this.getCurrentSetting() == s) {
                  Gui.drawRect(startXSettings, startYSettings, startXSettings + mc.fontRendererObj.getStringWidth(s.getName() + ": "), startYSettings + 9 + 2 - 1, MAIN);
               }

               if (s.isValueDouble) {
                  mc.fontRendererObj.drawStringWithShadow(s.getName() + ": " + (this.editMode && this.getCurrentSetting() == s ? ChatFormatting.WHITE : ChatFormatting.GRAY) + roundToPlace(((Double)s.getValueState()).doubleValue(), 2), (float)(1 + startXSettings), (float)(startYSettings + 1), -1);
               } else if (s.isValueBoolean) {
                  mc.fontRendererObj.drawStringWithShadow(s.getName() + ": " + (this.editMode && this.getCurrentSetting() == s ? ChatFormatting.WHITE : ChatFormatting.GRAY) + s.getValueState(), (float)(1 + startXSettings), (float)(startYSettings + 1), -1);
               } else {
                  mc.fontRendererObj.drawStringWithShadow(s.getModeTitle() + ": " + (this.editMode && this.getCurrentSetting() == s ? ChatFormatting.WHITE : ChatFormatting.GRAY) + s.mode.get(s.getCurrentMode()), (float)(1 + startXSettings), (float)(startYSettings + 1), -1);
               }

               startYSettings += 12;
            }
         }

         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();
      }
   }

   @EventTarget
   public void armor(EventRender2D e) {
   }

   @EventTarget
   public void potion(EventRender2D e) {
      ScaledResolution sr = new ScaledResolution(mc);
      GlStateManager.pushMatrix();
      int size = 16;
      int yOffset = 10;
      if (Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatOpen()) {
         yOffset = -3;
      }

      float x = (float)(sr.getScaledWidth() - size * 2 + 8);
      float y = (float)(sr.getScaledHeight() - size * 2 + yOffset);
      Collection<PotionEffect> effects = Minecraft.getMinecraft().thePlayer.getActivePotionEffects();
      int i = 0;
      if (!effects.isEmpty()) {
         for(PotionEffect potionEffect : effects) {
            Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
            int potionDuration = potionEffect.getDuration();
            String potionDurationFormatted = Potion.getDurationString(potionEffect);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
            if (potion.hasStatusIcon()) {
               int var9 = potion.getStatusIconIndex();
               this.guiInstance.drawTexturedModalRect((int)x, (int)y - 18 * i, var9 % 8 * 18, 198 + var9 / 8 * 18, 18, 18);
               mc.fontRendererObj.drawStringWithShadow("" + (potionDuration <= 300 ? ChatFormatting.RED : ChatFormatting.WHITE) + Potion.getDurationString(potionEffect), x - (float)mc.fontRendererObj.getStringWidth(potionDurationFormatted) - 5.0F, y - (float)(18 * i) + 6.0F, -1);
               ++i;
            }
         }
      }

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
