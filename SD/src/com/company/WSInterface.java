package com.company;

import java.rmi.RemoteException;

public interface WSInterface {
    void update(String str) throws RemoteException;
}
