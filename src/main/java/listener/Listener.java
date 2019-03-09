package main.java.listener;


import java.net.InetAddress;
import java.net.MulticastSocket;


public class Listener {

    private MulticastSocket multicastSocket;
    public InetAddress address;
    public int port;




    public Listener(InetAddress address, int port) {
        this.address = address;
        this.port = port;

    }

}
