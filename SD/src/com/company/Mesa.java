package com.company;
import java.io.Serializable;
import java.util.ArrayList;

public class Mesa implements Serializable{
    String id;
    String departamento;
    RmiMesa rm;
    ArrayList<Eleicao> eleicoes;
    public Mesa(String id, String departamento){
        this.id = id;
        this.departamento = departamento;
        eleicoes = new ArrayList<Eleicao>();
        rm = null;
    }
    public String toString(){
        String str = "Id: " + id + "\n";
        str += "Departamento: " + departamento + "\n";
        try {
            rm.test();
        }catch (Exception e){
            str += "Estado: desligada\n";
            return str;
        }
        str += "Estado: ligada\n";
        return str;
    }
    public String GetId(){
        return id;
    }
    public String GetDepartamento(){
        return departamento;
    }
}
