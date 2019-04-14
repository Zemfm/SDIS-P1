package main.java.database;


import main.java.file.FileChunkID;
import main.java.file.FileID;
import main.java.peer.Peer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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




    public synchronized void insertChunkInfo(FileID fileID, int replicationDegree, int chunkNo, int peerID) {
        FileChunkID id = new FileChunkID(fileID.toString(), chunkNo);

        perceivedRepDeg.put(id, new ArrayList<>());
        perceivedRepDeg.get(id).add(peerID);
        desiredRepDeg.put(id, replicationDegree);

        Peer.saveDBToDisk();

    }

    public synchronized void removeChunkInfo(FileChunkID chunkID){
        desiredRepDeg.remove(chunkID);
        perceivedRepDeg.remove(chunkID);
        Peer.saveDBToDisk();

    }

    public void printDatabase() {
        /*
        For each file whose backup it has initiated:
                The file pathname
                The backup service id of the file
                The desired replication degree
                    For each chunk of the file:
                        Its id
                        Its perceived replication degree
         */



        System.out.println("----------------------------------------------------");

        System.out.println("Printing Database Info...\n\n");

        if(!storedFiles.isEmpty()){
            System.out.println("Files I have backed up: ");



            for (FileID fid: storedFiles){

                if(fid.getNumChunks()!=-1){
                    System.out.println("\t id: " + fid.toString());
                    System.out.println("\t Desired rep degree: " + fid.getDesiredRepDeg());
                    System.out.println("\t Chunks: ");

                    Iterator<Map.Entry<FileChunkID, ArrayList<Integer>>> it = perceivedRepDeg.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<FileChunkID, ArrayList<Integer>> pair = it.next();

                        FileChunkID cID = pair.getKey();

                        if(cID.getFileID().equals(fid.toString())){

                            ArrayList<Integer> replications = pair.getValue();

                            System.out.println("\t\t ChunkNo:" + cID.getChunkNumber()+ " Perceived Replication Degree:"
                            + replications.size());
                        }


                        it.remove(); // avoids a ConcurrentModificationException
                    }

                }

            }

            System.out.println("----------------------------------------------------");
            System.out.println("Chunks on my system: ");
            for (FileID fid: storedFiles){

                if(fid.getNumChunks()==-1){

                    Iterator<Map.Entry<FileChunkID, ArrayList<Integer>>> it = perceivedRepDeg.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<FileChunkID, ArrayList<Integer>> pair = it.next();

                        FileChunkID cID = pair.getKey();

                        if(cID.getFileID().equals(fid.toString())){

                            ArrayList<Integer> replications = pair.getValue();

                            File f = new File("peer"+Peer.getID()+"/Backup/"+fid.toString().split("\\.")[0]+
                                    "/"+cID.toString());

                            System.out.println("\t\t ChunkNo:" + cID.getChunkNumber()+ " Perceived Replication Degree:"
                                    + replications.size() + " Size:"+ f.length());
                        }


                        it.remove(); // avoids a ConcurrentModificationException
                    }

                }

            }

            System.out.println("----------------------------------------------------");



        }

    }

    public synchronized void insertFile(FileID fileID) {

        if(!storedFiles.contains(fileID)){
            storedFiles.add(fileID);
            Peer.saveDBToDisk();
        }
        else {
            int i = storedFiles.indexOf(fileID);
            storedFiles.get(i).setNumChunks(fileID.getNumChunks());
        }

    }

    public synchronized void removeFile(FileID fileID){
        storedFiles.remove(fileID);

        Peer.saveDBToDisk();


    }

    public synchronized int getNumChunksOfFile(FileID fID){
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

    public synchronized void increasePerceivedRepDeg(FileChunkID chunkID, int senderID){

        if(perceivedRepDeg.containsKey(chunkID)) {

            if (!perceivedRepDeg.get(chunkID).contains(senderID)) {
                perceivedRepDeg.get(chunkID).add(senderID);
                System.out.println("SAVING DB");
                //dumpPerceived();
                Peer.saveDBToDisk();
            }

        }
        else {
            perceivedRepDeg.put(chunkID, new ArrayList<>());
            perceivedRepDeg.get(chunkID).add(senderID);
        }


    }

    public void dumpPerceived() {
        System.out.println("DUMPING PERCEIVED!");
        for (FileChunkID name: perceivedRepDeg.keySet()){

            String key =name.toString();
            String value = perceivedRepDeg.get(name).toString();
            System.out.println(key + " " + value);

        }

    }

    public void decreasePerceivedRepDeg(FileChunkID chunkID, int senderID){





        if(perceivedRepDeg.containsKey(chunkID)) {

            if (perceivedRepDeg.get(chunkID).contains(senderID)) {
                System.out.println("DECRESING FROM: " + perceivedRepDeg.get(chunkID).size());
                perceivedRepDeg.get(chunkID).remove(senderID);
                if(perceivedRepDeg.get(chunkID).size()==0)
                    perceivedRepDeg.remove(chunkID);

                System.out.println("DECRESING TO: " + perceivedRepDeg.get(chunkID).size());
                System.out.println("SAVING DB");
                Peer.saveDBToDisk();
            }

        }
    }

    public int getPerceivedRepDeg(FileChunkID chunkID){

        //dumpPerceived();

        if(perceivedRepDeg.containsKey(chunkID))
            return perceivedRepDeg.get(chunkID).size();
        else
            return -1;
    }

    public Integer getDesiredRepDeg(FileChunkID chunkID){
        if(desiredRepDeg.containsKey(chunkID))
            return desiredRepDeg.get(chunkID);
        else
            return -1;
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