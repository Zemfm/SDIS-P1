package main.java.service;


import main.java.file.FileChunkID;
import main.java.file.FileID;
import main.java.listeners.Broker;
import main.java.peer.Peer;
import main.java.utils.Constants.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

import static main.java.utils.Constants.*;
import static main.java.utils.Utilities.getLocalAddress;

public class PacketHandler implements Runnable {

    private DatagramPacket packetToHandle;
    private String packet_header;
    private byte[] packet_body;
    private String subprotocol;
    private FileID fileID;
    private float protocolVersion;
    private int replicationDegree;
    private int chunkNo;
    private String senderID;
    private int bodyStartingIndex;
    private String[] header_splitted;

    private InetAddress senderIP;


    public PacketHandler(DatagramPacket packetToHandle) {
        this.packetToHandle = packetToHandle;
        packet_header = null;
        packet_body = null;
        header_splitted = null;
        senderIP = packetToHandle.getAddress();
    }

    @Override
    public void run() {
        //System.out.println("HANDLER RUNNING...");
        //System.out.println("PACKET HEADER LENGTH " + packet_header.length());
        //int body_index_start = packet_header.length() + (2* CRLF.getBytes().length);
        //System.out.println(body_index_start);

        parseSubProtocol();
        //parseHeader();

        switch (subprotocol) {
            case PUTCHUNK:
                parsePUTCHUNK();
                try {
                    PUTCHUNKHandler();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    }

    private void REMOVEDHandler() {

    }

    private void parseDELETE() {

    }

    private void DELETEHandler() {

    }

    private void parseCHUNK() {

    }

    private void CHUNKHandler() {

    }

    private void parseGETCHUNK() {

    }

    private void GETCHUNKHandler() {

    }

    private void parseSTORED() {

    }

    private void STOREDHandler() {

    }


    private void parsePUTCHUNK() {


        replicationDegree = Integer.parseInt(header_splitted[5]);
        protocolVersion = Float.parseFloat(header_splitted[1]);
        senderID = header_splitted[2];
        fileID = new FileID(header_splitted[3]);
        chunkNo = Integer.parseInt(header_splitted[4]);

    }

    private void PUTCHUNKHandler() throws IOException {

        parseBody();
        File dir = new File("Chunks/");
        boolean test = false;

        if(!dir.exists()){

            System.out.println("creating directory: " + dir.getName());

            try{
                dir.mkdir();
            }
            catch(SecurityException se){
                se.printStackTrace();
            }

        }

        packet_body = Arrays.copyOfRange(packetToHandle.getData(), bodyStartingIndex, packetToHandle.getLength());
        FileID fileID = new FileID(header_splitted[3]);
        String fileName = fileID.toString();
        FileChunkID chunkID = new FileChunkID(fileName, Integer.parseInt(header_splitted[4]));
        System.out.println("\t FILEID : " + fileName + "\n"
                + "\t CHUNK NO : " + chunkNo+ "\n");
        System.out.println("\t PUT CHUNK " + header_splitted[3] + " with Replication Degree : " + replicationDegree  + " in " + "./Chunks");
        System.out.println("Chunk ID: " + chunkID.toString());
        File chunkfile = new File("Chunks/" + chunkID.toString());



        Peer.getMDBListener().countPutChunk(chunkID, senderID);

        if(chunkfile.exists()) {
            /* try {
                System.out.println("A chunk with the same name already exists, send STORED but not backing up...");
                Thread.sleep((long)(Math.random() * MAX_WAITING_TIME)); //acho que aqui n precisa de sleep
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }*/
            Broker.sendSTORED(chunkID);
        } else {

            if(senderIP != getLocalAddress()){
                try {
                    System.out.println("Entrei!!!");
                    FileOutputStream out = new FileOutputStream("Chunks/" + chunkID.toString());
                    System.out.println("Saving Chunk...");
                    out.write(packet_body);
                    out.close();

                    try{
                        System.out.println("Sending STORED response");
                        Thread.sleep((long)(Math.random() * MAX_WAITING_TIME)); //acho que aqui n precisa de sleep
                    } catch (InterruptedException ie){
                        ie.printStackTrace();
                    }
                    Broker.sendSTORED(chunkID);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                System.out.println("I");

            }

        }





    }

    /**
     * In this function we parse the Message Body which contains the Chunk Data. However, we don't know in which position the body starts
     * to parse the body we need to know the total length of the header lines, the number of lines and the total size occupied by CRLF
     * This way , when we sum the totalHeaderLinesLength with NumLines*CRLF.length we get the starting index of the body
     */
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
