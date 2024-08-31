package cn.hanabi.utils.auth.j4socket.framing;

import cn.hanabi.utils.auth.j4socket.enums.Opcode;

public class PingFrame extends ControlFrame {
   public PingFrame() {
      super(Opcode.PING);
   }
}
