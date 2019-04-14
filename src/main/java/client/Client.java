package main.java.client;

import main.java.service.RMI;


import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.regex.Pattern;

import static main.java.utils.Constants.*;
import static main.java.utils.Utilities.*;
import static main.java.utils.Utilities.validateAccessPoint;

//TODO: RENAME TO TestApp
public class Client {


    public static String AP;
    public static String subProtocol;
    public static String filePath;
    public static int replicationDegree;
    private static String rmiObjectAddress;
    private static String rmiObjectName;
    private static int amount;

    private static RMI peer;

    /*
    java DBS <peer_ap> <sub_protocol> <opnd_1> <opnd_2>
    java DBS 1923 BACKUP test1.pdf 3
     */

    /*
    If you choose to use RMI in the communication between the test application and the peer,
    you should use as access point the name of the remote object providing the "testing" service.
     */

    public static void main(String[] args) {



        parseAccessPoint(args);
        parseSubProtocol(args);

        try {
            Registry registry = LocateRegistry.getRegistry(rmiObjectAddress);

            peer = (RMI)registry.lookup(rmiObjectName);


        } catch (RemoteException | NotBoundException e) {
            System.out.println("Invalid RMI object name");
            return;
        }



        if(subProtocol.equals("BACKUP")) {
            System.out.println("Initializing Backup...");
            parseBackupArguments(args);

            try {
                File file = new File(filePath);
                peer.backup(file, replicationDegree);
                System.out.println("\t File Path: " + filePath + "\n");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if(subProtocol.equals("RESTORE")) {
            System.out.println("Initializing Restore...");
            parseRestoreArguments(args);

            try {
                File file = new File(filePath);
                peer.restore(file);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if(subProtocol.equals("DELETE")) {
            System.out.println("Initializing DELETE");
            parseDeleteArguments(args);
            try {
                peer.delete(filePath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
        else if(subProtocol.equals("RECLAIM")) {
            System.out.println("Initializing Reclaim...");
            parseReclaimArguments(args);

            try {
                peer.reclaim(amount);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        else if(subProtocol.equals("STATE")){
            System.out.println("Retrieving client state...");

            try {
                peer.state();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }







    }

    private static void parseDeleteArguments(String[] args) {

        filePath = args[2];
        System.out.println("DELETE FILE PATH: " + filePath);

    }

    private static void parseSubProtocol(String[] args) {
        subProtocol = args[1];
    }

    private static boolean parseBackupArguments(String[] args) {

        if(args.length != 4) {
            System.out.println("\t Usage: peer <accessPoint> BACKUP <filePath> <replicationDegree> \n");
            return false;
        } else {
            filePath = args[2];
            replicationDegree = Integer.parseInt(args[3]);
        }

        System.out.println("\t \t \t Client Arguments\n\n" +
                "\t Access Point: " + AP + "\n" +
                "\t Sub Protocol : " + subProtocol + "\n" +
                "\t File Path: " + filePath + "\n" + "\n" +
                "\t Replication Degree: " + replicationDegree + "\n");

        return true;

    }

    private static boolean parseRestoreArguments(String[] args) {
        if(args.length != 3) {
            System.out.println("\t Usage: peer <accessPoint> RESTORE <filePath> <replicationDegree> \n");
            return false;
        } else {
            filePath = args[2];

        }

        System.out.println("\t \t \t \t Client Arguments\n\n" +
                "\t Access Point: " + AP + "\n" +
                "\t Sub Protocol : " + subProtocol + "\n" +
                "\t File Path: " + filePath + "\n" + "\n");

        return true;
    }

    private static boolean parseReclaimArguments(String[] args){
        if(args.length != 3) {
            System.out.println("\t Usage: \n" + "\t On src/main/java run: \n" + "\t \t java Peer.peer <access_point> " +
                    "RECLAIM <amount>  \n");
            return false;
        } else {
            amount = Integer.parseInt(args[2]);

        }

        System.out.println("\t \t \t \t Client Arguments\n\n" +
                "\t Access Point: " + AP + "\n" +
                "\t Sub Protocol : " + subProtocol + "\n" +
                "\t Amount: " + amount + "\n" + "\n" );

        return true;
    }

    public static void parseAccessPoint(String[] args) {

        AP = args[0];


        if(validateAccessPoint(AP)) {
            String[] splittedAP = AP.split("/");
            rmiObjectName = splittedAP[1];
            rmiObjectAddress = splittedAP[0];
        } else {
            System.out.println("\t Wrong Access Point format! \n" + "\t Usage: IP/remoteObjectName");
        }


    }


}
