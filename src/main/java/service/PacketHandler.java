package main.java.service;


import main.java.utils.Constants.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;

import static main.java.utils.Constants.*;

public class PacketHandler implements Runnable {

    private DatagramPacket packetToHandle;
    private String packet_header;
    private byte[] packet_body;
    private String subprotocol;
    //private FileID fileID;
    private int protocolVersion;
    private int replicationDegree;
    private int chunkNo;
    private String senderID;
    private int bodyStartingIndex;
    private String[] header_splitted;


    public PacketHandler(DatagramPacket packetToHandle) {
        this.packetToHandle = packetToHandle;
        packet_header = null;
        packet_body = null;
        header_splitted = null;
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


    }

    private void PUTCHUNKHandler() {

    }





}
