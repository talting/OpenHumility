package cn.hanabi.utils.auth.j4socket.framing;

import cn.hanabi.utils.auth.j4socket.enums.Opcode;

public class ContinuousFrame extends DataFrame {
   public ContinuousFrame() {
      super(Opcode.CONTINUOUS);
   }
}
