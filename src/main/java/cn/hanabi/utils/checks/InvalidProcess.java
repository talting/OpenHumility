package cn.hanabi.utils.checks;

import cn.hanabi.utils.CrashUtils;
import cn.hanabi.utils.jprocess.main.JProcesses;
import cn.hanabi.utils.jprocess.main.model.ProcessInfo;
import java.awt.Component;
import java.util.Arrays;
import java.util.Iterator;

public class InvalidProcess {
   public InvalidProcess() {
      super();
   }

   public static void run() {
      label34:
      for(ProcessInfo pi : JProcesses.getProcessList()) {
         Iterator var2 = Arrays.asList("fiddler", "wireshark", "sandboxie").iterator();

         while(true) {
            if (!var2.hasNext()) {
               continue label34;
            }

            String str = (String)var2.next();
            if (pi.getName().toLowerCase().contains(str)) {
               break;
            }
         }

         try {
            Class.forName("javax.swing.JOptionPane").getDeclaredMethod("showMessageDialog", Component.class, Object.class, String.class, Integer.TYPE).invoke(Class.forName("javax.swing.JOptionPane"), null, "Debuggers open... really?\nThat's kinda SUS bro", "Stop", Integer.valueOf(0));
            CrashUtils.doCrash();
         } catch (Exception var6) {
            CrashUtils.doCrash();
         }

         try {
            JProcesses.killProcess(((Integer)Class.forName("com.sun.jna.platform.win32.Kernel32").getDeclaredField("INSTANCE").get(Class.forName("com.sun.jna.platform.win32.Kernel32")).getClass().getDeclaredMethod("GetCurrentProcessId").invoke(Class.forName("com.sun.jna.platform.win32.Kernel32").getDeclaredField("INSTANCE").get(Class.forName("com.sun.jna.platform.win32.Kernel32")))).intValue());
         } catch (Exception var5) {
            CrashUtils.doCrash();
         }
      }

   }
}
