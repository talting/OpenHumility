package cn.hanabi.altmanager;

import java.util.ArrayList;

public class AltManager {
   public static Alt lastAlt;
   public static ArrayList<Alt> registry = new ArrayList();

   public AltManager() {
      super();
   }

   public ArrayList<Alt> getRegistry() {
      return registry;
   }

   public void setLastAlt(Alt alt) {
      lastAlt = alt;
   }
}
