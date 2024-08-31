package cn.hanabi.altmanager;

public final class Alt {
   private String username;
   private String mask;
   private String password;
   public double anim;
   public double loginAnim;

   public Alt(String username, String password) {
      this(username, password, "");
   }

   public Alt(String username, String password, String mask) {
      super();
      this.anim = 0.0D;
      this.loginAnim = 0.0D;
      this.mask = "";
      this.username = username;
      this.password = password;
      this.mask = mask;
   }

   public String getMask() {
      return this.mask;
   }

   public void setMask(String mask) {
      this.mask = mask;
   }

   public String getPassword() {
      return this.password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getUsername() {
      return this.username;
   }
}
