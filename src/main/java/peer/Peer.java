package main.java.peer;


import main.java.database.Database;
import main.java.file.FileID;
import main.java.listeners.Listener;
import main.java.protocols.Backup;
import main.java.protocols.Delete;
import main.java.protocols.Reclaim;
import main.java.protocols.Restore;
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

import static main.java.utils.Utilities.getLocalAddress;
import static main.java.utils.Constants.*;

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

    private static volatile Database db;
    private static Disk disk;

    public static boolean restoring;

    /*
        javac -cp /Users/zemiguel/IdeaProjects/SDIS-P1/src/ peer/Peer.java
        DENTRO DO /src/: rmiregistry &
        usage:
        protocol version,the server id, service access point, MC, MDB, MDR
        rmi init example:
        java -Djava.net.preferIPv4Stack=true -Djava.rmi.server.codebase=file:/Users/zemiguel/IdeaProjects/SDIS-P1/src/
        main/java/service/ main/java/peer/Peer 1.0 0 192.168.0.1 224.0.0.0:8000 224.0.0.0:8001 224.0.0.0:8002
        1.0, 0, 192.168.0.1, 224.0.0.0:8000, 224.0.0.0:8001, 224.0.0.0:8002
        normal peer example:
        java peer.Peer 1.0 1 224.0.0.0:8000 224.0.0.0:8001 224.0.0.0:8002
    */
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        if(!parseArgs(args)) {
            System.out.println("Bad arguments");
            System.out.println("USAGE (RMI): java peer.Peer <protocol_version> <service_access_point> <MCADDR>:<MCPORT> " +
                    "<MDBADDR>:<MDBPORT> <MDRADDR>:<MDRPORT>");
            System.out.println("USAGE (NON-RMI): java peer.Peer <protocol_version> <MCADDR>:<MCPORT> " +
                    "<MDBADDR>:<MDBPORT> <MDRADDR>:<MDRPORT>");
            return;
        }


        ip = getLocalAddress();

        MCChannel = new Listener(MCAddress, MCPort);
        MDBChannel = new Listener(MDBAddress, MDBPort);
        MDRChannel = new Listener(MDRAddress, MDRPort);

        loadDisk();
        loadDatabase();

        db.printDatabase();
        disk.printDisk();
        restoring = false;


        System.out.println("Start Listening on MC Channel...");
        new Thread(MCChannel).start();
        System.out.println("Start Listening on MDB Channel...");
        new Thread(MDBChannel).start();
        System.out.println("Start Listening on MDR Channel...");
        new Thread(MDRChannel).start();

        return;





    }

    public static void saveDBToDisk() {

        File dir = new File("peer"+getID()+"/database/");

        if(!dir.exists()){
            System.out.println("creating directory: " + dir.getName());

            try{
                dir.mkdirs();
            }
            catch(SecurityException se){
                se.printStackTrace();
            }

        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("peer"+getID()+"/database/dbs.data");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Database does not exist!");
            createDB();
            System.out.println("New DB created and saved to disk...");
        }
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assert oos != null;
            oos.writeObject(db);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void createDB() {
        db = new Database();
        saveDBToDisk();
    }

    private static void loadDatabase() {
        System.out.println("Loading database...");
        try {
            FileInputStream fileInputStream = new FileInputStream("peer"+getID()+"/database/dbs.data");

            ObjectInputStream objectInputStream = new ObjectInputStream(
                    fileInputStream);
            db = (Database) objectInputStream.readObject();
            objectInputStream.close();
        } catch (FileNotFoundException e) {
            System.out.println("Database not found");

            createDB();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
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

        //System.setProperty("java.rmi.server.hostname", "localhost");

        //System.setProperty("rmi.server.codebase", "file:/Users/zemiguel/IdeaProjects/SDIS-P1/src/main/java/service/bin/");
        try {


            RMI peer = new Peer();

            RMI stub = (RMI) UnicastRemoteObject.exportObject(peer, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("obj", stub);

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }



    }



    public static Database getDb() {
        return db;

    }

    public static Listener getMCListener() {
        return MCChannel;
    }

    public static Listener getMDBListener() {
        return MDBChannel;
    }

    public static Listener getMDRListener() {
        return MDRChannel;
    }

    public static void saveDisk(){
        FileOutputStream fos = null;
        File dir = new File("peer"+getID()+"/disk/");

        if(!dir.exists()){
            System.out.println("creating directory: " + dir.getName());

            try{
                dir.mkdirs();
            }
            catch(SecurityException se){
                se.printStackTrace();
            }

        }

        try {
            fos = new FileOutputStream("peer"+getID()+"/disk/disk.data");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Disk does not exist!");
            createDisk();
            System.out.println("New Disk created and saved to disk...");
        }
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            oos.writeObject(disk);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadDisk() throws ClassNotFoundException, IOException {

        System.out.println("Loading disk...");

        try {
            FileInputStream fileInputStream = new FileInputStream("peer"+getID()+"/disk/disk.data");

            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            disk = (Disk) objectInputStream.readObject();
            objectInputStream.close();
        } catch (FileNotFoundException e) {
            System.out.println("Disk not found");
            createDisk();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void createDisk(){
        disk = new Disk();
        saveDisk();
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
        Thread t = new Thread(new Delete(new FileID(filePath, 0)));

        t.start();

        try {
            t.join();
        } catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    @Override
    public void restore(File file) throws RemoteException {

        Thread t = new Thread(new Restore(file));
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    @Override
    public String state() throws RemoteException {

        db.printDatabase();
        disk.printDisk();
        return null;
    }

    @Override
    public void reclaim(int amount) throws RemoteException {

        Thread t = new Thread(new Reclaim(amount));
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



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

    public static Disk getDisk(){ return disk; }


}