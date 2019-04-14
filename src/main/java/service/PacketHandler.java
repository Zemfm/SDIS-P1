package main.java.service;


import main.java.file.FileChunk;
import main.java.file.FileChunkID;
import main.java.file.FileID;
import main.java.listeners.Broker;
import main.java.peer.Peer;
import main.java.protocols.Backup;
import main.java.protocols.BackupChunk;
import main.java.utils.Constants.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static main.java.utils.Constants.*;
import static main.java.utils.Utilities.getLocalAddress;
import static main.java.utils.Utilities.sha256;

public class PacketHandler implements Runnable {

    private DatagramPacket packetToHandle;
    private String packet_header;
    private byte[] packet_body;
    private String subprotocol;
    private FileID fileID;
    private float protocolVersion;
    private int replicationDegree;
    private int chunkNo;
    private int senderID;
    private int bodyStartingIndex;
    private String[] header_splitted;

    private InetAddress senderIP;

    private boolean listeningChunk;
    private boolean receivedChunk;
    private boolean badMessage;


    public PacketHandler(DatagramPacket packetToHandle) {
        this.packetToHandle = packetToHandle;
        packet_header = null;
        packet_body = null;
        header_splitted = null;
        senderIP = packetToHandle.getAddress();
        listeningChunk = false;
        receivedChunk = false;
        badMessage = false;
    }

    @Override
    public void run() {

        parseSubProtocol();
        //parseHeader();


        if(Integer.parseInt(header_splitted[2]) != Peer.getID()){

            switch (subprotocol) {
                case PUTCHUNK:
                    parsePUTCHUNK();
                    PUTCHUNKHandler();
                    break;
                case STORED:
                    parseSTORED();
                    STOREDHandler();
                    break;
                case GETCHUNK:
                    parseGETCHUNK();
                    GETCHUNKHandler();
                    break;
                case CHUNK:
                    parseCHUNK();
                    CHUNKHandler();
                    break;
                case DELETE:
                    parseDELETE();
                    DELETEHandler();
                    break;
                case REMOVED:
                    parseREMOVED();
                    REMOVEDHandler();
                    break;

                default: System.out.println("Unknown protocol. Ignoring message... " + subprotocol);
                    badMessage = true;
                    break;

            }

            if(!badMessage)
                System.out.println("\t Sender ID: " + packetToHandle.getAddress() + " \n" +
                        "\t PEER ID : " + senderID + "\n");


        }



    }

