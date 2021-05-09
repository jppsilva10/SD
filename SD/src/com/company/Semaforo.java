package com.company;

import java.io.Serializable;

public class Semaforo implements Serializable {
    int keys;
    public Semaforo(int keys){
        this.keys = keys;
    }
    synchronized public void doWait() throws InterruptedException {
        while(keys<=0){
            try {
                wait();
            } catch(InterruptedException e) {
                System.out.println("interruptedException caught");
            }
        }
        keys--;
    }
    synchronized public void DoSignal(){
        keys++;
        notifyAll();
    }
}
