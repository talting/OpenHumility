package cn.hanabi.utils.auth.j4socket.enums;

public enum Opcode {
   CONTINUOUS,
   TEXT,
   BINARY,
   PING,
   PONG,
   CLOSING;

   private Opcode() {
   }
}
