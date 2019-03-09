package main.java.utils;

public class Constants {

    public static final int CHUNK_MAX_SIZE = 64000;
    public static final String PUTCHUNK = "PUTCHUNK";
    public static final String STORED = "STORED";
    public static final String REMOVED = "REMOVED";
    public static final String GETCHUNK = "GETCHUNK";
    public static final String DELETE = "DELETE";
    public static final String CHUNK = "CHUNK";
    public static final String PROTOCOL_VERSION= "1.0";
    public static final String MESSAGE_SEPARATOR = " ";
    private final static char CR  = (char) 0x0D;
    private final static char LF  = (char) 0x0A;
    public final static String CRLF  = "" + CR + LF;
    public static final String ENCODING = "ISO-8859-1";
    public static final int MAX_WAITING_TIME = 400;
    public static final int DISK_SIZE = 2560;

}