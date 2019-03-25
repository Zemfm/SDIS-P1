package main.java.file;

import java.io.Serializable;

public class FileChunk implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int CHUNK_MAX_SIZE = 64000;
    private int replicationDegree;
    private int chunkNo;
    private FileID fileID;
    private byte[] chunkData;

    public FileChunk(int replicationDegree, int chunkNo, FileID fileID, byte[] chunkData) {
        this.replicationDegree = replicationDegree;
        this.chunkNo = chunkNo;
        this.fileID = fileID;
        this.chunkData = chunkData;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public FileID getFileID() {
        return fileID;
    }

    public byte[] getChunkData() {
        return chunkData;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }
}

