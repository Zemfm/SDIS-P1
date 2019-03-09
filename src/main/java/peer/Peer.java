package main.java.peer;

import main.java.listener.Listener;

import java.io.IOException;
import java.net.MulticastSocket;

public class Peer {

    public static String name;
    private static MulticastSocket multicastSocket; //todo: remove?

    private static Listener mcChannel;  //MC CHANNEL
    private static Listener mdbChannel; //BACKUP CHANNEL
    private static Listener mdrChannel; //RESTORE CHANNEL
    private static String rmiRemoteObject;
    public static int protocolVersion;


    //protocol version,the server id, service access point, MC, MDB, MDR
    public static void main(String[] args) throws IOException {


        multicastSocket = new MulticastSocket();


        //this line is used in macOS
        System.setProperty("java.net.preferIPv4Stack", "true");




        //if args > x -> INITIALIZE RMI
        //num of args could be wrong
        if(args.length == 6) {
            System.out.println("\t LAUNCHING RMI");
            if(!parseArgs(args))
                return;

            //launchRMI();

        } else {//else normal peer

            if(!parseArgs(args))
                return;
        }

        System.out.println("Start Listening on MC Channel...");
        //new Thread(mcChannel).start();
        System.out.println("Start Listening on MDB Channel...");
        //new Thread(mdbChannel).start();
        System.out.println("Start Listening on MDR Channel...");
        //new Thread(mdrChannel).start();





    }

    private static boolean parseArgs(String[] args) {
        return false;
    }


}
