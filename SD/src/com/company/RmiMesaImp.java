package com.company;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RmiMesaImp extends UnicastRemoteObject implements RmiMesa, Serializable {
    RmiServer rs;
    ArrayList<String> users;
    ArrayList<String> terminals;
    Map<String, String> lista;
    public RmiMesaImp(RmiServer rs) throws RemoteException{
            this.rs = rs;
            users = new ArrayList<String>();
            terminals = new ArrayList<String>();
            lista = new HashMap<String, String>();
    }

    public void setLists(ArrayList<String> users, ArrayList<String> terminals, Map<String, String> lista){
        this.users = users;
        this.terminals = terminals;
        this.lista = lista;
    }

    public void setRs(RmiServer rs){
        synchronized (this.rs) {
            this.rs = rs;
        }
    }
    public void test() throws RemoteException {
    }

    @Override
    public String listarUsers() throws RemoteException {
        synchronized (users) {
            String str = "";
            for (int i = 0; i < users.size(); i++) {
                str += ";" + users.get(i);
            }
            return str;
        }
    }
    public String ListarTerminals()throws RemoteException{
        synchronized (terminals) {
            String str = "";
            for (int i = 0; i < terminals.size(); i++) {
                str += ",terminal " + terminals.get(i);
                if(lista.get(terminals.get(i)).equals("ready")){
                    str += " (livre)";
                }
                else{
                    str += " (ocupado)";
                }
            }
            return str;
        }
    }
}
