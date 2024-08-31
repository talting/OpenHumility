package cn.hanabi.utils;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import sun.management.VMManagement;
import sun.misc.Unsafe;

public class AntiDump {
   private static final Unsafe unsafe;
   private static Method findNative;
   private static ClassLoader classLoader;
   public static boolean ENABLE;
   private static final String[] naughtyFlags = new String[]{"-XBootclasspath", "-javaagent", "-Xdebug", "-agentlib", "-Xrunjdwp", "-Xnoagent", "-verbose", "-DproxySet", "-DproxyHost", "-DproxyPort", "-Djavax.net.ssl.trustStore", "-Djavax.net.ssl.trustStorePassword"};

   public AntiDump() {
      super();
   }

   public static void check() {
      if (ENABLE) {
         try {
            Field jvmField = ManagementFactory.getRuntimeMXBean().getClass().getDeclaredField("jvm");
            jvmField.setAccessible(true);
            VMManagement jvm = (VMManagement)jvmField.get(ManagementFactory.getRuntimeMXBean());
            List<String> inputArguments = jvm.getVmArguments();

            for(String arg : naughtyFlags) {
               for(String inputArgument : inputArguments) {
                  if (inputArgument.contains(arg)) {
                     System.out.println("Found illegal program arguments!");
                     dumpDetected();
                  }
               }
            }

            try {
               byte[] bytes = createDummyClass("java/lang/instrument/Instrumentation");
               unsafe.defineClass("java.lang.instrument.Instrumentation", bytes, 0, bytes.length, (ClassLoader)null, (ProtectionDomain)null);
            } catch (Throwable var9) {
               var9.printStackTrace();
               dumpDetected();
            }

            if (isClassLoaded("sun.instrument.InstrumentationImpl")) {
               System.out.println("Found sun.instrument.InstrumentationImpl!");
               dumpDetected();
            }

            byte[] bytes = createDummyClass("dummy/class/path/MaliciousClassFilter");
            unsafe.defineClass("dummy.class.path.MaliciousClassFilter", bytes, 0, bytes.length, (ClassLoader)null, (ProtectionDomain)null);
            System.setProperty("sun.jvm.hotspot.tools.jcore.filter", "dummy.class.path.MaliciousClassFilter");
            disassembleStruct();
         } catch (Throwable var10) {
            var10.printStackTrace();
            dumpDetected();
         }

      }
   }

   private static boolean isClassLoaded(String clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
      Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
      m.setAccessible(true);
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      ClassLoader scl = ClassLoader.getSystemClassLoader();
      return m.invoke(cl, clazz) != null || m.invoke(scl, clazz) != null;
   }

