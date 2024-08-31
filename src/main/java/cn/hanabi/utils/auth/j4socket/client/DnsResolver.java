package cn.hanabi.utils.auth.j4socket.client;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public interface DnsResolver {
   InetAddress resolve(URI var1) throws UnknownHostException;
}
