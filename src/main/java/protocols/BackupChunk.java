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

        //Peer.getMCListener().startCountingStoreds(fileChunkID);

        /*
            done?:
            This message is used to ensure that the chunk is backed up with the desired replication degree
            as follows. The initiator-peer collects the confirmation messages during a time interval of one
            second. If the number of confirmation messages it received up to the end of that interval is lower
             than the desired replication degree, it retransmits the backup message on the MDB channel, and doubles
            the time interval for receiving confirmation messages. This procedure is repeated up to a maximum
             number of five times, i.e. the initiator will send at most 5 PUTCHUNK messages per chunk.

         */

        while(!repDegReached) {

            timeToRetry = timeToRetry *2;

            Peer.getMCListener().clearCount(fileChunkID);

            try {
                Broker.sendPUTCHUNK(chunk);

                System.out.println("Listening for STOREDs for " + timeToRetry + "ms");
                Thread.sleep(timeToRetry);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            int perceivedRepDeg = Peer.getMCListener().getCount(fileChunkID);

            System.out.println("CONTEI: " + perceivedRepDeg + " de " + chunk.getReplicationDegree());

            Peer.getMCListener().dumpHashmap();

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

        Peer.getMCListener().stopCounting(fileChunkID);
    }
}