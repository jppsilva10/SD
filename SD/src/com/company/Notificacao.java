package com.company;

import java.io.Serializable;
import java.util.ArrayList;

public class Notificacao implements Serializable {
    ArrayList<String> notificacoes;
    protected Semaforo s;

    public Notificacao(){
        notificacoes = new ArrayList<String>();
        this.s = new Semaforo(0);
    }
    public void Put(String str){
        synchronized (this) {
            notificacoes.add(str);
        }
        s.DoSignal();
    }
    public String Get(){
        try {
            s.doWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String str;
        synchronized (this) {
            str = notificacoes.get(0);
            notificacoes.remove(str);
        }
        return str;
    }
}
