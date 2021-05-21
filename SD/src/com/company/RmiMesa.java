package com.company;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiMesa  extends Remote {
    void test() throws RemoteException;

    void setRs(RmiServer rs)  throws RemoteException;
}
