package main.java.listeners;


import main.java.file.FileChunkID;
import main.java.peer.Peer;
import main.java.service.PacketHandler;
import main.java.utils.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class Listener implements Runnable {

    private MulticastSocket multicastSocket;
    public InetAddress address;
    public int port;




    public Listener(InetAddress address, int port) {
        this.address = address;
        this.port = port;

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


                /*
                TODO test as mentioned in peer.java
                do not send pack to the peer that is running
                 */
                System.out.println("SENDER ID: " + senderAddress.toString());
                System.out.println("MY ID: " + Peer.getAddress().toString());
                if(!senderAddress.toString().equals(Peer.getAddress().toString())) {

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
                    System.out.println("NOT SENDING PACKET TO MYSELF");
                }



            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        multicastSocket.close();



    }


    public void startCountingStoreds(FileChunkID fileChunkID) {

    }

    public void clearCount(FileChunkID fileChunkID) {

    }

    public int getCount(FileChunkID fileChunkID) {
        return 0;
    }

    public void dumpHashmap() {

    }

    public void stopCounting(FileChunkID fileChunkID) {

    }

    public void send(DatagramPacket messagePacket) throws IOException {
        multicastSocket.send(messagePacket);
    }

    public void countPutChunk(FileChunkID chunkID, String senderID) {

    }
}
