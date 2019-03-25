package main.java.utils;


import main.java.peer.Peer;



import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.regex.Pattern;

import static main.java.utils.Constants.*;

public class Utilities {


    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static void info_message(String message) {
        System.out.println(message);
    }

    public static InetAddress getLocalAddress() throws IOException
    {
        MulticastSocket socket = new MulticastSocket();
        socket.setTimeToLive(0);

        InetAddress addr = InetAddress.getByName("225.0.0.0");
        socket.joinGroup(addr);

        byte[] bytes = new byte[0];
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, addr,
                socket.getLocalPort());

        socket.send(packet);
        socket.receive(packet);

        socket.close();

        return packet.getAddress();
    }

    public static boolean validateAccessPoint(String peerAP) {

        Pattern pattern;
        pattern = Pattern.compile("^([0-9]{1,3}\\.){3}[0-9]{1,3}(\\/([A-Z]|[a-z])+)?$");

        if(!pattern.matcher(peerAP).matches()) {
            System.out.println("Invalid Access Point!");
            return false;
        }
        return true;
    }



    //TODO FINISH AND USE THIS
    public static String messageConstructor(String messageType/*, FileChunk chunk*/) {
        String message = "default";
        switch (messageType) {
            case "PUTCHUNK":
                message = Constants.PUTCHUNK + MESSAGE_SEPARATOR
                        + Peer.getProtocolVersion() + MESSAGE_SEPARATOR
                        + Peer.getID() + MESSAGE_SEPARATOR
                        // + chunk.getFileID() + MESSAGE_SEPARATOR
                        // + chunk.getChunkNo() + MESSAGE_SEPARATOR
                        // + chunk.getReplicationDegree() + MESSAGE_SEPARATOR
                        + CRLF + CRLF;
                break;

        }
        return message;
    }

}
