package com.company;
import java.io.Serializable;
import java.util.Calendar;

public class Voto implements Serializable{
    Calendar instante;
    Pessoa eleitor;
    Mesa mesa;
    public Voto(Pessoa eleitor, Mesa mesa, Calendar instante){
        this.eleitor = eleitor;
        this.mesa = mesa;
        this.instante = instante;
    }
    public Boolean equals(Voto voto){
        if(voto.eleitor.equals(this.eleitor)){
            return true;
        }
        else{
            return false;
        }
    }
    public String toString(){
        String str = "Eleitor: " + eleitor.GetUsername()  + "(" + eleitor.GetTipo() + ")" + "\n";
        if(mesa!=null) {
            str += "Local: Mesa " + mesa.GetId() + "\n";
        }
        else{
            str += "Local: Online\n";
        }
        str += "Instande: " + instante.get(Calendar.DAY_OF_MONTH) + "/" + (instante.get(Calendar.MONTH)+1) + "/" + instante.get(Calendar.YEAR) + "\n";
        str +="------------------------------\n";
        return str;
    }
}
