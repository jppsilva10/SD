package com.company;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiMesaImp extends UnicastRemoteObject implements RmiMesa, Serializable {
    RmiServer rs;
    public RmiMesaImp(RmiServer rs) throws RemoteException{
            this.rs = rs;
    }
    public void setRs(RmiServer rs){
        synchronized (this.rs) {
            this.rs = rs;
        }
    }
    public void test() throws RemoteException {
    }
}
