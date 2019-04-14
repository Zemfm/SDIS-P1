package main.java.protocols;

import main.java.file.FileID;
import main.java.listeners.Broker;
import main.java.peer.Peer;

public class Delete implements Runnable {

    private FileID fileID;

    public Delete(FileID fId){
        fileID = fId;
    }

    @Override
    public void run() {
        Broker.sendDELETE(fileID);

        Peer.getDb().removeFile(fileID);
    }

}
