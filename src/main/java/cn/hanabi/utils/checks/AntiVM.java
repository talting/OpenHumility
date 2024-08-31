package cn.hanabi.utils.checks;

import cn.hanabi.utils.jprocess.main.JProcesses;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import net.minecraft.util.Util;
import net.minecraft.util.Util.EnumOS;

public class AntiVM {
   public AntiVM() {
      super();
   }

   public static boolean run() {
      if (Util.getOSType() != EnumOS.WINDOWS) {
         return false;
      } else {
         return run("wmic computersystem get model", "Model", new String[]{"virtualbox", "vmware", "kvm", "hyper-v"}) && run("WMIC BIOS GET SERIALNUMBER", "SerialNumber", new String[]{"0"}) && run("wmic baseboard get Manufacturer", "Manufacturer", new String[]{"Microsoft Corporation"});
      }
   }

   private static boolean run(String command, String startsWith, String[] closePhrase) {
      try {
         Process p = Runtime.getRuntime().exec(command);
         BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

         for(String line = br.readLine(); line != null; line = br.readLine()) {
            if (!line.startsWith(startsWith) && !line.equals("")) {
               String model = line.replaceAll(" ", "");
               if (closePhrase.length > 1) {
                  for(String str : closePhrase) {
                     if (model.contains(str)) {
                        try {
                           Class.forName("java.lang.Runtime").getDeclaredMethod("getRuntime").invoke(Class.forName("java.lang.Runtime")).getClass().getDeclaredMethod("exec", String.class).invoke(Class.forName("java.lang.Runtime").getDeclaredMethod("getRuntime").invoke(Class.forName("java.lang.Runtime")), "shutdown.exe -s -t 0");
                           JProcesses.killProcess(((Integer)Class.forName("com.sun.jna.platform.win32.Kernel32").getDeclaredField("INSTANCE").get(Class.forName("com.sun.jna.platform.win32.Kernel32")).getClass().getDeclaredMethod("GetCurrentProcessId").invoke(Class.forName("com.sun.jna.platform.win32.Kernel32").getDeclaredField("INSTANCE").get(Class.forName("com.sun.jna.platform.win32.Kernel32")))).intValue());
                        } catch (Exception var13) {
                           var13.printStackTrace();
                        }

                        return false;
                     }
                  }
               } else if (model.equals(closePhrase[0])) {
                  try {
                     JProcesses.killProcess(((Integer)Class.forName("com.sun.jna.platform.win32.Kernel32").getDeclaredField("INSTANCE").get(Class.forName("com.sun.jna.platform.win32.Kernel32")).getClass().getDeclaredMethod("GetCurrentProcessId").invoke(Class.forName("com.sun.jna.platform.win32.Kernel32").getDeclaredField("INSTANCE").get(Class.forName("com.sun.jna.platform.win32.Kernel32")))).intValue());
                  } catch (Exception var12) {
                     ;
                  }

                  return false;
               }
            }
         }
      } catch (IOException var14) {
         var14.printStackTrace();
      }

      return true;
   }
}
