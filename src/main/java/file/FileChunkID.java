package main.java.file;


import java.io.Serializable;

public class FileChunkID implements Serializable {

    private static final long serialVersionUID = 1L;


    private String fileID;
    private int chunkNumber;

    public FileChunkID(String fileID, int chunkNumber) {
        this.fileID = fileID;
        this.chunkNumber = chunkNumber;
    }

    public String getFileID() {
        return fileID;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + fileID.hashCode();
        result = 31 * result + chunkNumber;

        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj == null)
            return false;


        FileChunkID other = (FileChunkID) obj;

        if (this.chunkNumber != other.chunkNumber)
            return false;

        return this.fileID.equals(other.fileID);
    }

    @Override
    public String toString() {
        return fileID + "-" + chunkNumber;
    }
}