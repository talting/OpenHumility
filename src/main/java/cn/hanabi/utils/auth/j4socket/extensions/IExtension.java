package cn.hanabi.utils.auth.j4socket.extensions;

import cn.hanabi.utils.auth.j4socket.exceptions.InvalidDataException;
import cn.hanabi.utils.auth.j4socket.framing.Framedata;

public interface IExtension {
   void decodeFrame(Framedata var1) throws InvalidDataException;

   void encodeFrame(Framedata var1);

   boolean acceptProvidedExtensionAsServer(String var1);

   boolean acceptProvidedExtensionAsClient(String var1);

   void isFrameValid(Framedata var1) throws InvalidDataException;

   String getProvidedExtensionAsClient();

   String getProvidedExtensionAsServer();

   IExtension copyInstance();

   void reset();

   String toString();
}
