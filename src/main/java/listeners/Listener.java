package main.java.listeners;


import main.java.file.FileChunk;
import main.java.file.FileChunkID;
import main.java.peer.Peer;
import main.java.service.PacketHandler;
import main.java.utils.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class Listener implements Runnable {

    private MulticastSocket multicastSocket;
    public InetAddress address;
    public int port;

    public volatile ConcurrentHashMap<String, ArrayList<FileChunk>> chunksReceived;

    private ConcurrentHashMap<FileChunkID, ArrayList<String>> storedChunks;

    private ConcurrentHashMap<FileChunkID, ArrayList<String>> putChunks;



    public Listener(InetAddress addr, int prt) {
        address = addr;
        port = prt;
        storedChunks = new ConcurrentHashMap<>();
        putChunks = new ConcurrentHashMap<>();
        chunksReceived = new ConcurrentHashMap<>();

    }



    @Override
    public void run() {

        try {
            multicastSocket = new MulticastSocket(port);
            multicastSocket.setTimeToLive(1);
            multicastSocket.joinGroup(address);

        } catch (IOException e) {
            e.printStackTrace();
        }



        boolean end = false;
        while(!end) {

            byte[] buffer = new byte[Constants.CHUNK_MAX_SIZE];
            Thread t;
            try {


                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);
                InetAddress senderAddress =  packet.getAddress();
                int senderPort = packet.getPort();


                /* Force this if statement true to run locally */
                /* TODO: remove this line (for non-local runs) */
                boolean debug = true;
                if(debug ||!senderAddress.toString().equals(Peer.getAddress().toString())) {

                    System.out.println("\t Sender ID: " + senderAddress.toString() + " \n" +
                        "\t PEER ID : " + Peer.getID() + "\n");


                    t = new Thread(new PacketHandler(packet));
                    t.start();

                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    System.out.println("\n This is my message, ignoring...\n");
                }



            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        multicastSocket.close();



    }


    public void startCountingStoreds(FileChunkID fileChunkID) {

        if (!storedChunks.containsKey(fileChunkID)) {
            //System.out.println("Starting to count:" + chunkID.toString());
            storedChunks.put(fileChunkID, new ArrayList<>());
        }

    }

    public void clearCount(FileChunkID fileChunkID) {
        storedChunks.get(fileChunkID).clear();
    }

    public int getCount(FileChunkID fileChunkID) {
        return storedChunks.get(fileChunkID).size();
    }

    public void dumpHashmap() {
        for (FileChunkID name: storedChunks.keySet()){

            String key =name.toString();
            String value = storedChunks.get(name).toString();
            System.out.println(key + " " + value);

        }

    }

    public void stopCounting(FileChunkID fileChunkID) {
        storedChunks.remove(fileChunkID);
    }

    public void send(DatagramPacket messagePacket) throws IOException {
        multicastSocket.send(messagePacket);
    }

    public synchronized void countStored(FileChunkID chunkID, String senderID) {
        if (storedChunks.containsKey(chunkID))
            if (!storedChunks.get(chunkID).contains(senderID))
                storedChunks.get(chunkID).add(senderID);
            else
                System.out.println("Already counted this peer");

    }

    public void countPutChunk(FileChunkID chunkID, String senderID) {

    }

    public boolean isCounting(FileChunkID fileChunkID) {
        if(storedChunks.containsKey(fileChunkID))
            return true;
        else {
            return false;
        }
    }



    /*
        MDR
     */
    public synchronized void queueChunk(FileChunk chunk) {
        //System.out.println("CHUNK TO MERGE: " + chunk.getFileID().toString());
        ArrayList<FileChunk> fileChunks = chunksReceived.get(chunk.getFileID().toString());
        fileChunks.add(chunk);
        //System.out.println("File Chunks: " + fileChunks);

        //printChunksReceived();
        notifyAll();
    }

    public synchronized ArrayList<FileChunk> retrieveChunk(String fileID) {
        ArrayList<FileChunk> receivedChunks = chunksReceived.get(fileID);

        return receivedChunks;
    }

    public void printChunksReceived() {
        for (String name: chunksReceived.keySet()){

            String value = chunksReceived.get(name).toString();
            System.out.println(name + ": " + value);
        }
    }


}
