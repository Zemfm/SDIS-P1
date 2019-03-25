package main.java.peer;

import com.sun.deploy.cache.Cache;
import main.java.listeners.Listener;
import main.java.protocols.Backup;
import main.java.service.RMI;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class Peer implements RMI {


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
        rmi init example:TODO
        1.0, 0, 192.168.0.1, 224.0.0.0:8000, 224.0.0.0:8001, 224.0.0.0:8002

        normal peer example:
        java peer.Peer 1.0 1 224.0.0.0:8000 224.0.0.0:8001 224.0.0.0:8002
    */
    public static void main(String[] args) throws IOException {



        //this line is used in macOS
        //-Djava.net.preferIPv4Stack=true works better
        //System.setProperty("java.net.preferIPv4Stack", "true");


        if(!parseArgs(args)) {
            System.out.println("Bad arguments");
            System.out.println("USAGE (RMI): java peer.Peer <protocol_version> <service_access_point> <MCADDR>:<MCPORT> " +
                    "<MDBADDR>:<MDBPORT> <MDRADDR>:<MDRPORT>");
            System.out.println("USAGE (NON-RMI): java peer.Peer <protocol_version> <MCADDR>:<MCPORT> " +
                    "<MDBADDR>:<MDBPORT> <MDRADDR>:<MDRPORT>");
            return;
        }


        //TODO: test this. This is used to ensure that a peer isn't sending chunks to himself
        //see Listener.java TODO same as Utilities.getLocalAdress()? move this there
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

        return;





    }


    private static boolean parseArgs(String[] args) {

        //args == 6 -> INITIALIZE RMI
        if(args.length == 6) {

            protocolVersion = Float.parseFloat(args[0]);
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

            protocolVersion = Float.parseFloat(args[0]);
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
        else return false;


        return true;
    }

    private static void launchRMI() {

        System.setProperty("java.rmi.server.hostname", "localhost");
        //po meu mac
        System.setProperty("rmi.server.codebase", "file:/Users/zemiguel/IdeaProjects/SDIS-P1/src/main/java/service/bin/");
       /* try {


            RMI peer = new Peer();

            RMI stub = (RMI) UnicastRemoteObject.exportObject(peer, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("obj", stub);

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }*/

        try {
            try {
                LocateRegistry.createRegistry(SERVER_PORT);
            } catch (RemoteException e) {
                LocateRegistry.getRegistry(SERVER_PORT);
                e.printStackTrace();
            }
            System.setProperty("java.rmi.server.hostname", SERVER_HOST);
            connectionRequestHandler = new ConnectionRequestHandlerImpl();
            dataRequestHandler = new DataRequestHandlerImpl();
            String rmiUrl = "rmi://" + SERVER_HOST + ":" + SERVER_PORT + "/";
            Naming.rebind(rmiUrl + "ConnectionRequestHandler", connectionRequestHandler);
            Naming.rebind(rmiUrl + "DataRequestHandler", dataRequestHandler);
        } catch (RemoteException e1) {
            e1.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }



    }


    //TODO
    /*public static null getDb() {


    }*/

    public static Listener getMcListener() {
        return MCChannel;
    }

    @Override
    public void backup(File file, int replicationDegree) throws RemoteException {

        Thread t = new Thread(new Backup(file, replicationDegree));
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void delete(String filePath) throws RemoteException {

    }

    @Override
    public void restore(File file) throws RemoteException {

    }

    @Override
    public String state() throws RemoteException {
        return null;
    }

    @Override
    public void reclaim(int amount) throws RemoteException {

    }




    public static int getID() {
        return ID;
    }

    public static InetAddress getAddress() {
        return ip;
    }

    public static float getProtocolVersion() {
        return protocolVersion;
    }


}
