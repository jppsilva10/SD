package com.company;
import java.io.Serializable;
import java.util.*;

public class Lista implements Serializable, Comparator<Lista>{
    protected String nome;
    protected int votos;
    ArrayList<Pessoa> pessoas;

    public Lista(String nome){
        this.nome = nome;
        votos = 0;
        pessoas = new ArrayList<Pessoa>();
    }

    public Pessoa FindPessoa(String username){
        for(int i=0; i<pessoas.size(); i++){
            if(pessoas.get(i).GetUsername().equals(username)){
                return pessoas.get(i);
            }
        }
        return null;
    }

    public String toString(){
        return nome + " - " + votos;
    }
    public String GetNome(){
        return nome;
    }
    public void AddVoto(){
        votos++;
    }
    public int GetVotos(){
        return votos;
    }

    public void SetNome(String nome){
        this.nome = nome;
    }

    public int compare(Lista a, Lista b)
    {
        return a.votos - b.votos;
    }
}
