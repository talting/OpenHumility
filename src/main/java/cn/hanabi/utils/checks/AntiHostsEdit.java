package cn.hanabi.utils.checks;

import java.awt.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class AntiHostsEdit {
   public AntiHostsEdit() {
      super();
   }

   public static void run() throws IOException {
      String fileName = "/Windows/System32/drivers/etc/hosts";
      Path path = Paths.get(fileName);
      byte[] bytes = Files.readAllBytes(path);
      List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
      allLines.forEach((line) -> {
         Stream.of("hypixel", "mineplex", "cubecraft").filter((server) -> {
            return line.toLowerCase().contains(server);
         }).forEach((server) -> {
            try {
               Class.forName("javax.swing.JOptionPane").getDeclaredMethod("showMessageDialog", Component.class, Object.class, String.class, Integer.TYPE).invoke(Class.forName("javax.swing.JOptionPane"), null, "Editing your hosts file really? \nDebugging is just skidding with extra work ;)", "Stop", Integer.valueOf(0));
            } catch (Exception var2) {
               ;
            }

         });
      });
   }
}
