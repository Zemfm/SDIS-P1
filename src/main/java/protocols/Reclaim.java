package main.java.protocols;

import main.java.file.FileChunkID;
import main.java.file.FileID;
import main.java.listeners.Broker;
import main.java.peer.Peer;

import java.io.File;

import static main.java.utils.Constants.MAX_WAITING_TIME;

public class Reclaim implements Runnable {

    private int amount;

    public Reclaim(int amount){
        this.amount = amount;

    }
    @Override
    public void run() {

        Peer.getDisk().setCapacity(amount);

        while (Peer.getDisk().getFreeSpace() < 0){
            FileChunkID chunkID = Peer.getDb().getHighestPerceivedRepDegChunk();

            System.out.println("CHUNKID: " + chunkID.toString());
            if(chunkID != null){
                Broker.sendREMOVED(chunkID);


                File f = new File("peer"+Peer.getID()+"/Backup/"+
                        chunkID.getFileID().split("\\.")[0]+ "/"+chunkID.toString());

               // File f = new File("/debug/"+chunkID.toString());



                Peer.getDisk().removeFile(f.length());
                Peer.getDb().decreasePerceivedRepDeg(chunkID, Peer.getID());
                /* Peer.getDb().removeFile(new FileID(chunkID.getFileID(), -1)); */

                try{
                    //System.out.println("\tWaiting...");
                    Thread.sleep((long)(Math.random() * MAX_WAITING_TIME));
                } catch (InterruptedException ie){
                    ie.printStackTrace();
                }

                f.delete();


            }

        }

    }
}
