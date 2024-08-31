package cn.hanabi.utils.auth.j4socket.framing;

import cn.hanabi.utils.auth.j4socket.enums.Opcode;
import java.nio.ByteBuffer;

public interface Framedata {
   boolean isFin();

   boolean isRSV1();

   boolean isRSV2();

   boolean isRSV3();

   boolean getTransfereMasked();

   Opcode getOpcode();

   ByteBuffer getPayloadData();

   void append(Framedata var1);
}
