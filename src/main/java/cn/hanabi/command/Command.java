package cn.hanabi.command;

import com.darkmagician6.eventapi.EventManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

public abstract class Command {
   private final String name;
   private final String[] aliases;
   protected static final Minecraft mc = Minecraft.getMinecraft();

   protected Command(String name, String... aliases) {
      super();
      this.name = name;
      this.aliases = aliases;
      EventManager.register(this);
   }

   public abstract void run(String var1, String[] var2);

   public abstract List autocomplete(int var1, String[] var2);

   public boolean match(String name) {
      for(String alias : this.aliases) {
         if (alias.equalsIgnoreCase(name)) {
            return true;
         }
      }

      return this.name.equalsIgnoreCase(name);
   }

   @NotNull
   List getNameAndAliases() {
      List l = new ArrayList();
      l.add(this.name);
      l.addAll(Arrays.asList(this.aliases));
      return l;
   }
}
