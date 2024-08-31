package cn.hanabi.utils.auth.j4socket.framing;

import cn.hanabi.utils.auth.j4socket.enums.Opcode;

public class BinaryFrame extends DataFrame {
   public BinaryFrame() {
      super(Opcode.BINARY);
   }
}
