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
        FileID fID = new FileID(file.getName());
        String filename = new FileID(file.getName()).toString();


        Peer.restoring = true;



        System.out.println("\t Restoring file: " + filename + "\n");
        Peer.getMDRListener().chunksReceived.put(filename, new ArrayList<>());
        ArrayList<FileChunk> chunks = new ArrayList<>();

        ArrayList<FileChunk> fileChunks;

        int fileParts = Peer.getDb().getNumChunksOfFile(fID);


        System.out.println("NUM CHUNKS2: " + fileParts);


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

            File dir = new File("peer"+Peer.getID()+"/restored/");

            if(!dir.exists()) {
                dir.mkdirs();
            }

            FileOutputStream out = null;
            try {
                out = new FileOutputStream("peer"+Peer.getID()+"/restored/" + filename);

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


        Peer.restoring = false;




    }

    private static byte[] concatBytes(byte[] a, byte[] b) throws IOException {


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write( a );
        outputStream.write( b );

        byte result[] = outputStream.toByteArray( );

        return result;
    }
}
