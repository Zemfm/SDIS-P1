package main.java.listeners;

import main.java.file.FileChunk;
import main.java.file.FileChunkID;
import main.java.file.FileID;
import main.java.peer.Peer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

import static main.java.utils.Utilities.*;
import static main.java.utils.Constants.*;


public class Broker {


    public static void sendSTORED(FileChunkID chunkID) {
        String message = messageConstructor("STORED", null, chunkID, null);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(message.getBytes());

            byte messageToSend[] = outputStream.toByteArray();

            Broker.sendToMC(messageToSend);
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    public static void sendDELETE(FileID fileID) {
        String message = messageConstructor("DELETE", null, null, fileID);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(message.getBytes());

            byte messageToSend[] = outputStream.toByteArray();

            Broker.sendToMC(messageToSend);
        }
        catch(IOException e){
            e.printStackTrace();
        }


        Broker.sendToMC(message.getBytes());
    }



    public static void sendPUTCHUNK(FileChunk chunk) throws InterruptedException {
        String message = messageConstructor("PUTCHUNK", chunk, null, null);


        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(message.getBytes());

            byte messageToSend[] = outputStream.toByteArray();

            Broker.sendToMDB(messageToSend);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void sendREMOVED(FileChunkID chunkID){

        String message = messageConstructor("REMOVED", null, chunkID, null);


        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(message.getBytes());

            byte messageToSend[] = outputStream.toByteArray();

            Broker.sendToMC(messageToSend);
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    public static void sendGETCHUNK(FileChunkID chunkID) {
        String message = messageConstructor("GETCHUNK", null, chunkID, null);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(message.getBytes());

            byte messageToSend[] = outputStream.toByteArray();

            Broker.sendToMC(messageToSend);
        }
        catch(IOException e){
            e.printStackTrace();
        }



    }

    public static void sendCHUNK(FileChunk chunk) {
        String message = messageConstructor("CHUNK", chunk, null, null);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(message.getBytes());

            byte messageToSend[] = outputStream.toByteArray();

            Broker.sendToMC(messageToSend);
        }
        catch(IOException e){
            e.printStackTrace();
        }



    }

    private static void sendToMDB(byte[] backupMessage) throws InterruptedException {


        DatagramPacket messagePacket = new DatagramPacket(backupMessage, backupMessage.length, Peer.getMDBListener().address, Peer.getMDBListener().port);
        
        try {
            Peer.getMDBListener().send(messagePacket);

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private static void sendToMC(byte[] backupMessage) {


        DatagramPacket messagePacket = new DatagramPacket(backupMessage, backupMessage.length, Peer.getMCListener().address, Peer.getMCListener().port);


        try {
            Peer.getMCListener().send(messagePacket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendToMDR(byte[] restoreMessage) {
        DatagramPacket messagePacket = new DatagramPacket(restoreMessage, restoreMessage.length, Peer.getMDRListener().address, Peer.getMDRListener().port);

        try {
            Peer.getMDRListener().send(messagePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}