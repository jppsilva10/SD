package com.company;

import java.io.Serializable;
import java.util.ArrayList;

public class Dados implements Serializable {
    ArrayList<Pessoa> pessoas;
    ArrayList<Eleicao> eleicoes;
    ArrayList<Mesa> mesas;
    ArrayList<RmiClient> clientes;
    Notificacao notificacoes;
    public Dados(){
        pessoas = new ArrayList<Pessoa>();
        eleicoes = new ArrayList<Eleicao>();
        mesas = new ArrayList<Mesa>();
        clientes = new ArrayList<RmiClient>();
        notificacoes = new Notificacao();
    }
}
