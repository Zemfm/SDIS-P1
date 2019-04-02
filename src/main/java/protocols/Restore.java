package main.java.protocols;

import main.java.file.FileChunk;
import main.java.file.FileChunkID;
import main.java.file.FileID;
import main.java.listeners.Broker;
import main.java.peer.Peer;

import java.io.*;
import java.util.ArrayList;

import static main.java.utils.Constants.*;
import static main.java.utils.Utilities.*;

public class Restore implements Runnable {

    private File file;


    public Restore(File file) {
        this.file = file;
    }

    @Override
    public void run() {
        byte[] fileData = new byte[0];


        /* TODO: SHA */
        String filename = new FileID(file.getName()).toString();


        Peer.restoring = true;

        //TODO: SAVE BACKED UP FILES TO DB: ON ALL PEERS?



        //Check if file was backed up already
        if(Peer.getDb().isFileStored(filename)){
            System.out.println("The file is stored in the database");

            System.out.println("\t Preparing to Restore the File: " + filename + "\n");
            Peer.getMDRListener().chunksReceived.put(filename, new ArrayList<>());
            ArrayList<FileChunk> chunks = new ArrayList<>();

            ArrayList<FileChunk> fileChunks;

            int fileParts = Peer.getDb().getNumChunksOfFile(filename);


            for (int i = 0; i < fileParts; i++) {

                FileChunkID chunkID = new FileChunkID(filename, i);



                //send get chunk
                Broker.sendGETCHUNK(chunkID);

                //receive chunk
                fileChunks = Peer.getMDRListener().chunksReceived.get(filename);


                FileChunk chunkAUX = fileChunks.isEmpty() ? null : Peer.getMDRListener().chunksReceived.get(filename).remove(0);

                while (chunkAUX == null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    chunkAUX = fileChunks.isEmpty() ? null : Peer.getMDRListener().chunksReceived.get(filename).remove(0);
                }

                chunks.add(chunkAUX);


                //System.out.println("DATABASE : ");
                //Peer.getDb().printDatabase();
            }




            for (int i = 0; i < fileParts; i++) {
                FileChunk chunkTmp = null;
                ArrayList<FileChunk> chunkAUX = null;
                for(FileChunk chunk : chunks) {
                    if(chunk.getChunkNo() == i) {
                        chunkTmp = chunk;
                        break;
                    }
                }

                if(chunkTmp == null) {
                    System.out.println("Missing chunk file!!");
                } else {
                    try {
                        fileData = concatBytes(fileData, chunkTmp.getChunkData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                File dir = new File("Restored/");

                if(!dir.exists()) {
                    dir.mkdir();
                }

                FileOutputStream out = null;
                try {
                    out = new FileOutputStream("Restored/" + filename);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    assert out != null;
                    out.write(fileData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        } else {
            System.out.println("\t The file you are trying to restore does not exist. Confirm if it was backed up");
        }


        Peer.restoring = true;




    }

    private static byte[] concatBytes(byte[] a, byte[] b) throws IOException {


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write( a );
        outputStream.write( b );

        byte result[] = outputStream.toByteArray( );

        return result;
    }
}
