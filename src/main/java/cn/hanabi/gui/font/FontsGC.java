package cn.hanabi.gui.font;

import cn.hanabi.events.EventTick;
import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;

public class FontsGC {
   private static final int GC_TICKS = 200;
   private static int ticks = 0;
   public static final int REMOVE_TIME = 30000;
   private static final ArrayList<VertexFontRenderer> arr = new ArrayList();

   public FontsGC() {
      super();
   }

   @EventTarget
   public void onTick(EventTick event) {
      if (ticks++ > 200) {
         ticks = 0;

         for(VertexFontRenderer fontRenderer : arr) {
            fontRenderer.gcTick();
         }
      }

   }

   public void add(VertexFontRenderer fontRenderer) {
      if (arr.contains(fontRenderer)) {
         throw new IllegalArgumentException("FontRenderer already added!");
      } else {
         arr.add(fontRenderer);
      }
   }

   public void remove(VertexFontRenderer fontRenderer) {
      if (!arr.contains(fontRenderer)) {
         throw new IllegalArgumentException("FontRenderer not added!");
      } else {
         arr.remove(fontRenderer);
      }
   }

   public static void removeAll() {
      arr.clear();
   }

   static {
      EventManager.register(FontsGC.class);
   }
}
