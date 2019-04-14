package main.java.database;


import main.java.file.FileChunk;
import main.java.file.FileChunkID;
import main.java.file.FileID;
import main.java.peer.Peer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Database implements Serializable {

    private static final long serialVersionUID = 1L;



    private ConcurrentHashMap<FileChunkID, ArrayList<Integer>> perceivedRepDeg;
    private ConcurrentHashMap<FileChunkID, Integer> desiredRepDeg;
    private List<FileID> storedFiles;


    public Database() {

        storedFiles = new ArrayList<>();
        perceivedRepDeg = new ConcurrentHashMap<FileChunkID, ArrayList<Integer>>();
        desiredRepDeg = new ConcurrentHashMap<FileChunkID, Integer>();
    }




    public void insertChunkInfo(FileID fileID, int replicationDegree, int chunkNo, int peerID) {
        FileChunkID id = new FileChunkID(fileID.toString(), chunkNo);

        perceivedRepDeg.put(id, new ArrayList<>());
        perceivedRepDeg.get(id).add(peerID);
        desiredRepDeg.put(id, replicationDegree);

        Peer.saveDBToDisk();

    }

    public void removeChunkInfo(FileChunkID chunkID){
        desiredRepDeg.remove(chunkID);
        perceivedRepDeg.remove(chunkID);
        Peer.saveDBToDisk();

    }

    public void printDatabase() {
        System.out.println("printing db...");

        if(!storedFiles.isEmpty())
            System.out.println("Files I have backed up: ");

        for (FileID fid: storedFiles){

            String value = fid.toString();
            System.out.println("\t" + value);

        }
        for (FileChunkID name: perceivedRepDeg.keySet()){

            String key = name.toString();
            int value = perceivedRepDeg.get(name).size();
            System.out.println(key + " perceivedRepDeg: " + value);
        }
    }

    public void insertFile(FileID fileID) {

        if(!storedFiles.contains(fileID)){
            storedFiles.add(fileID);
            Peer.saveDBToDisk();
        }
        else {
            int i = storedFiles.indexOf(fileID);
            storedFiles.get(i).setNumChunks(fileID.getNumChunks());
        }

    }

    public void removeFile(FileID fileID){
        storedFiles.remove(fileID);

        Peer.saveDBToDisk();


    }

    public int getNumChunksOfFile(FileID fID){
        int index = storedFiles.indexOf(fID);
        if(index!=-1)
            return storedFiles.get(index).getNumChunks();
        else
            return -1;

    }

    public String printStoredFiles() {
        return "\t Stored Files: \n " +
                storedFiles.toString() + "\n";
    }

    public boolean isFileStored(FileID fID) {

        return storedFiles.contains(fID);
    }



    public List<String> getFileChunksofFileID(FileID fileID) {


        List<String> chunksOfFile = new ArrayList<>();

        printDatabase();
        for(FileID fid : storedFiles) {
            if(fid.equals(fileID)) {

                for (int i = 0; i < fid.getNumChunks(); i++){
                    chunksOfFile.add(fid + "-" + i);
                }
            }

        }

        return chunksOfFile;
    }


    public synchronized void addNewRepDegCounter(FileChunkID chunkID, Integer repDeg){

        if (!perceivedRepDeg.containsKey(chunkID)) {
            //TODO: ADD OWN PEERD ID TO LIST??
            perceivedRepDeg.put(chunkID, new ArrayList<>());
        }


        if (!desiredRepDeg.containsKey(chunkID)) {
            desiredRepDeg.put(chunkID, repDeg);
        }

        System.out.println("SAVING DB");
        Peer.saveDBToDisk();
    }

    public void removeRepDegCounter(FileChunkID chunkID){
        perceivedRepDeg.remove(chunkID);
        desiredRepDeg.remove(chunkID);
    }

    public void increasePerceivedRepDeg(FileChunkID chunkID, int senderID){

        if(perceivedRepDeg.containsKey(chunkID)) {

            if (!perceivedRepDeg.get(chunkID).contains(senderID)) {
                perceivedRepDeg.get(chunkID).add(senderID);
                System.out.println("SAVING DB");
                Peer.saveDBToDisk();
            }

        }
    }

    public void decreasePerseivedRepDeg(FileChunkID chunkID, int senderID){
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