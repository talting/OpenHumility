package cn.hanabi.utils.auth.j4socket.handshake;

import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;

public class HandshakedataImpl1 implements HandshakeBuilder {
   private byte[] content;
   private TreeMap map = new TreeMap(String.CASE_INSENSITIVE_ORDER);

   public HandshakedataImpl1() {
      super();
   }

   public Iterator iterateHttpFields() {
      return Collections.unmodifiableSet(this.map.keySet()).iterator();
   }

   public String getFieldValue(String name) {
      String s = (String)this.map.get(name);
      return s == null ? "" : s;
   }

   public byte[] getContent() {
      return this.content;
   }

   public void setContent(byte[] content) {
      this.content = content;
   }

   public void put(String name, String value) {
      this.map.put(name, value);
   }

   public boolean hasFieldValue(String name) {
      return this.map.containsKey(name);
   }
}
