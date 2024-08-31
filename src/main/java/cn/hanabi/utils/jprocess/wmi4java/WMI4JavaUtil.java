package cn.hanabi.utils.jprocess.wmi4java;

public final class WMI4JavaUtil {
   public WMI4JavaUtil() {
      super();
   }

   public static String join(String delimiter, Iterable parts) {
      StringBuilder joinedString = new StringBuilder();

      for(Object part : parts) {
         joinedString.append(part);
         joinedString.append(delimiter);
      }

      joinedString.delete(joinedString.length() - delimiter.length(), joinedString.length());
      return joinedString.toString();
   }
}
