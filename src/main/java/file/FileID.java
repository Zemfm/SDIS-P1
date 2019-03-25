package main.java.file;

import java.io.Serializable;

public class FileID implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fileID;


    public FileID(String fileID) {
        this.fileID = fileID;

    }

    @Override
    public String toString() {
        String[] fileIDSplitted = fileID.split("/");
        return fileIDSplitted[fileIDSplitted.length - 1];
    }
}