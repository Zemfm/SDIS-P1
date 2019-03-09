package main.java.peer;

import main.java.listeners.Listener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.io.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class Peer {


    private static Listener MCChannel;  //MC CHANNEL
    private static Listener MDBChannel; //BACKUP CHANNEL
    private static Listener MDRChannel; //RESTORE CHANNEL

    private static String rmiRemoteObject;

    private static int ID;
    private static float protocolVersion;

    private static InetAddress MCAddress;
    private static InetAddress MDBAddress;
    private static InetAddress MDRAddress;

    private static int MCPort;
    private static int MDBPort;
    private static int MDRPort;

    private static InetAddress ip;

    /*
        usage:
        protocol version,the server id, service access point, MC, MDB, MDR
        rmi init example:
        1.0, 0, 192.168.0.1, 224.0.0.0:8000, 224.0.0.0:8001, 224.0.0.0:8002

        normal peer example:
        1.0, 1, 224.0.0.0 8000 224.0.0.0 8001 224.0.0.0 8002
    */
    public static void main(String[] args) throws IOException {



        //this line is used in macOS
        System.setProperty("java.net.preferIPv4Stack", "true");





        if(!parseArgs(args)) {
            System.out.println("Bad arguments");
            return;
        }


        //TODO: test this. This is used to ensure that a peer isn't sending chunks to himself
        //see Listener.java TODO
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            //ip = socket.getLocalAddress().getHostAddress(); this returns a string
            ip = socket.getLocalAddress(); //this returns a InetAddress
        }

        MCChannel = new Listener(MCAddress, MCPort);
        MDBChannel = new Listener(MDBAddress, MDBPort);
        MDRChannel = new Listener(MDRAddress, MDRPort);




        System.out.println("Start Listening on MC Channel...");
        new Thread(MCChannel).start();
        System.out.println("Start Listening on MDB Channel...");
        new Thread(MDBChannel).start();
        System.out.println("Start Listening on MDR Channel...");
        new Thread(MDRChannel).start();





    }


    private static boolean parseArgs(String[] args) {

        //args == 6 -> INITIALIZE RMI
        if(args.length == 6) {

            protocolVersion = Integer.parseInt(args[0]);
            ID = Integer.parseInt(args[1]);
            rmiRemoteObject = args[2];
            try {

                MCAddress = InetAddress.getByName(args[3].split(":")[0]);
                MDBAddress = InetAddress.getByName(args[4].split(":")[0]);
                MDRAddress = InetAddress.getByName(args[5].split(":")[0]);

                MCPort = Integer.parseInt(args[3].split(":")[1]);
                MDBPort = Integer.parseInt(args[4].split(":")[1]);
                MDRPort = Integer.parseInt(args[5].split(":")[1]);



            }
            catch (UnknownHostException e){
                System.out.println("Address not found");
                return false;
            }

            System.out.println("\t LAUNCHING RMI");
            launchRMI();

        } else if (args.length == 5) {//normal peer

            protocolVersion = Integer.parseInt(args[0]);
            ID = Integer.parseInt(args[1]);

            try {

                MCAddress = InetAddress.getByName(args[2].split(":")[0]);
                MDBAddress = InetAddress.getByName(args[3].split(":")[0]);
                MDRAddress = InetAddress.getByName(args[4].split(":")[0]);

                MCPort = Integer.parseInt(args[2].split(":")[1]);
                MDBPort = Integer.parseInt(args[3].split(":")[1]);
                MDRPort = Integer.parseInt(args[4].split(":")[1]);



            }
            catch (UnknownHostException e){
                System.out.println("Address not found");
                return false;
            }


        }


        return false;
    }

    private static void launchRMI() {

        //TODO
    }


    public static int getID() {
        return ID;
    }

    public static InetAddress getAddress() {
        return ip;
    }
}
