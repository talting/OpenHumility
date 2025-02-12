package cn.hanabi.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

public class IconUtils {
   public IconUtils() {
      super();
   }

   public static ByteBuffer[] getFavicon() {
      try {
         return new ByteBuffer[]{readImageToBuffer(IconUtils.class.getResourceAsStream("/assets/minecraft/Client/icon16.png")), readImageToBuffer(IconUtils.class.getResourceAsStream("/assets/minecraft/Client/icon32.png"))};
      } catch (IOException var1) {
         var1.printStackTrace();
         return null;
      }
   }

   private static ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException {
      if (imageStream == null) {
         return null;
      } else {
         BufferedImage bufferedImage = ImageIO.read(imageStream);
         int[] rgb = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), (int[])null, 0, bufferedImage.getWidth());
         ByteBuffer byteBuffer = ByteBuffer.allocate(4 * rgb.length);

         for(int i : rgb) {
            byteBuffer.putInt(i << 8 | i >> 24 & 255);
         }

         byteBuffer.flip();
         return byteBuffer;
      }
   }
}
