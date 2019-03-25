package main.java.service;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI extends Remote {

    void backup(File file, int replicationDegree) throws RemoteException;

    void delete(String filePath) throws RemoteException;

    void restore(File file) throws RemoteException;

    String state() throws RemoteException;

    void reclaim(int amount) throws RemoteException;

}