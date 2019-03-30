package main.java.database;


import main.java.file.FileChunk;
import main.java.file.FileChunkID;
import main.java.file.FileID;
import main.java.peer.Peer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Database implements Serializable {

    private static final long serialVersionUID = 1L;


    private volatile ConcurrentHashMap<FileChunkID, FileChunk> database;

    private  ConcurrentHashMap<FileChunkID, ArrayList<String>> perceivedRepDeg;
    private  ConcurrentHashMap<FileChunkID, Integer> desiredRepDeg;

    private volatile ConcurrentHashMap<String, FileID> storedFiles;

    public Database() {
        database = new ConcurrentHashMap<>();
        storedFiles = new ConcurrentHashMap<>();
        database = new ConcurrentHashMap<FileChunkID, FileChunk>();
        perceivedRepDeg = new ConcurrentHashMap<FileChunkID, ArrayList<String>>();
        desiredRepDeg = new ConcurrentHashMap<FileChunkID, Integer>();
    }




    public void insertChunk(FileID fileID, int replicationDegree, int chunkNo, byte[] chunkData) {
        FileChunkID id = new FileChunkID(fileID.toString(), chunkNo);
        FileChunk chunk = new FileChunk(replicationDegree, chunkNo, fileID, chunkData);
        database.put(id, chunk);


        Peer.saveDBToDisk();

    }

    public void removeChunk(FileChunkID chunkID){
        desiredRepDeg.remove(chunkID);
        perceivedRepDeg.remove(chunkID);
        database.remove(chunkID);
        Peer.saveDBToDisk();

    }

    public boolean hasChunk(FileChunkID chunkID) {
        return database.containsKey(chunkID);

    }

    public FileChunk getChunk(FileChunkID chunkID){
        if(hasChunk(chunkID))
            return database.get(chunkID);
        else
            return null;
    }

    public void printDatabase() {
        for (FileChunkID name: database.keySet()){

            String key =name.toString();
            String value = database.get(name).toString();
            System.out.println(key + " " + value);
        }
        for (FileChunkID name: perceivedRepDeg.keySet()){

            String key =name.toString();
            String value = perceivedRepDeg.get(name).toString();
            System.out.println(key + " " + value);
        }
    }

    public void insertFile(String fileName, FileID fileID) {
        System.out.println("\t Inserting File ->" + fileName + " " + fileID + " in Stored Files Database \n");
        storedFiles.put(fileName, fileID);

        Peer.saveDBToDisk();
    }

    public int getNumChunksOfFile(String fileName){
        return storedFiles.get(fileName).getNumChunks();
    }

    public String printStoredFiles() {
        return "\t Stored Files: \n " +
                storedFiles.toString() + "\n";
    }

    public boolean isFileStored(String fileName) {
        return storedFiles.containsKey(fileName);
    }



    public ArrayList<FileChunkID> getFileChunksofFileID(FileID fileID) {
        ArrayList<FileChunkID> chunksOfFile = new ArrayList<>();
        System.out.println("DATABASE : ");
        System.out.println(database);

        for(FileChunkID cid : database.keySet()) {
            System.out.println("DATABASE : " + database.keySet());
            System.out.println("CID: " + cid.getFileID());
            if(cid.getFileID().equals(fileID.toString())) {
                chunksOfFile.add(cid);
            }

        }

        return chunksOfFile;
    }


    public synchronized void addNewRepDegCounter(FileChunkID chunkID, Integer repDeg){

        if (!perceivedRepDeg.containsKey(chunkID)) {
            perceivedRepDeg.put(chunkID, new ArrayList<>());
        }


        if (!desiredRepDeg.containsKey(chunkID)) {
            desiredRepDeg.put(chunkID, new Integer(repDeg));
        }

        System.out.println("SAVING DB");
        Peer.saveDBToDisk();
    }

    public void removeRepDegCounter(FileChunkID chunkID){
        perceivedRepDeg.remove(chunkID);
        desiredRepDeg.remove(chunkID);
    }

    public void increasePerseivedRepDeg(FileChunkID chunkID, String senderID){

        if(perceivedRepDeg.containsKey(chunkID)) {

            if (!perceivedRepDeg.get(chunkID).contains(senderID)) {
                perceivedRepDeg.get(chunkID).add(senderID);
                System.out.println("SAVING DB");
                Peer.saveDBToDisk();
            }

        }
    }

    public void decreasePerseivedRepDeg(FileChunkID chunkID, String senderID){
        if(perceivedRepDeg.containsKey(chunkID)) {

            if (perceivedRepDeg.get(chunkID).contains(senderID)) {
                perceivedRepDeg.get(chunkID).remove(senderID);
                System.out.println("SAVING DB");
                Peer.saveDBToDisk();
            }

        }
    }

    public int getPerceivedRepDeg(FileChunkID chunkID){
        return perceivedRepDeg.get(chunkID).size();
    }

    public Integer getDesiredRepDeg(FileChunkID chunkID){
        return desiredRepDeg.get(chunkID);
    }


    public synchronized FileChunkID getHighestPerceivedRepDegChunk() {
        FileChunkID best = null;

        for (FileChunkID chunkID : perceivedRepDeg.keySet()) {
            if (best == null || perceivedRepDeg.get(chunkID).size() > perceivedRepDeg.get(best).size())
                best = chunkID;

        }

        return best;
    }


}