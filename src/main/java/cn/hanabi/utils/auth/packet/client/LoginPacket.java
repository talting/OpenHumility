package cn.hanabi.utils.auth.packet.client;

public class LoginPacket {
   public String Version;
   public String UserName;
   public String Password;
   public String HWID;

   public LoginPacket(String version, String userName, String password, String hWID) {
      super();
      this.Version = version;
      this.UserName = userName;
      this.Password = password;
      this.HWID = hWID;
   }
}