    private void parseSubProtocol() {

        ByteArrayInputStream stream = new ByteArrayInputStream(packetToHandle.getData());
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        try {
            packet_header = reader.readLine();
            header_splitted = packet_header.split(" ");
            subprotocol = header_splitted[0];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void parseREMOVED() {

        protocolVersion = Float.parseFloat(header_splitted[1]);
        senderID = Integer.parseInt(header_splitted[2]);
        fileID = new FileID(sha256(header_splitted[3]), -1);
        chunkNo = Integer.parseInt(header_splitted[4]);



    }

    private void REMOVEDHandler() {


        if(Peer.getDb().isFileStored(fileID)){
            FileChunkID chunkID = new FileChunkID(fileID.toString(), chunkNo);
            Peer.getDb().decreasePerceivedRepDeg(chunkID , senderID);

            System.out.println("HANDLER REMOVED: "+ Peer.getDb().getPerceivedRepDeg(chunkID) + "/" +
                    Peer.getDb().getDesiredRepDeg(chunkID));


            if(Peer.getDb().getPerceivedRepDeg(chunkID) < Peer.getDb().getDesiredRepDeg(chunkID)){
                System.out.println("\tPerceived rep degree dropped below desired rep degree.");

                Peer.getMCListener().startCountingPutChunks(chunkID);

                try{
                    System.out.println("\tWaiting for Putchunk messages...");
                    Thread.sleep((long)(Math.random() * MAX_WAITING_TIME));
                } catch (InterruptedException ie){
                    ie.printStackTrace();
                }

                int putChunksReceived = Peer.getMCListener().getCountPutChunks(chunkID);

                Peer.getMCListener().stopSavingPutChunks(chunkID);

                if (putChunksReceived == 0){

                    try {
                        File cFile = new File("peer"+Peer.getID()+"/Backup/"+
                                fileID.toString().split("\\.")[0]+ "/"+chunkID.toString());




                        byte[] data = Backup.loadFileData(cFile);

                        //FileChunk(int replicationDegree, int chunkNo, FileID fileID, byte[] chunkData)
                        fileID = new FileID(fileID.toString(), Peer.getDb().getDesiredRepDeg(chunkID));
                        FileChunk fChunk = new FileChunk(Peer.getDb().getDesiredRepDeg(chunkID), chunkNo,
                                fileID, data);

                        new Thread(new BackupChunk(fChunk)).start();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


                }


            }

        }






    }

    private void parseDELETE() {


        protocolVersion = Float.parseFloat(header_splitted[1]);
        senderID = Integer.parseInt(header_splitted[2]);
        fileID = new FileID(sha256(header_splitted[3]), -1);

    }

    private void DELETEHandler() {



        final File folder = new File("peer"+Peer.getID()+"/Backup/"+fileID.toString()+
                "/");

        System.out.println("FOLDER: " + folder.getPath());

        final File[] files = folder.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( final File dir,
                                   final String name ) {
                return name.matches( fileID.toString() + "-.*" );
            }
        } );
        for ( final File file : files ) {
            if ( !file.delete() ) {
                System.err.println( "Can't remove " + file.getAbsolutePath() );
            }
            else {
                Peer.getDisk().removeFile(file.length());
                Peer.getDb().removeChunkInfo(new FileChunkID(fileID.toString(),
                        Integer.parseInt(file.getName().split("-")[1])));

                Peer.getDb().removeFile(fileID);
            }
        }





    }

    private void parseCHUNK() {

        //CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
        if(listeningChunk)
            receivedChunk=true;

        protocolVersion = Float.parseFloat(header_splitted[1]);
        senderID = Integer.parseInt(header_splitted[2]);
        fileID = new FileID(header_splitted[3], -1);
        chunkNo = Integer.parseInt(header_splitted[4]);
        parseBody();

    }

    private void CHUNKHandler() {


        if(Peer.restoring){
            FileChunk chunk = new FileChunk(-1, chunkNo, fileID, packet_body);


            Peer.getMDRListener().queueChunk(chunk);
            //System.out.println("\t CHUNKS RECEIVED : ");
            //Peer.getMDRListener().printChunksReceived();
        }


    }

    private void parseGETCHUNK() {

        /* GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */

        protocolVersion = Float.parseFloat(header_splitted[1]);
        senderID = Integer.parseInt(header_splitted[2]);
        fileID = new FileID(header_splitted[3], -1);
        chunkNo = Integer.parseInt(header_splitted[4]);


    }

    private void GETCHUNKHandler() {


        FileChunkID chunkID = new FileChunkID(fileID.toString(), chunkNo);
        FileInputStream is = null;
        File chunkFile = new File("peer"+Peer.getID()+"/Backup/"+fileID.toString().split("\\.")[0]+ "/" + chunkID);
        try {
            is  = new FileInputStream(chunkFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] chunkData = new byte[(int) chunkFile.length()];

        try {
            assert is != null;
            is.read(chunkData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        FileChunk chunk = new FileChunk(-1, chunkID.getChunkNumber(), fileID, chunkData);

        /*

        To avoid flooding the host with CHUNK messages, each peer shall wait for a random time
        uniformly distributed between 0 and 400 ms, before sending the CHUNK message. If it receives
        a CHUNK message before that time expires, it will not send the CHUNK message.

         */

        try {
            listeningChunk = true;
            Thread.sleep((long)(Math.random() * MAX_WAITING_TIME));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(!receivedChunk)
            Broker.sendCHUNK(chunk, chunkID);

        receivedChunk = false;
        listeningChunk = false;


    }

    private void parseSTORED() {
        //STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>

        protocolVersion = Float.parseFloat(header_splitted[1]);
        senderID = Integer.parseInt(header_splitted[2]);
        fileID = new FileID(header_splitted[3], -1);
        chunkNo = Integer.parseInt(header_splitted[4]);

    }

    private void STOREDHandler() {

        FileChunkID fcID = new FileChunkID(fileID.toString(), chunkNo);
        Peer.getMCListener().countStored(fcID, String.valueOf(senderID));
        Peer.getDb().increasePerceivedRepDeg(new FileChunkID(fileID.toString(), chunkNo), senderID);

    }


    private void parsePUTCHUNK() {


        replicationDegree = Integer.parseInt(header_splitted[5]);
        protocolVersion = Float.parseFloat(header_splitted[1]);
        senderID = Integer.parseInt(header_splitted[2]);
        fileID = new FileID(header_splitted[3], replicationDegree);
        chunkNo = Integer.parseInt(header_splitted[4]);
        parseBody();

    }

    private void PUTCHUNKHandler() {



        File dir = new File("peer"+Peer.getID()+"/Backup/"+fileID.toString().split("\\.")[0]+ "/");

        if(!dir.exists()){

            System.out.println("creating directory: " + dir.getName());

            try{
                dir.mkdirs();
            }
            catch(SecurityException se){
                se.printStackTrace();
            }

        }



        FileChunkID chunkID = new FileChunkID(fileID.toString(), chunkNo);
        System.out.println("\t FILEID : " + fileID.toString() + "\n"
                + "\t CHUNK NO : " + chunkNo+ "\n");
        System.out.println("\t PUT CHUNK: " + fileID.toString() + " with Replication Degree : " + replicationDegree  + " in " + dir.getPath());
        System.out.println("\t Chunk ID: " + chunkID.toString());





        File chunkfile = new File(dir.getPath()+"/"+ chunkID.toString());





        Peer.getMDBListener().countPutChunk(chunkID, String.valueOf(senderID));

        if(chunkfile.exists()) {
            System.out.println("\n\t I already have this chunk, sending STORED...");
            Broker.sendSTORED(chunkID);
        } else {

            if (Peer.getDisk().saveFile(packet_body.length)){

                try {
                    FileOutputStream out = new FileOutputStream(dir.getPath()+"/" + chunkID.toString());
                    System.out.println("\n\t Saving Chunk...\n");
                    out.write(packet_body);
                    out.close();

                    Peer.getDb().insertFile(fileID);
                    Peer.getDb().insertChunkInfo(fileID, replicationDegree, chunkNo, Peer.getID());

                    try{
                        System.out.println("\tSending STORED response...");
                        Thread.sleep((long)(Math.random() * MAX_WAITING_TIME));
                    } catch (InterruptedException ie){
                        ie.printStackTrace();
                    }

                    Broker.sendSTORED(chunkID);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        }





    }

    private void parseBody() {

        ByteArrayInputStream stream = new ByteArrayInputStream(packetToHandle.getData());
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        String line = null;
        int totalHeaderLinesLength = 0;
        int totalLines = 0;

        do {
            try {
                line = reader.readLine();
                totalHeaderLinesLength += line.length();
                totalLines++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (!(line != null && line.isEmpty()));

        bodyStartingIndex = totalHeaderLinesLength + (totalLines * CRLF.getBytes().length);

        packet_body = Arrays.copyOfRange(packetToHandle.getData(), bodyStartingIndex,
                packetToHandle.getLength());

        //System.out.println("STARTING INDEX:"  + bodyStartingIndex);
    }





}
