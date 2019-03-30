package main.java.service;


import main.java.file.FileChunk;
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
    private int senderID;
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

        /* TODO: should be boolean */
        parseSubProtocol();
        //parseHeader();


        /* TODO: parse should be boolean */
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

                default: System.out.println("Unknown protocol. Ignoring message...");
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

        /*
        TODO:
        Upon receiving this message, a peer that has a local copy of the chunk shall
        update its local count of this chunk. If this count drops below the desired replication
        degree of that chunk, it shall initiate the chunk backup subprotocol after a random delay
         uniformly distributed between 0 and 400 ms. If during this delay, a peer receives a PUTCHUNK
          message for the same file chunk, it should back off and restrain from starting yet another
           backup subprotocol for that file chunk.
         */

    }

    private void parseDELETE() {

    }

    private void DELETEHandler() {

        /*

            Enhancement: If a peer that backs up some chunks of the file is not running at the time
            the initiator peer sends a DELETE message for that file, the space used by these chunks
            will never be reclaimed. Can you think of a change to the protocol, possibly including
            additional messages, that would allow to reclaim storage space even in that event?

            Os peers respondem quando fazem delete, o initiator peer diminui o repdegree, se chegar a 0
            todos os peers que tinham esse chunk apagaram. Se nao chegar a 0, é pq há um peer offline
            que não apagou. Nesse caso, guardar numa hash, o chunkID e repDegree.
            Quando um peer se liga, pergunta no broadcast se existem chunks por apagar, se alguém responder,
            procurar na pasta chunk pelo chunk, ao encontrar, apaga e o initiator peer que avisou que havia
            um chunk por apagar, diminui o rep degree, se chegar a 0 apaga esse valor da hash.

            OU: (pior, mas mais simples)
            Quando um peer inicia, verificar se tem chunks na pasta /chunks, se tiver, enviar uma mensagem
            DELETEENH para o MC (por cada chunk, esperando por respostas depois de cada envio)
            a perguntar se o chunk foi apagado, se receber uma resposta, apaga esse chunk.
             O(s) peer(s) precisariam de guardar um historico de DELETES. (talvez cada peer ter um historico
             de cada DELETE msg que iniciou)

         */

    }

    private void parseCHUNK() {

        //CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>

        protocolVersion = Float.parseFloat(header_splitted[1]);
        senderID = Integer.parseInt(header_splitted[2]);
        fileID = new FileID(header_splitted[3]);
        chunkNo = Integer.parseInt(header_splitted[4]);
        parseBody();

    }

    private void CHUNKHandler() {



        FileChunk chunk = new FileChunk(-1, chunkNo, fileID, packet_body);


        Peer.getMDRListener().queueChunk(chunk);
        //System.out.println("\t CHUNKS RECEIVED : ");
        //Peer.getMDRListener().printChunksReceived();

    }

    private void parseGETCHUNK() {

        /* GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */

        protocolVersion = Float.parseFloat(header_splitted[1]);
        senderID = Integer.parseInt(header_splitted[2]);
        fileID = new FileID(header_splitted[3]);
        chunkNo = Integer.parseInt(header_splitted[4]);


    }

    private void GETCHUNKHandler() {


        /*
        Enhancement: If chunks are large, this protocol may not be desirable: only one peer
        needs to receive the chunk, but we are using a multicast channel for sending the chunk.
        Can you think of a change to the protocol that would eliminate this problem, and yet interoperate
         with non-initiator peers that implement the protocol described in this section? Your enhancement
          must use TCP to get full credit.


        Esta msg "CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>"
        passa a ser "CHUNKENH <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>",
        depois liga-se por TCP ao ipeer e envia o chunk?
        Para ligar por TCP:
        O initiator peer que comecou o protocolo de restore, abre um servidor TCP,
        depois na msg GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        envia a port onde os peers se devem ligar para enviar o chunk.
        O ip pode ser descoberto quando um peer recebe a msg GETCHUNK (senderIP = packetToHandle.getAddress();).

        ex TCP: https://www.pegaxchange.com/2017/12/07/simple-tcp-ip-server-client-java/

         */

        FileChunkID chunkID = new FileChunkID(fileID.toString(), chunkNo);
        FileInputStream is = null;
        File chunkFile = new File("Chunks/" + chunkID);
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

        try { //TODO: DO NOT SEND IF I RECEIVE A CHUNK MESSAGE DURING THIS
            Thread.sleep((long)(Math.random() * MAX_WAITING_TIME));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Broker.sendCHUNK(chunk, chunkID);








    }

    private void parseSTORED() {
        //STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>

        protocolVersion = Float.parseFloat(header_splitted[1]);
        senderID = Integer.parseInt(header_splitted[2]);
        fileID = new FileID(header_splitted[3]);
        chunkNo = Integer.parseInt(header_splitted[4]);

    }

    private void STOREDHandler() {

        FileChunkID fcID = new FileChunkID(fileID.toString(), chunkNo);
        Peer.getMCListener().countStored(fcID, String.valueOf(senderID));

    }


    private void parsePUTCHUNK() {


        replicationDegree = Integer.parseInt(header_splitted[5]);
        protocolVersion = Float.parseFloat(header_splitted[1]);
        senderID = Integer.parseInt(header_splitted[2]);
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





        /* TODO: not needed? */
        Peer.getMDBListener().countPutChunk(chunkID, String.valueOf(senderID));

        if(chunkfile.exists()) {
            System.out.println("\n\t I already have this chunk, sending STORED...");
            Broker.sendSTORED(chunkID);
        } else {



            try {
                FileOutputStream out = new FileOutputStream("Chunks/" + chunkID.toString());
                System.out.println("Saving Chunk...");
                out.write(packet_body);
                out.close();

                try{
                    System.out.println("Sending STORED response");
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
