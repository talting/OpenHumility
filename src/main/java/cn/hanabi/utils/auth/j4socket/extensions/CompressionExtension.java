package cn.hanabi.utils.auth.j4socket.extensions;

import cn.hanabi.utils.auth.j4socket.exceptions.InvalidDataException;
import cn.hanabi.utils.auth.j4socket.exceptions.InvalidFrameException;
import cn.hanabi.utils.auth.j4socket.framing.ControlFrame;
import cn.hanabi.utils.auth.j4socket.framing.DataFrame;
import cn.hanabi.utils.auth.j4socket.framing.Framedata;

public abstract class CompressionExtension extends DefaultExtension {
   public CompressionExtension() {
      super();
   }

   public void isFrameValid(Framedata inputFrame) throws InvalidDataException {
      if (!(inputFrame instanceof DataFrame) || !inputFrame.isRSV2() && !inputFrame.isRSV3()) {
         if (inputFrame instanceof ControlFrame && (inputFrame.isRSV1() || inputFrame.isRSV2() || inputFrame.isRSV3())) {
            throw new InvalidFrameException("bad rsv RSV1: " + inputFrame.isRSV1() + " RSV2: " + inputFrame.isRSV2() + " RSV3: " + inputFrame.isRSV3());
         }
      } else {
         throw new InvalidFrameException("bad rsv RSV1: " + inputFrame.isRSV1() + " RSV2: " + inputFrame.isRSV2() + " RSV3: " + inputFrame.isRSV3());
      }
   }
}
