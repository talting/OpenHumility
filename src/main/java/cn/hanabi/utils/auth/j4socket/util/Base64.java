package cn.hanabi.utils.auth.j4socket.util;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPOutputStream;

public class Base64 {
   public static final int NO_OPTIONS = 0;
   public static final int ENCODE = 1;
   public static final int GZIP = 2;
   public static final int DO_BREAK_LINES = 8;
   public static final int URL_SAFE = 16;
   public static final int ORDERED = 32;
   private static final int MAX_LINE_LENGTH = 76;
   private static final byte EQUALS_SIGN = 61;
   private static final byte NEW_LINE = 10;
   private static final String PREFERRED_ENCODING = "US-ASCII";
   private static final byte WHITE_SPACE_ENC = -5;
   private static final byte[] _STANDARD_ALPHABET = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
   private static final byte[] _STANDARD_DECODABET = new byte[]{-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, -9, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, -9, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};
   private static final byte[] _URL_SAFE_ALPHABET = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95};
   private static final byte[] _URL_SAFE_DECODABET = new byte[]{-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, 63, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};
   private static final byte[] _ORDERED_ALPHABET = new byte[]{45, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 95, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122};
   private static final byte[] _ORDERED_DECODABET = new byte[]{-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 0, -9, -9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -9, -9, -9, -1, -9, -9, -9, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, -9, -9, -9, -9, 37, -9, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};

   private static final byte[] getAlphabet(int options) {
      if ((options & 16) == 16) {
         return _URL_SAFE_ALPHABET;
      } else {
         return (options & 32) == 32 ? _ORDERED_ALPHABET : _STANDARD_ALPHABET;
      }
   }

   private static final byte[] getDecodabet(int options) {
      if ((options & 16) == 16) {
         return _URL_SAFE_DECODABET;
      } else {
         return (options & 32) == 32 ? _ORDERED_DECODABET : _STANDARD_DECODABET;
      }
   }

   private Base64() {
      super();
   }

   private static byte[] encode3to4(byte[] b4, byte[] threeBytes, int numSigBytes, int options) {
      encode3to4(threeBytes, 0, numSigBytes, b4, 0, options);
      return b4;
   }

   private static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset, int options) {
      byte[] ALPHABET = getAlphabet(options);
      int inBuff = (numSigBytes > 0 ? source[srcOffset] << 24 >>> 8 : 0) | (numSigBytes > 1 ? source[srcOffset + 1] << 24 >>> 16 : 0) | (numSigBytes > 2 ? source[srcOffset + 2] << 24 >>> 24 : 0);
      switch(numSigBytes) {
      case 1:
         destination[destOffset] = ALPHABET[inBuff >>> 18];
         destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 63];
         destination[destOffset + 2] = 61;
         destination[destOffset + 3] = 61;
         return destination;
      case 2:
         destination[destOffset] = ALPHABET[inBuff >>> 18];
         destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 63];
         destination[destOffset + 2] = ALPHABET[inBuff >>> 6 & 63];
         destination[destOffset + 3] = 61;
         return destination;
      case 3:
         destination[destOffset] = ALPHABET[inBuff >>> 18];
         destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 63];
         destination[destOffset + 2] = ALPHABET[inBuff >>> 6 & 63];
         destination[destOffset + 3] = ALPHABET[inBuff & 63];
         return destination;
      default:
         return destination;
      }
   }

   public static String encodeBytes(byte[] source) {
      String encoded = null;

      try {
         encoded = encodeBytes(source, 0, source.length, 0);
      } catch (IOException var3) {
         assert false : var3.getMessage();
      }

      assert encoded != null;

      return encoded;
   }

   public static String encodeBytes(byte[] source, int off, int len, int options) throws IOException {
      byte[] encoded = encodeBytesToBytes(source, off, len, options);

      try {
         return new String(encoded, "US-ASCII");
      } catch (UnsupportedEncodingException var6) {
         return new String(encoded);
      }
   }

   public static byte[] encodeBytesToBytes(byte[] source, int off, int len, int options) throws IOException {
      if (source == null) {
         throw new IllegalArgumentException("Cannot serialize a null array.");
      } else if (off < 0) {
         throw new IllegalArgumentException("Cannot have negative offset: " + off);
      } else if (len < 0) {
         throw new IllegalArgumentException("Cannot have length offset: " + len);
      } else if (off + len > source.length) {
         throw new IllegalArgumentException(String.format("Cannot have offset of %d and length of %d with array of length %d", off, len, source.length));
      } else if ((options & 2) != 0) {
         ByteArrayOutputStream baos = null;
         GZIPOutputStream gzos = null;
         Base64.OutputStream b64os = null;

         try {
            baos = new ByteArrayOutputStream();
            b64os = new Base64.OutputStream(baos, 1 | options);
            gzos = new GZIPOutputStream(b64os);
            gzos.write(source, off, len);
            gzos.close();
         } catch (IOException var23) {
            throw var23;
         } finally {
            try {
               if (gzos != null) {
                  gzos.close();
               }
            } catch (Exception var22) {
               ;
            }

            try {
               if (b64os != null) {
                  b64os.close();
               }
            } catch (Exception var21) {
               ;
            }

            try {
               if (baos != null) {
                  baos.close();
               }
            } catch (Exception var20) {
               ;
            }

         }

         return baos.toByteArray();
      } else {
         boolean breakLines = (options & 8) != 0;
         int encLen = len / 3 * 4 + (len % 3 > 0 ? 4 : 0);
         if (breakLines) {
            encLen += encLen / 76;
         }

         byte[] outBuff = new byte[encLen];
         int d = 0;
         int e = 0;
         int len2 = len - 2;

         for(int lineLength = 0; d < len2; e += 4) {
            encode3to4(source, d + off, 3, outBuff, e, options);
            lineLength += 4;
            if (breakLines && lineLength >= 76) {
               outBuff[e + 4] = 10;
               ++e;
               lineLength = 0;
            }

            d += 3;
         }

         if (d < len) {
            encode3to4(source, d + off, len - d, outBuff, e, options);
            e += 4;
         }

         if (e <= outBuff.length - 1) {
            byte[] finalOut = new byte[e];
            System.arraycopy(outBuff, 0, finalOut, 0, e);
            return finalOut;
         } else {
            return outBuff;
         }
      }
   }

   private static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset, int options) {
      if (source == null) {
         throw new IllegalArgumentException("Source array was null.");
      } else if (destination == null) {
         throw new IllegalArgumentException("Destination array was null.");
      } else if (srcOffset >= 0 && srcOffset + 3 < source.length) {
         if (destOffset >= 0 && destOffset + 2 < destination.length) {
            byte[] DECODABET = getDecodabet(options);
            if (source[srcOffset + 2] == 61) {
               int outBuff = (DECODABET[source[srcOffset]] & 255) << 18 | (DECODABET[source[srcOffset + 1]] & 255) << 12;
               destination[destOffset] = (byte)(outBuff >>> 16);
               return 1;
            } else if (source[srcOffset + 3] == 61) {
               int outBuff = (DECODABET[source[srcOffset]] & 255) << 18 | (DECODABET[source[srcOffset + 1]] & 255) << 12 | (DECODABET[source[srcOffset + 2]] & 255) << 6;
               destination[destOffset] = (byte)(outBuff >>> 16);
               destination[destOffset + 1] = (byte)(outBuff >>> 8);
               return 2;
            } else {
               int outBuff = (DECODABET[source[srcOffset]] & 255) << 18 | (DECODABET[source[srcOffset + 1]] & 255) << 12 | (DECODABET[source[srcOffset + 2]] & 255) << 6 | DECODABET[source[srcOffset + 3]] & 255;
               destination[destOffset] = (byte)(outBuff >> 16);
               destination[destOffset + 1] = (byte)(outBuff >> 8);
               destination[destOffset + 2] = (byte)outBuff;
               return 3;
            }
         } else {
            throw new IllegalArgumentException(String.format("Destination array with length %d cannot have offset of %d and still store three bytes.", destination.length, destOffset));
         }
      } else {
         throw new IllegalArgumentException(String.format("Source array with length %d cannot have offset of %d and still process four bytes.", source.length, srcOffset));
      }
   }

   public static class OutputStream extends FilterOutputStream {
      private boolean encode;
      private int position;
      private byte[] buffer;
      private int bufferLength;
      private int lineLength;
      private boolean breakLines;
      private byte[] b4;
      private boolean suspendEncoding;
      private int options;
      private byte[] decodabet;

      public OutputStream(java.io.OutputStream out) {
         this(out, 1);
      }

      public OutputStream(java.io.OutputStream out, int options) {
         super(out);
         this.breakLines = (options & 8) != 0;
         this.encode = (options & 1) != 0;
         this.bufferLength = this.encode ? 3 : 4;
         this.buffer = new byte[this.bufferLength];
         this.position = 0;
         this.lineLength = 0;
         this.suspendEncoding = false;
         this.b4 = new byte[4];
         this.options = options;
         this.decodabet = Base64.getDecodabet(options);
      }

      public void write(int theByte) throws IOException {
         if (this.suspendEncoding) {
            this.out.write(theByte);
         } else {
            if (this.encode) {
               this.buffer[this.position++] = (byte)theByte;
               if (this.position >= this.bufferLength) {
                  this.out.write(Base64.encode3to4(this.b4, this.buffer, this.bufferLength, this.options));
                  this.lineLength += 4;
                  if (this.breakLines && this.lineLength >= 76) {
                     this.out.write(10);
                     this.lineLength = 0;
                  }

                  this.position = 0;
               }
            } else if (this.decodabet[theByte & 127] > -5) {
               this.buffer[this.position++] = (byte)theByte;
               if (this.position >= this.bufferLength) {
                  int len = Base64.decode4to3(this.buffer, 0, this.b4, 0, this.options);
                  this.out.write(this.b4, 0, len);
                  this.position = 0;
               }
            } else if (this.decodabet[theByte & 127] != -5) {
               throw new IOException("Invalid character in Base64 data.");
            }

         }
      }

      public void write(byte[] theBytes, int off, int len) throws IOException {
         if (this.suspendEncoding) {
            this.out.write(theBytes, off, len);
         } else {
            for(int i = 0; i < len; ++i) {
               this.write(theBytes[off + i]);
            }

         }
      }

      public void flushBase64() throws IOException {
         if (this.position > 0) {
            if (!this.encode) {
               throw new IOException("Base64 input not properly padded.");
            }

            this.out.write(Base64.encode3to4(this.b4, this.buffer, this.position, this.options));
            this.position = 0;
         }

      }

      public void close() throws IOException {
         this.flushBase64();
         super.close();
         this.buffer = null;
         this.out = null;
      }
   }
}