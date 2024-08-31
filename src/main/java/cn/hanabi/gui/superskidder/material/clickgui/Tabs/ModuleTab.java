package cn.hanabi.gui.superskidder.material.clickgui.Tabs;

import cn.hanabi.Hanabi;
import cn.hanabi.gui.superskidder.material.clickgui.Main;
import cn.hanabi.gui.superskidder.material.clickgui.Tab;
import cn.hanabi.gui.superskidder.material.clickgui.button.Button;
import cn.hanabi.gui.superskidder.material.clickgui.button.values.BMode;
import cn.hanabi.gui.superskidder.material.clickgui.button.values.BNumbers;
import cn.hanabi.gui.superskidder.material.clickgui.button.values.BOption;
import cn.hanabi.modules.Mod;
import cn.hanabi.value.Value;
import java.util.ArrayList;

public class ModuleTab extends Tab {
   public Mod module;
   private ArrayList<Button> btns = new ArrayList();
   float startX = Main.windowX + 20.0F;
   float startY = Main.windowY + 70.0F;

   public ModuleTab(Mod m) {
      super();
      this.module = m;
      this.name = m.getName();

      for(Value v : Value.getValue(this.module)) {
         if (v.isValueBoolean) {
            Button value = new BOption(this.startX, this.startY, v, this);
            this.btns.add(value);
         } else if (!v.isValueByte && !v.isValueLong && !v.isValueDouble && !v.isValueFloat) {
            if (v.isValueMode) {
               Button value = new BMode(this.startX, this.startY, v, this);
               this.btns.add(value);
            }
         } else {
            Button value = new BNumbers(this.startX, this.startY, v, this);
            this.btns.add(value);
         }
      }

   }

   public void render(float mouseX, float mouseY) {
      this.startX = Main.windowX + 20.0F + Main.animListX;
      this.startY = Main.windowY + 70.0F;

      for(Button v : this.btns) {
         if (!(v instanceof BMode)) {
            v.draw(mouseX, mouseY);
         }

         v.x = this.startX;
         v.y = this.startY;
         if (this.startX + 200.0F < Main.windowX + Main.windowWidth) {
            if (v instanceof BOption) {
               this.startX += (float)(40 + Hanabi.INSTANCE.fontManager.wqy18.getStringWidth(v.v.getName()));
            } else {
               this.startX += (float)Math.max(80, 20 + Hanabi.INSTANCE.fontManager.wqy18.getStringWidth(v.v.getName()));
            }
         } else {
            this.startX = Main.windowX + 20.0F + Main.animListX;
            this.startY += 30.0F;
         }
      }

      this.startX = Main.windowX + 20.0F + Main.animListX;
      this.startY = Main.windowY + 70.0F;

      for(Button v : this.btns) {
         if (v instanceof BMode) {
            v.draw(mouseX, mouseY);
         }

         v.x = this.startX;
         v.y = this.startY;
         if (this.startX + 100.0F + (float)Hanabi.INSTANCE.fontManager.wqy18.getStringWidth(v.v.getName()) < Main.windowX + Main.windowWidth) {
            if (v instanceof BOption) {
               this.startX += (float)(40 + Hanabi.INSTANCE.fontManager.wqy18.getStringWidth(v.v.getName()));
            } else {
               this.startX += 80.0F;
            }
         } else {
            this.startX = Main.windowX + 20.0F + Main.animListX;
            this.startY += 30.0F;
         }
      }

   }

   public void mouseClicked(float mouseX, float mouseY) {
      super.mouseClicked(mouseX, mouseY);
      this.startX = Main.windowX + 20.0F + Main.animListX;
      this.startY = Main.windowY + 70.0F;

      for(Button v : this.btns) {
         v.mouseClick(mouseX, mouseY);
      }

   }
}
