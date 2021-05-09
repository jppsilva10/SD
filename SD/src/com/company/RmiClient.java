package com.company;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiClient extends Remote {
    void print(String str) throws RemoteException;
    void test() throws RemoteException;
    void setRs(RmiServer rmiServer) throws RemoteException;
}
