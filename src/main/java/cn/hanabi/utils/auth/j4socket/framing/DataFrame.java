package cn.hanabi.utils.auth.j4socket.framing;

import cn.hanabi.utils.auth.j4socket.enums.Opcode;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidDataException;

public abstract class DataFrame extends FramedataImpl1 {
   public DataFrame(Opcode opcode) {
      super(opcode);
   }

   public void isValid() throws InvalidDataException {
   }
}
