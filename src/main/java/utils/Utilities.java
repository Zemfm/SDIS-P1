package main.java.utils;


import main.java.file.FileChunk;
import main.java.file.FileChunkID;
import main.java.file.FileID;
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



    //TODO: CONFIRM MESSAGES ARE AS SPECIFIED IN DOCUMENT <- IMPORTANT FOR INTEROPERABILITY
    public static String messageConstructor(String protocol, FileChunk chunk, FileChunkID chunkID, FileID fileID) {
        String message = "default";
        switch (protocol) {
            case "PUTCHUNK":
                message = PUTCHUNK + MESSAGE_SEPARATOR;
                message += Peer.getProtocolVersion() + MESSAGE_SEPARATOR;
                message += Peer.getID() + MESSAGE_SEPARATOR;
                message += chunk.getFileID() + MESSAGE_SEPARATOR;
                message += chunk.getChunkNo() + MESSAGE_SEPARATOR;
                message += chunk.getReplicationDegree() + MESSAGE_SEPARATOR;
                message += CRLF + CRLF;
                message += chunk.getChunkData();
                break;
            case "STORED":
                message = STORED + MESSAGE_SEPARATOR;
                message += Peer.getProtocolVersion() + MESSAGE_SEPARATOR;
                message += Peer.getID() + MESSAGE_SEPARATOR;
                message += chunkID.getFileID() + MESSAGE_SEPARATOR;
                message += chunkID.getChunkNumber() + MESSAGE_SEPARATOR;
                message += CRLF + CRLF;
                break;
            case "REMOVED":
                message = REMOVED + MESSAGE_SEPARATOR;
                message += Peer.getProtocolVersion() + MESSAGE_SEPARATOR;
                message += Peer.getID() + MESSAGE_SEPARATOR;
                message += chunkID.getFileID() + MESSAGE_SEPARATOR;
                message += chunkID.getChunkNumber() + MESSAGE_SEPARATOR;
                message += CRLF + CRLF;
                break;
            case "DELETE":
                message = DELETE + MESSAGE_SEPARATOR;
                message += Peer.getProtocolVersion() + MESSAGE_SEPARATOR;
                message += Peer.getID() + MESSAGE_SEPARATOR;
                message += fileID.toString() + MESSAGE_SEPARATOR;
                message += CRLF+CRLF;
                break;
            case "GETCHUNK":
                message = GETCHUNK + MESSAGE_SEPARATOR;
                message += Peer.getProtocolVersion() + MESSAGE_SEPARATOR;
                message += Peer.getID() + MESSAGE_SEPARATOR;
                message += chunkID.getFileID() + MESSAGE_SEPARATOR;
                message += chunkID.getChunkNumber() + MESSAGE_SEPARATOR;
                message += CRLF + CRLF;
                break;
            case "CHUNK": //TODO getChunkData might be wrong
                message = CHUNK + MESSAGE_SEPARATOR;
                message += Peer.getProtocolVersion() + MESSAGE_SEPARATOR;
                message += Peer.getID() + MESSAGE_SEPARATOR;
                message += chunkID.getFileID() + MESSAGE_SEPARATOR;
                message += chunkID.getChunkNumber() + MESSAGE_SEPARATOR;
                message += CRLF + CRLF;
                message += chunk.getChunkData();
                break;

        }
        return message;
    }

}
