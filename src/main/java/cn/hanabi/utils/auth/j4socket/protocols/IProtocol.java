package cn.hanabi.utils.auth.j4socket.protocols;

public interface IProtocol {
   boolean acceptProvidedProtocol(String var1);

   String getProvidedProtocol();

   IProtocol copyInstance();

   String toString();
}
