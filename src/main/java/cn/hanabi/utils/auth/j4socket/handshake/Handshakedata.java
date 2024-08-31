package cn.hanabi.utils.auth.j4socket.handshake;

import java.util.Iterator;

public interface Handshakedata {
   Iterator iterateHttpFields();

   String getFieldValue(String var1);

   boolean hasFieldValue(String var1);

   byte[] getContent();
}
