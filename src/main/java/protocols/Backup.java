package main.java.protocols;

import main.java.file.*;
import main.java.peer.Peer;


import java.io.*;
import java.util.Arrays;

import static main.java.utils.Constants.*;
import static main.java.utils.Utilities.*;

public class Backup implements Runnable{

    private File file;
    private int repDeg;
    private String encryptedID;
    private FileID fileID;
    private int fileParts;
    //private static Message message;


    public Backup(File file, int replicationDegree) {

        this.file = file;
        this.repDeg = replicationDegree;

    }

    /**
     * @param file File to split into chunks and later backed up
     * @return Number of chunks that the file was split into
     * @throws IOException exception to be thrown in case of an invalid file.
     */
    public int createChunks(File file) throws IOException {


        byte[] fileData = loadFileData(file);

        fileParts = fileData.length / CHUNK_MAX_SIZE;


        String fileName = file.getName();

        ByteArrayInputStream streamBuffer = new ByteArrayInputStream(fileData);
        byte[] data = new byte[CHUNK_MAX_SIZE];


        for(int i = 0; i <= fileParts; i++) {
            FileChunkID id = new FileChunkID(file.getName(), i);


            byte[] chunkData;

            /*
                Size of last chunk is always shorter than CHUNK_MAX_SIZE
                If the file size is a multiple of CHUNK_MAX_SIZE, the last chunk has size 0.
             */

            if(i == fileParts - 1 && file.length() % CHUNK_MAX_SIZE == 0) {
                chunkData = new byte[0];
            } else {
                int bytesRead = streamBuffer.read(data, 0, data.length);
                chunkData = Arrays.copyOfRange(data, 0, bytesRead);
            }


            /* TODO: SHA */
            fileID = new FileID(file.getName(), repDeg);

            fileID.setNumChunks(fileParts + 1);


            FileChunk chunk = new FileChunk(repDeg, i, fileID, chunkData);

            Thread t = new Thread(new BackupChunk(chunk));
            t.start();

            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Peer.saveDBToDisk();


        return fileParts;

    }


    @Override
    public void run() {
        //String fileIDString;
        //fileIDString = file.getName() + file.getPath() + file.lastModified();
        //encryptedID = sha256(fileIDString);
        try {
            createChunks(file);
        } catch (IOException e) {
            e.printStackTrace();
        }



        Peer.getDb().insertFile(fileID);






    }

    public static byte[] loadFileData(File file) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(file);
        byte[] fileData = new byte[(int) file.length()];

        try {
            inputStream.read(fileData);
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }



        return fileData;
    }






}
