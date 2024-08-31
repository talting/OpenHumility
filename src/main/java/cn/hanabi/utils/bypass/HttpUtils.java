package cn.hanabi.utils.bypass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

public class HttpUtils {
   private static final String BOUNDARY_PREFIX = "--";
   private static final String LINE_END = "\r\n";

   public HttpUtils() {
      super();
   }

   public static HttpResponse postFormData(String urlStr, Map<String, byte[]> filePathMap, Map<String, Object> keyValues, Map<String, Object> headers) throws IOException {
      HttpURLConnection conn = getHttpURLConnection(urlStr, headers);
      String boundary = "MyBoundary" + System.currentTimeMillis();
      conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

      try {
         DataOutputStream out = new DataOutputStream(conn.getOutputStream());
         Throwable var7 = null;

         try {
            if (keyValues != null && !keyValues.isEmpty()) {
               for(Entry entry : keyValues.entrySet()) {
                  writeSimpleFormField(boundary, out, entry);
               }
            }

            if (filePathMap != null && !filePathMap.isEmpty()) {
               for(Entry entry : filePathMap.entrySet()) {
                  writeFile((String)entry.getKey(), (byte[])entry.getValue(), boundary, out);
               }
            }

            String endStr = "--" + boundary + "--" + "\r\n";
            out.write(endStr.getBytes());
         } catch (Throwable var18) {
            var7 = var18;
            throw var18;
         } finally {
            if (out != null) {
               if (var7 != null) {
                  try {
                     out.close();
                  } catch (Throwable var17) {
                     var7.addSuppressed(var17);
                  }
               } else {
                  out.close();
               }
            }

         }
      } catch (Exception var20) {
         return new HttpUtils.HttpResponse(500, var20.getMessage());
      }

      return getHttpResponse(conn);
   }

   private static HttpURLConnection getHttpURLConnection(String urlStr, Map<String, Object> headers) throws IOException {
      URL url = new URL(urlStr);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setConnectTimeout(50000);
      conn.setReadTimeout(50000);
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setUseCaches(false);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Charset", "UTF-8");
      conn.setRequestProperty("connection", "keep-alive");
      if (headers != null && !headers.isEmpty()) {
         for(Entry header : headers.entrySet()) {
            conn.setRequestProperty((String)header.getKey(), header.getValue().toString());
         }
      }

      return conn;
   }

   private static HttpUtils.HttpResponse getHttpResponse(HttpURLConnection conn) {
      HttpUtils.HttpResponse response;
      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         Throwable var3 = null;

         try {
            int responseCode = conn.getResponseCode();
            StringBuilder responseContent = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null) {
               responseContent.append(line);
            }

            response = new HttpUtils.HttpResponse(responseCode, responseContent.toString());
         } catch (Throwable var15) {
            var3 = var15;
            throw var15;
         } finally {
            if (reader != null) {
               if (var3 != null) {
                  try {
                     reader.close();
                  } catch (Throwable var14) {
                     var3.addSuppressed(var14);
                  }
               } else {
                  reader.close();
               }
            }

         }
      } catch (Exception var17) {
         response = new HttpUtils.HttpResponse(500, var17.getMessage());
      }

      return response;
   }

   private static void writeFile(String paramName, byte[] bytes, String boundary, DataOutputStream out) {
      try {
         String boundaryStr = "--" + boundary + "\r\n";
         out.write(boundaryStr.getBytes());
         String fileName = UUID.randomUUID() + ".jpg";
         String contentDispositionStr = String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"", paramName, fileName) + "\r\n";
         out.write(contentDispositionStr.getBytes());
         String contentType = "Content-Type: application/octet-stream\r\n\r\n";
         out.write(contentType.getBytes());
         out.write(bytes);
         out.write("\r\n".getBytes());
      } catch (Exception var8) {
         ;
      }

   }

   private static void writeSimpleFormField(String boundary, DataOutputStream out, Entry entry) throws IOException {
      String boundaryStr = "--" + boundary + "\r\n";
      out.write(boundaryStr.getBytes());
      String contentDispositionStr = String.format("Content-Disposition: form-data; name=\"%s\"", entry.getKey()) + "\r\n" + "\r\n";
      out.write(contentDispositionStr.getBytes());
      String valueStr = entry.getValue().toString() + "\r\n";
      out.write(valueStr.getBytes());
   }

   public static HttpUtils.HttpResponse postText(String urlStr, String filePath) throws IOException {
      URL url = new URL(urlStr);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "text/plain");
      conn.setDoOutput(true);
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);

      try {
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
         Throwable var5 = null;

         try {
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            Throwable var7 = null;

            try {
               String line;
               try {
                  while((line = fileReader.readLine()) != null) {
                     writer.write(line);
                  }
               } catch (Throwable var32) {
                  var7 = var32;
                  throw var32;
               }
            } finally {
               if (fileReader != null) {
                  if (var7 != null) {
                     try {
                        fileReader.close();
                     } catch (Throwable var31) {
                        var7.addSuppressed(var31);
                     }
                  } else {
                     fileReader.close();
                  }
               }

            }
         } catch (Throwable var34) {
            var5 = var34;
            throw var34;
         } finally {
            if (writer != null) {
               if (var5 != null) {
                  try {
                     writer.close();
                  } catch (Throwable var30) {
                     var5.addSuppressed(var30);
                  }
               } else {
                  writer.close();
               }
            }

         }
      } catch (Exception var36) {
         return new HttpUtils.HttpResponse(500, var36.getMessage());
      }

      return getHttpResponse(conn);
   }

   public static class HttpResponse {
      private int code;
      private String content;

      public HttpResponse(int status, String content) {
         super();
         this.code = status;
         this.content = content;
      }

      public int getCode() {
         return this.code;
      }

      public void setCode(int code) {
         this.code = code;
      }

      public String getContent() {
         return this.content;
      }

      public void setContent(String content) {
         this.content = content;
      }

      public String toString() {
         return "[ code = " + this.code + " , content = " + this.content + " ]";
      }
   }
}
