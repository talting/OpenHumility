package cn.hanabi.utils.auth.j4socket.framing;

import cn.hanabi.utils.auth.j4socket.enums.Opcode;

public class PongFrame extends ControlFrame {
   public PongFrame() {
      super(Opcode.PONG);
   }

   public PongFrame(PingFrame pingFrame) {
      super(Opcode.PONG);
      this.setPayload(pingFrame.getPayloadData());
   }
}
