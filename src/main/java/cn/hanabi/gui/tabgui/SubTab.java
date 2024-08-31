package cn.hanabi.gui.tabgui;

public class SubTab {
   private String text;
   private SubTab.TabActionListener listener;
   private Object object;

   public SubTab(String text, SubTab.TabActionListener listener, Object object) {
      super();
      this.text = text;
      this.listener = listener;
      this.object = object;
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      this.text = text;
   }

   public SubTab.TabActionListener getListener() {
      return this.listener;
   }

   public void setListener(SubTab.TabActionListener listener) {
      this.listener = listener;
   }

   public Object getObject() {
      return this.object;
   }

   public void setObject(Object object) {
      this.object = object;
   }

   public void press() {
      if (this.listener != null) {
         this.listener.onClick(this);
      }

   }

   public interface TabActionListener {
      void onClick(SubTab var1);
   }
}
