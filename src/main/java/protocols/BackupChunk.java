package main.java.protocols;


import main.java.file.FileChunk;
import main.java.file.FileChunkID;
import main.java.file.FileID;
import main.java.listeners.Broker;
import main.java.peer.Peer;

import java.io.IOException;

public class BackupChunk implements Runnable {

    FileChunk chunk;


    public BackupChunk(FileChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public void run() {
        int attempt = 0;
        boolean repDegReached = false;
        long timeToRetry = 500;

        FileID fID = new FileID(chunk.getFileID().toString());


        FileChunkID fileChunkID = new FileChunkID(fID.toString(), chunk.getChunkNo());
        Peer.getMcListener().startCountingStoreds(fileChunkID);
        while(!repDegReached) {

            timeToRetry = timeToRetry *2;

            Peer.getMcListener().clearCount(fileChunkID);

            try {
                Broker.sendPUTCHUNK(chunk);

                System.out.println("Listening for STOREDs for " + timeToRetry + "ms");
                Thread.sleep(timeToRetry);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            int perceivedRepDeg = Peer.getMcListener().getCount(fileChunkID);
            // System.out.println("REP DEGREE PERCEIVED: " + perceivedRepDeg);

            System.out.println("CONTEI: " + perceivedRepDeg + " de " + chunk.getReplicationDegree());

            Peer.getMcListener().dumpHashmap();

            if(perceivedRepDeg < chunk.getReplicationDegree()) {
                attempt++;

                if(attempt > 5) {
                    System.out.println("The Desired Replication Degree wasn't reached, terminating...");
                    repDegReached = true;


                }
                else {
                    System.out.println("The Desired Replication Degree was not reached, trying again...");

                }

            } else {
                System.out.println("Desired Replication Degree accomplished!");
                repDegReached = true;
            }

        }

        Peer.getMcListener().stopCounting(fileChunkID);
    }
}