   private static byte[] createDummyClass(String name) {
      ClassNode classNode = new ClassNode();
      classNode.name = name.replace('.', '/');
      classNode.access = 1;
      classNode.version = 52;
      classNode.superName = "java/lang/Object";
      List methods = new ArrayList();
      MethodNode methodNode = new MethodNode(9, "<clinit>", "()V", (String)null, (String[])null);
      InsnList insn = new InsnList();
      insn.add(new FieldInsnNode(178, "java/lang/System", "out", "Ljava/io/PrintStream;"));
      insn.add(new LdcInsnNode("Nice try"));
      insn.add(new MethodInsnNode(182, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
      insn.add(new TypeInsnNode(187, "java/lang/Throwable"));
      insn.add(new InsnNode(89));
      insn.add(new LdcInsnNode("owned"));
      insn.add(new MethodInsnNode(183, "java/lang/Throwable", "<init>", "(Ljava/lang/String;)V", false));
      insn.add(new InsnNode(191));
      methodNode.instructions = insn;
      methods.add(methodNode);
      classNode.methods = methods;
      ClassWriter classWriter = new ClassWriter(2);
      classNode.accept(classWriter);
      return classWriter.toByteArray();
   }

   private static void dumpDetected() {
      try {
         unsafe.putAddress(0L, 0L);
      } catch (Exception var1) {
         ;
      }

      FMLCommonHandler.instance().exitJava(0, false);
      Error error = new Error();
      error.setStackTrace(new StackTraceElement[0]);
      throw error;
   }

   private static void resolveClassLoader() throws NoSuchMethodException {
      String os = System.getProperty("os.name").toLowerCase();
      if (os.contains("windows")) {
         String vmName = System.getProperty("java.vm.name");
         String dll = vmName.contains("Client VM") ? "/bin/client/jvm.dll" : "/bin/server/jvm.dll";

         try {
            System.load(System.getProperty("java.home") + dll);
         } catch (UnsatisfiedLinkError var5) {
            throw new RuntimeException(var5);
         }

         classLoader = AntiDump.class.getClassLoader();
      } else {
         classLoader = null;
      }

      findNative = ClassLoader.class.getDeclaredMethod("findNative", ClassLoader.class, String.class);

      try {
         Class cls = ClassLoader.getSystemClassLoader().loadClass("jdk.internal.module.IllegalAccessLogger");
         Field logger = cls.getDeclaredField("logger");
         unsafe.putObjectVolatile(cls, unsafe.staticFieldOffset(logger), (Object)null);
      } catch (Throwable var4) {
         ;
      }

      findNative.setAccessible(true);
   }

   private static void setupIntrospection() throws Throwable {
      resolveClassLoader();
   }

   public static void disassembleStruct() {
      try {
         setupIntrospection();
         long entry = getSymbol("gHotSpotVMStructs");
         unsafe.putLong(entry, 0L);
      } catch (Throwable var2) {
         var2.printStackTrace();
         dumpDetected();
      }

   }

   private static long getSymbol(String symbol) throws InvocationTargetException, IllegalAccessException {
      long address = ((Long)findNative.invoke((Object)null, classLoader, symbol)).longValue();
      if (address == 0L) {
         throw new NoSuchElementException(symbol);
      } else {
         return unsafe.getLong(address);
      }
   }

   private static String getString(long addr) {
      if (addr == 0L) {
         return null;
      } else {
         char[] chars = new char[40];

         int offset;
         byte b;
         for(offset = 0; (b = unsafe.getByte(addr + (long)offset)) != 0; chars[offset++] = (char)b) {
            if (offset >= chars.length) {
               chars = Arrays.copyOf(chars, offset * 2);
            }
         }

         return new String(chars, 0, offset);
      }
   }

   private static void readStructs(Map structs) throws InvocationTargetException, IllegalAccessException {
      long entry = getSymbol("gHotSpotVMStructs");
      long typeNameOffset = getSymbol("gHotSpotVMStructEntryTypeNameOffset");
      long fieldNameOffset = getSymbol("gHotSpotVMStructEntryFieldNameOffset");
      long typeStringOffset = getSymbol("gHotSpotVMStructEntryTypeStringOffset");
      long isStaticOffset = getSymbol("gHotSpotVMStructEntryIsStaticOffset");
      long offsetOffset = getSymbol("gHotSpotVMStructEntryOffsetOffset");
      long addressOffset = getSymbol("gHotSpotVMStructEntryAddressOffset");
      long arrayStride = getSymbol("gHotSpotVMStructEntryArrayStride");

      while(true) {
         String typeName = getString(unsafe.getLong(entry + typeNameOffset));
         String fieldName = getString(unsafe.getLong(entry + fieldNameOffset));
         if (fieldName == null) {
            long address = ((Long)findNative.invoke((Object)null, classLoader, Integer.valueOf(2))).longValue();
            if (address == 0L) {
               throw new NoSuchElementException("");
            }

            unsafe.getLong(address);
            return;
         }

         String typeString = getString(unsafe.getLong(entry + typeStringOffset));
         boolean isStatic = unsafe.getInt(entry + isStaticOffset) != 0;
         long offset = unsafe.getLong(entry + (isStatic ? addressOffset : offsetOffset));
         Set fields = (Set)structs.get(typeName);
         if (fields == null) {
            structs.put(typeName, fields = new HashSet());
         }

         fields.add(new Object[]{fieldName, typeString, offset, isStatic});
         entry += arrayStride;
      }
   }

   private static void readTypes(Map types, Map structs) throws InvocationTargetException, IllegalAccessException {
      long entry = getSymbol("gHotSpotVMTypes");
      long typeNameOffset = getSymbol("gHotSpotVMTypeEntryTypeNameOffset");
      long superclassNameOffset = getSymbol("gHotSpotVMTypeEntrySuperclassNameOffset");
      long isOopTypeOffset = getSymbol("gHotSpotVMTypeEntryIsOopTypeOffset");
      long isIntegerTypeOffset = getSymbol("gHotSpotVMTypeEntryIsIntegerTypeOffset");
      long isUnsignedOffset = getSymbol("gHotSpotVMTypeEntryIsUnsignedOffset");
      long sizeOffset = getSymbol("gHotSpotVMTypeEntrySizeOffset");
      long arrayStride = getSymbol("gHotSpotVMTypeEntryArrayStride");

      while(true) {
         String typeName = getString(unsafe.getLong(entry + typeNameOffset));
         if (typeName == null) {
            return;
         }

         String superclassName = getString(unsafe.getLong(entry + superclassNameOffset));
         boolean isOop = unsafe.getInt(entry + isOopTypeOffset) != 0;
         boolean isInt = unsafe.getInt(entry + isIntegerTypeOffset) != 0;
         boolean isUnsigned = unsafe.getInt(entry + isUnsignedOffset) != 0;
         int size = unsafe.getInt(entry + sizeOffset);
         Set fields = (Set)structs.get(typeName);
         types.put(typeName, new Object[]{typeName, superclassName, size, isOop, isInt, isUnsigned, fields});
         entry += arrayStride;
      }
   }

   static {
      Unsafe ref;
      try {
         Class clazz = Class.forName("sun.misc.Unsafe");
         Field theUnsafe = clazz.getDeclaredField("theUnsafe");
         theUnsafe.setAccessible(true);
         ref = (Unsafe)theUnsafe.get((Object)null);
      } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException var3) {
         var3.printStackTrace();
         ref = null;
      }

      unsafe = ref;
   }
}
