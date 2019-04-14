package main.java.protocols;


import main.java.file.FileChunk;
import main.java.file.FileChunkID;
import main.java.file.FileID;
import main.java.listeners.Broker;
import main.java.peer.Peer;

import java.io.IOException;

public class BackupChunk implements Runnable {

    public FileChunk chunk;
    public boolean repDegReached;

    public BackupChunk(FileChunk chunk) {
        this.chunk = chunk;
        repDegReached = false;
    }

    @Override
    public void run() {
        int attempt = 0;

        long timeToRetry = 500;
        FileID fID = new FileID(chunk.getFileID().toString());


        FileChunkID fileChunkID = new FileChunkID(fID.toString(), chunk.getChunkNo());



        while(!repDegReached) {

            timeToRetry = timeToRetry *2;

            if(Peer.getMCListener().isCounting(fileChunkID))
                Peer.getMCListener().clearCount(fileChunkID);

            try {
                Broker.sendPUTCHUNK(chunk);
                Peer.getMCListener().startCountingStoreds(fileChunkID);
                System.out.println("Listening for STOREDs for " + timeToRetry + "ms");
                Thread.sleep(timeToRetry);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            int perceivedRepDeg = Peer.getMCListener().getCount(fileChunkID);

            System.out.println("Replication degree count: " + perceivedRepDeg + "/" + chunk.getReplicationDegree());


            if(perceivedRepDeg < chunk.getReplicationDegree()) {
                attempt++;

                if(attempt > 4) {
                    System.out.println("The Desired Replication Degree wasn't reached in 5 tries, skipping chunk...");
                    repDegReached = true;
                    return;


                }
                else {
                    System.out.println("The Desired Replication Degree was not reached, trying again...");

                }

            } else {
                System.out.println("Desired Replication Degree accomplished!");
                repDegReached = true;
            }

        }



        Peer.getMCListener().stopCounting(fileChunkID);
    }
}