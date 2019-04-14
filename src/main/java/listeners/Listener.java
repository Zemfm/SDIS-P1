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

import static main.java.utils.Constants.*;


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

            byte[] buffer = new byte[PACKET_MAX_SIZE];
            Thread t;
            try {


                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);


                t = new Thread(new PacketHandler(packet));
                t.start();

                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
            if (!storedChunks.get(chunkID).contains(senderID)) {
                storedChunks.get(chunkID).add(senderID);
                Peer.getDb().increasePerceivedRepDeg(chunkID, Integer.parseInt(senderID));
            }
            else
                System.out.println("Already counted this peer");

    }
    public synchronized void startCountingPutChunks(FileChunkID chunkID) {
        if (!putChunks.containsKey(chunkID))
            putChunks.put(chunkID, new ArrayList<>());
    }

    public synchronized void countPutChunk(FileChunkID chunkID, String senderID) {

        if (putChunks.containsKey(chunkID))
            if (!putChunks.get(chunkID).contains(senderID))
                putChunks.get(chunkID).add(senderID);

    }

    public synchronized int getCountPutChunks(FileChunkID chunkID) {
        return putChunks.get(chunkID).size();
    }

    public synchronized void stopSavingPutChunks(FileChunkID chunkID) {
        putChunks.remove(chunkID);
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
