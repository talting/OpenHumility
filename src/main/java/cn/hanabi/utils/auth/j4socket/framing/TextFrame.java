package cn.hanabi.utils.auth.j4socket.framing;

import cn.hanabi.utils.auth.j4socket.enums.Opcode;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidDataException;
import cn.hanabi.utils.auth.j4socket.util.Charsetfunctions;

public class TextFrame extends DataFrame {
   public TextFrame() {
      super(Opcode.TEXT);
   }

   public void isValid() throws InvalidDataException {
      super.isValid();
      if (!Charsetfunctions.isValidUTF8(this.getPayloadData())) {
         throw new InvalidDataException(1007, "Received text is no valid utf8 string!");
      }
   }
}
