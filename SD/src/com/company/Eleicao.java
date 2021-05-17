package com.company;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class Eleicao  extends Thread implements Serializable {
    protected String titulo;
    protected String descricao;
    protected Calendar inicio;
    protected Calendar fim;
    protected String tipo;
    protected Boolean terminada;
    protected Notificacao notificacoes;
    ArrayList<Lista> listas;
    ArrayList<Voto> votos;
    ArrayList<Mesa> mesas;

    public Eleicao(String titulo, String descricao, Calendar inicio, Calendar fim, String tipo, Notificacao notificacoes){
        this.titulo = titulo;
        this.descricao = descricao;
        this.inicio = inicio;
        this.fim = fim;
        this.tipo = tipo;
        this.notificacoes = notificacoes;
        listas = new ArrayList<Lista>();
        listas.add(new Lista("Em branco"));
        listas.add(new Lista("Nulos"));
        votos = new ArrayList<Voto>();
        mesas = new ArrayList<Mesa>();
        terminada = false;
    }

    synchronized public void run(){

        while(Calendar.getInstance().before(fim)){
            try {
                wait(fim.getTimeInMillis()-Calendar.getInstance().getTimeInMillis()); //aguarda até ao fim da eleição
            } catch(InterruptedException e) {
                System.out.println("interruptedException caught");
            }
        }
        if(!terminada) { // para não repetir notificações
            System.out.println("\nEleicao " + titulo + " terminada\n");
            notificacoes.Put("\nEleicao " + titulo + " terminada\n||"+"Eleicao|"+titulo+";resultado|"+GetResultado()); // acrescenta uma nova notificação;
            terminada = true;
        }
    }

    public Lista FindLista(String nome){ // procura uma lista pelo seu nome
        for(int i=0; i<listas.size(); i++){
            if(listas.get(i).GetNome().equals(nome)) return listas.get(i);
        }
        return null;
    }

    public Lista FindMesa(String mesa_id){ // procura uma mesa pelo seu nome
        for(int i=0; i<mesas.size(); i++){
            if(mesas.get(i).GetId().equals(mesa_id)) return listas.get(i);
        }
        return null;
    }

    public int GetVotos(String nome){  // retorna o núero de votos de uma lista
        Lista l = FindLista(nome);
        if(l==null){ // não encontrou a lista
            //erro
        }
        l.GetVotos();
        return 0;
    }

    public boolean FindPessoa(String username){
        for(int i=0; i<votos.size(); i++){
            if(votos.get(i).eleitor.GetUsername().equals(username)){
                return true;
            }
        }
        return false;
    }

    public String toString(){
        String str = "";

        str += "Titulo: " + titulo + "\n";
        str += "Descricao: " + descricao + "\n";
        str += "Tipo: " + tipo + "\n";
        str += "Lista:" + "\n";
        for(int i=0; i<listas.size(); i++){
            str += "\t" + listas.get(i) + "\n";
        }
        str += "Inicio: " + inicio.get(Calendar.DAY_OF_MONTH) + "/" + (inicio.get(Calendar.MONTH)+1) + "/" + inicio.get(Calendar.YEAR) + " " + inicio.get(Calendar.HOUR_OF_DAY) + ":" + inicio.get(Calendar.MINUTE) + "\n";
        str += "Fim: " + fim.get(Calendar.DAY_OF_MONTH) + "/" + (fim.get(Calendar.MONTH)+1) + "/" + fim.get(Calendar.YEAR) + " " + fim.get(Calendar.HOUR_OF_DAY) + ":" + fim.get(Calendar.MINUTE) + "\n";

        str += "Eleitores:" + "\n";

        for(int i=0; i<votos.size(); i++){
            str += "" + votos.get(i) + "\n";
        }

        str += GetResultado();
        return str;
    }

    public String GetResultado(){
        String str = "";
        if(Calendar.getInstance().after(fim)){
            str += "Resultado:" + "\n";
            int total = 0;
            Lista l = listas.get(0);
            for(int i=1; i<listas.size(); i++){
                total += listas.get(i).GetVotos();
                if(l.GetVotos()<listas.get(i).GetVotos()){
                    l = listas.get(i);
                }
            }
            if(total!=0) {
                for (int i = 0; i < listas.size(); i++) {
                    str += "" + listas.get(i) + ": " + listas.get(i) + " / " + (listas.get(i).GetVotos() / total) + "%\n";
                }
            }
            if(total!=0) {
                str += "Vencedor: " + l.GetNome();
            }
        }
        return str;
    }

    public boolean contain(Voto voto){
        return votos.contains(voto);
    }
    public String GetTitulo(){
        return titulo;
    }
    public String GetDescricao(){
        return descricao;
    }
    public Calendar GetInicio(){
        return inicio;
    }
    public Calendar GetFim(){
        return fim;
    }
    public String GetTipo(){
        return tipo;
    }
    public void SetTitulo(String titulo){
        this.titulo = titulo;
    }
    public void SetDescricao(String descricao){
        this.descricao = descricao;
    }
    public void SetInicio(Calendar inicio){
        this.inicio = inicio;
    }
    public void SetFim(Calendar fim){
        this.fim = fim;
    }
    public void SetTipo(String tipo){
        this.tipo = tipo;
    }
}
