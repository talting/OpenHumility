package cn.hanabi.altmanager;

import cn.hanabi.Hanabi;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;

public class AltFileManager {
   private static final File directory;
   public static ArrayList<Alts> Files = new ArrayList();

   public AltFileManager() {
      super();
      this.makeDirectories();
      Files.add(new Alts("alts", false, true));
   }

   public void loadFiles() {
      for(AltFileManager.CustomFile f : Files) {
         try {
            if (f.loadOnStart()) {
               f.loadFile();
            }
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }

   }

   public void saveFiles() {
      for(AltFileManager.CustomFile f : Files) {
         try {
            f.saveFile();
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }

   }

   public AltFileManager.CustomFile getFile(Class clazz) {
      for(AltFileManager.CustomFile file : Files) {
         if (file.getClass() == clazz) {
            return file;
         }
      }

      return null;
   }

   public void makeDirectories() {
      try {
         if (!directory.exists()) {
            if (directory.mkdir()) {
               Hanabi.INSTANCE.println("Directory is created!");
            } else {
               Hanabi.INSTANCE.println("Failed to create directory!");
            }
         }

      } catch (Exception var2) {
         throw new RuntimeException();
      }
   }

   static {
      directory = new File(Minecraft.getMinecraft().mcDataDir.toString() + "/" + "Humility");
   }

   public abstract static class CustomFile {
      private final File file;
      private final String name;
      private final boolean load;

      public CustomFile(String name, boolean Module2, boolean loadOnStart) {
         super();
         this.name = name;
         this.load = loadOnStart;
         this.file = new File(AltFileManager.directory, name + ".txt");
         if (!this.file.exists()) {
            try {
               this.saveFile();
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }

      }

      public final File getFile() {
         return this.file;
      }

      private boolean loadOnStart() {
         return this.load;
      }

      public final String getName() {
         return this.name;
      }

      public abstract void loadFile() throws IOException;

      public abstract void saveFile() throws IOException;
   }
}
