package cn.hanabi.utils.auth.j4socket.framing;

import cn.hanabi.utils.auth.j4socket.enums.Opcode;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidDataException;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidFrameException;

public abstract class ControlFrame extends FramedataImpl1 {
   public ControlFrame(Opcode opcode) {
      super(opcode);
   }

   public void isValid() throws InvalidDataException {
      if (!this.isFin()) {
         throw new InvalidFrameException("Control frame can't have fin==false set");
      } else if (this.isRSV1()) {
         throw new InvalidFrameException("Control frame can't have rsv1==true set");
      } else if (this.isRSV2()) {
         throw new InvalidFrameException("Control frame can't have rsv2==true set");
      } else if (this.isRSV3()) {
         throw new InvalidFrameException("Control frame can't have rsv3==true set");
      }
   }
}
