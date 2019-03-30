package main.java.file;

import java.io.Serializable;

public class FileID implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fileID;
    private int numChunks;


    public FileID(String fileID) {
        this.fileID = fileID;
        this.numChunks = -1;

    }

    @Override
    public String toString() {
        String[] fileIDSplitted = fileID.split("/");
        return fileIDSplitted[fileIDSplitted.length - 1];
    }

    public void setNumChunks(int numChunks) {
        this.numChunks = numChunks;
    }

    public int getNumChunks() {
        return numChunks;
    }
}