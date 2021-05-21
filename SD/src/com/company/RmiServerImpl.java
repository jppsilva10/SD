package com.company;

import com.github.scribejava.core.model.OAuth2AccessToken;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.net.*;
import java.util.ArrayList;
import java.io.*;
import java.util.Calendar;
import java.util.Properties;

public class RmiServerImpl extends UnicastRemoteObject implements RmiServer, Serializable{

    protected ArrayList<Pessoa> pessoas;
    protected ArrayList<Eleicao> eleicoes;
    protected ArrayList<Mesa> mesas;
    protected Dados dados;
    protected Notificacao notificacoes;
    protected ArrayList<RmiClient> clientes;

    static int RmiPort;
    //static int UdpPort;
    //static String UdpAddress;
    static int RmiSleepTime;

    public RmiServerImpl() throws RemoteException
    {
        super();

        LoadDados();

        //--- iniciar as threads das eleições ---
        for(int i=0; i<eleicoes.size(); i++){
            eleicoes.get(i).start();
        }
        //---------------------------------------

    }

    public void reconect() // reconectar com os clientes
    {
        boolean erro = false;
        synchronized (clientes) {
            for (int i = 0; i < clientes.size(); i++) {
                try {
                    clientes.get(i).setRs((RmiServer)this);
                } catch (Exception e) {
                    clientes.remove(clientes.get(i));
                    i--;
                    System.out.println("cliente removido");
                    erro = true;
                }
            }
        }
        try {
            if(erro) updateAll(ListarUsers());
        }catch (RemoteException e){
        }

        synchronized (mesas) {
            for (int i = 0; i < mesas.size(); i++) {
                try {
                    mesas.get(i).rm.setRs((RmiServer)this);
                } catch (Exception e) {
                    mesas.get(i).rm = null;
                }
            }
        }

        synchronized (pessoas) {
            synchronized (eleicoes){
                synchronized (mesas){
                    synchronized (notificacoes){
                        SaveDados();
                    }
                }
            }
        }
    }

    public void subscribe(RmiClient cliente) throws RemoteException
    {
        synchronized (clientes) {
            clientes.add(cliente);
        }

        synchronized (pessoas) {
            synchronized (eleicoes){
                synchronized (mesas){
                    synchronized (notificacoes){
                        SaveDados();
                    }
                }
            }
        }
    }

    public void subscribe(String mesa_id, RmiMesa cliente) throws RemoteException, NotFoundException
    {
        Mesa m = findMesa(mesa_id);
        if(m==null){ // não encontrou a mesa
            throw new NotFoundException();
        }

        synchronized (mesas){
            m.rm = cliente;
        }

        System.out.println("\nMesa "+ mesa_id + " ligada\n");
        notificacoes.Put("\nMesa "+ mesa_id + " ligada\n");
        synchronized (pessoas) {
            synchronized (eleicoes){
                synchronized (mesas){
                    synchronized (notificacoes){
                        SaveDados();
                    }
                }
            }
        }
    }

    public void print()
    {
        while(true) {
            String str;
            str = notificacoes.Get(); // espera a até haver uma notificação nova
            synchronized (clientes) {
                for (int i = 0; i < clientes.size(); i++) { // notifica todos as mesas
                    try {
                        if(str.contains("terminada")){
                            clientes.get(i).print(str.split("\\|\\|")[0]);
                            updateAll(str.split("\\|\\|")[1]);
                        }
                        else{
                            clientes.get(i).print(str);
                        }
                    } catch (RemoteException e) { // Se a ligação com a mesa deixar de funcionar remove-se o ojeto remoto da lista
                        clientes.remove(clientes.get(i));
                        System.out.println("client removido");
                        i--;
                    }
                }
            }
            synchronized (pessoas) {
                synchronized (eleicoes){
                    synchronized (mesas){
                        synchronized (notificacoes){
                            SaveDados();
                        }
                    }
                }
            }
        }
    }

    public Pessoa findPessoa(String numero_CC) // procura uma pessoa pelo seu número de CC
    {
        synchronized (pessoas) {
            for (int i = 0; i < pessoas.size(); i++) {
                if (pessoas.get(i).GetNumero_CC().equals(numero_CC)) return pessoas.get(i);
            }
            return null;
        }
    }

    public Pessoa findPessoaByUsername(String username) // procura uma pessoa pelo seu username
    {
        synchronized (pessoas) {
            for (int i = 0; i < pessoas.size(); i++) {
                if (pessoas.get(i).GetUsername().equals(username)) return pessoas.get(i);
            }
            return null;
        }
    }

    public Pessoa findPessoaByName(String nome) // procura pessoa pelo seu nome
    {
        synchronized (pessoas) {
            for (int i = 0; i < pessoas.size(); i++) {
                if (pessoas.get(i).GetNome().equals(nome)) return pessoas.get(i);
            }
            return null;
        }
    }

    public void createPessoa(String tipo, String nome, String username, String password, String departamento, String contacto, String morada, String numero_CC, Calendar validade_CC) throws DataConflictException.DuplicatedNumero_CC, DataConflictException.DuplicatedUsername
    {
        synchronized (pessoas) {
            Pessoa p = findPessoa(numero_CC);
            if (p != null) {// já existe uma pessoa com esse númedro de CC
                throw new DataConflictException.DuplicatedNumero_CC();
            }
            p = findPessoaByUsername(username);
            if (p != null) {// já existe uma pessoa com esse username
                throw new DataConflictException.DuplicatedUsername();
            }

            p = new Pessoa(tipo, nome, username, password, departamento, contacto, morada, numero_CC, validade_CC);
            pessoas.add(p);
        }
        synchronized (pessoas) {
            synchronized (eleicoes){
                synchronized (mesas){
                    synchronized (notificacoes){
                        SaveDados();
                    }
                }
            }
        }
        System.out.println("Pessoa registada");
    }

    public void editPessoa(String nome, String username,String password, String departamento, String contacto, String morada, String numero_CC, Calendar validade_CC, String numero_CC_anterior) throws NotFoundException.PessoaNF, DataConflictException.DuplicatedNumero_CC, DataConflictException.DuplicatedUsername
    {
        synchronized (pessoas) {
            Pessoa p = findPessoa(numero_CC_anterior);
            if (p == null) { // não encontrou a pessoa
                throw new NotFoundException.PessoaNF();
            }
            if (!numero_CC.equals(numero_CC_anterior)) { // caso seja para mudar o numero de CC
                Pessoa p2 = findPessoa(numero_CC_anterior);
                if(p2!=null){ // já existe uma pessoa com esse numero de cc
                    throw new DataConflictException.DuplicatedNumero_CC();
                }

            }
            synchronized (p){
                if (!username.equals(p.GetUsername())) { // caso seja para mudar o username
                    Pessoa p2 = findPessoaByUsername(username);
                    if(p2!=null){ // já existe uma pessoa com esse username
                        throw new DataConflictException.DuplicatedUsername();
                    }

                }
                p.SetNome(nome);
                p.SetUsername(username);
                p.SetPassword(password);
                p.SetDepartamento(departamento);
                p.SetContacto(contacto);
                p.SetMorada(morada);
                p.SetNumero_CC(numero_CC);
                p.SetValidade_CC(validade_CC);
            }
        }
        synchronized (pessoas) {
            synchronized (eleicoes){
                synchronized (mesas){
                    synchronized (notificacoes){
                        SaveDados();
                    }
                }
            }
        }
        System.out.println("Pessoa editada");
    }

    public Eleicao findEleicao(String titulo) // procura uma eleição pelo título
    {
        synchronized (eleicoes) {
            for (int i = 0; i < eleicoes.size(); i++) {
                if (eleicoes.get(i).GetTitulo().equals(titulo)) return eleicoes.get(i);
            }
            return null;
        }
    }

    public void createEleicao(String titulo, String descricao, Calendar inicio, Calendar fim, String tipo) throws DataConflictException
    {
        Eleicao e;
        synchronized (eleicoes) {
            e = findEleicao(titulo);
            if (e != null) {// já existe uma eleição com esse título
                throw new DataConflictException();
            }
            e = new Eleicao(titulo, descricao, inicio, fim, tipo, notificacoes);
            eleicoes.add(e);
        }
        e.start();
        synchronized (pessoas) {
            synchronized (eleicoes){
                synchronized (mesas){
                    synchronized (notificacoes){
                        SaveDados();
                    }
                }
            }
        }
        System.out.println("Eleicao criada");
    }

    public void editEleicao(String titulo, String descricao, Calendar inicio, Calendar fim, String tipo, String titulo_anterior) throws NotFoundException.EleicaoNF, TimeBoundsException.EleicaoAlreadyStarted, DataConflictException
    {
        synchronized (eleicoes) {
            Eleicao e = findEleicao(titulo_anterior);
            if (e == null) { // não encontrou a eleição
                throw new NotFoundException.EleicaoNF();
            }
            if(!titulo.equals(titulo_anterior)){ // caso seja para editar o título
                if(findEleicao(titulo)!=null){ //já existe uma eleição com esse título
                    throw new DataConflictException();
                }
            }
            synchronized (e) {
                if (Calendar.getInstance().after(e.GetInicio())) { // a eleição já começou
                    throw new TimeBoundsException.EleicaoAlreadyStarted();
                }
                e.SetTitulo(titulo);
                if(!titulo_anterior.equals(titulo)) updateAll("Eleicao|"+titulo_anterior+";titulo|"+titulo);
                if(!e.GetDescricao().equals(descricao)) updateAll("Eleicao|"+titulo+";descricao|"+descricao);
                e.SetDescricao(descricao);
                updateAll("Eleicao|"+titulo+";inicio|" + inicio.get(Calendar.DAY_OF_MONTH) + "/" + (inicio.get(Calendar.MONTH)+1) + "/" + inicio.get(Calendar.YEAR) + " " + inicio.get(Calendar.HOUR_OF_DAY) + ":" + inicio.get(Calendar.MINUTE));
                e.SetInicio(inicio);
                updateAll("Eleicao|"+titulo+";fim|" + fim.get(Calendar.DAY_OF_MONTH) + "/" + (fim.get(Calendar.MONTH)+1) + "/" + fim.get(Calendar.YEAR) + " " + fim.get(Calendar.HOUR_OF_DAY) + ":" + fim.get(Calendar.MINUTE));
                e.SetFim(fim);
                if(!e.GetDescricao().equals(tipo)) updateAll("Eleicao|"+titulo+";tipo|"+tipo);
                e.SetTipo(tipo);
                e.notify();
            }
        }
        synchronized (pessoas) {
            synchronized (eleicoes){
                synchronized (mesas){
                    synchronized (notificacoes){
                        SaveDados();
                    }
                }
            }
        }
        System.out.println("eleicao editada");
    }

    public String consultarEleicao(String titulo) throws NotFoundException.EleicaoNF
    {

        Eleicao e = findEleicao(titulo);
        if (e == null) { // não encontrou a eleição
            throw new NotFoundException.EleicaoNF();
        }
        synchronized (e) {
            return "" + e;
        }
    }

    public String consultarPessoa(String username) throws NotFoundException.PessoaNF
    {

        Pessoa p = findPessoaByUsername(username);
        if (p == null) { // não encontrou a eleição
            throw new NotFoundException.PessoaNF();
        }
        synchronized (p) {
            return "" + p;
        }
    }

    public String consultarMesa(String id) throws NotFoundException.MesaNF
    {
        Mesa m = findMesa(id);
        if(m==null){// nao encontrou a mesa
            throw new NotFoundException.MesaNF();
        }
        return "" + m;
    }

    public String listarPessoas()
    {
        synchronized (pessoas){
            String str = "";
            int count = 1;
            for (int i = 0; i < pessoas.size(); i++) {
                str += ";" + count + "|" + pessoas.get(i).GetUsername();
                count++;
            }
            str = "size|" + count + str;
            return str;
        }
    }

    public String listarEleicoes()
    {
        synchronized (pessoas){
            String str = "";
            int count = 1;
            for (int i = 0; i < eleicoes.size(); i++) {
                str += ";" + count + "|" + eleicoes.get(i).GetTitulo();
                count++;
            }
            str = "size|" + count + str;
            return str;
        }
    }

    public String listarListas(String titulo) throws NotFoundException.EleicaoNF
    {
        Eleicao e = findEleicao(titulo);
        if(e==null){// não encontrou a eleção
            throw new NotFoundException.EleicaoNF();
        }
        synchronized (e) {
            String str = "";
            int count = 1;
            for (int i = 0; i < e.listas.size(); i++) {
                str += ";" + count + "|" + e.listas.get(i).GetNome();
                count++;
            }
            str = "size|" + count + str;
            return str;
        }
    }

    public String listarPessoaPorLista(String titulo, String lista) throws NotFoundException.EleicaoNF, NotFoundException.ListaNF
    {
        Eleicao e = findEleicao(titulo);
        if(e==null){// não encontrou a eleção
            throw new NotFoundException.EleicaoNF();
        }
        Lista l = e.FindLista(lista);
        if(lista==null){
            throw new NotFoundException.ListaNF();
        }
        synchronized (e){
            String str = "";
            int count = 1;
            for(int i=0; i<l.pessoas.size(); i++){
                str += ";" + count + "|" + l.pessoas.get(i).GetUsername();
                count++;
            }
            str = "size|" + count + str;
            return str;
        }
    }

    public String listarPessoaPorEleicao(String titulo) throws NotFoundException.EleicaoNF
    {
        Eleicao e = findEleicao(titulo);
        if(e==null){// não encontrou a eleção
            throw new NotFoundException.EleicaoNF();
        }
        synchronized (e){
            String str = "";
            int count = 1;
            for(int i=0; i<pessoas.size(); i++){
                if(pessoas.get(i).GetTipo().equals(e.GetTipo())){
                    str += ";" + count + "|" + pessoas.get(i).GetUsername();
                    count++;
                }
            }
            str = "size|" + count + str;
            return str;
        }
    }

    public String listarMesas()
    {
        synchronized (mesas){
            String str = "";
            int count = 1;
            for (int i = 0; i < mesas.size(); i++) {
                str += ";" + count + "|" + mesas.get(i).GetId();
                count++;
            }
            str = "size|" + count + str;
            return str;
        }
    }

    public String listarMesasFuncionais()
    {
        synchronized (mesas){
            String str = "";
            int count = 1;
            for (int i = 0; i < mesas.size(); i++) {
                try{
                    mesas.get(i).rm.test();
                }catch (Exception e){
                    mesas.get(i).rm = null;
                    continue;
                }
                str += ";" + count + "|" + mesas.get(i).GetId();
                count++;
            }
            str = "size|" + count + str;
            return str;
        }
    }

    public String listarMesasPorEleicao(String eleicao) throws NotFoundException.EleicaoNF
    {
        Eleicao e = findEleicao(eleicao);
        if(e==null){// nao encontrou a elicao
            throw new NotFoundException.EleicaoNF();
        }
        synchronized (mesas){
            synchronized (e){
                String str = "";
                int count = 1;
                for (int i = 0; i < e.mesas.size(); i++) {
                    str += ";" + count + "|" + e.mesas.get(i).GetId();
                    count++;
                }
                str = "size|" + count + str;
                return str;
            }
        }
    }

    public String listarEleicoesNaoIniciadas()
    {
        synchronized (eleicoes) {
            String str = "";
            int count = 1;
            Calendar c = Calendar.getInstance();
            for (int i = 0; i < eleicoes.size(); i++) {
                if (c.before(eleicoes.get(i).GetInicio())) {
                    str += ";" + count + "|" + eleicoes.get(i).GetTitulo();
                    count++;
                }
            }
            str = "size|" + count + str;
            return str;
        }
    }

    public String listarEleicoesIniciadas()
    {
        synchronized (eleicoes) {
            String str = "";
            Calendar c = Calendar.getInstance();
            for (int i = 0; i < eleicoes.size(); i++) {
                if (c.before(eleicoes.get(i).GetFim())) {
                    if (c.after(eleicoes.get(i).GetInicio())) str += eleicoes.get(i).GetTitulo() + "\n";
                }
            }
            return str;
        }
    }

    public String ListarEleicoesTerminadas()
    {
        synchronized (eleicoes) {
            String str = "";
            for (int i = 0; i < eleicoes.size(); i++) {
                if (Calendar.getInstance().after(eleicoes.get(i).GetFim())) str += eleicoes.get(i).GetTitulo() + "\n";
            }
            return str;
        }
    }

    public String ListarEleicoesPorMesa(String mesa_id) throws NotFoundException.MesaNF
    {

        Mesa m = findMesa(mesa_id);
        if(m==null){ // nao encontrou a mesa
            throw new NotFoundException.MesaNF();
        }
        synchronized (eleicoes) {
            synchronized (mesas) {
                String str = "";
                int count = 1;
                Calendar c = Calendar.getInstance();
                for (int i = 0; i < m.eleicoes.size(); i++) {
                    if (c.before(m.eleicoes.get(i).GetFim())) {
                        if (c.after(m.eleicoes.get(i).GetInicio())){
                            str += ";" + count + "|" + m.eleicoes.get(i).GetTitulo();
                            count++;
                        }
                    }
                }
                str = "size|" + count + str;
                return str;
            }
        }
    }

    public boolean login(String username, String password) throws NotFoundException.PessoaNF
    {
        Pessoa p = findPessoaByUsername(username);
        if (p == null) { // não encontrou a pessoa
            return false;
        }
        synchronized (p) {

            if (p.GetPassword().equals(password)) { // validar a password
                return true;
            }
            return false;
        }
    }

    public String facebookLogin()
    {
        synchronized (pessoas){
            String str = "";
            int count = 1;
            Calendar c = Calendar.getInstance();
            for (int i = 0; i < pessoas.size(); i++) {
                if(pessoas.get(i).GetAccessToken()!=null){
                    str += ";" + count + "|" + pessoas.get(i).GetUsername() + "," + pessoas.get(i).GetPassword();
                    count++;
                }
            }
            str = "size|" + count + str;
            return str;
        }
    }

    public Boolean getVoto(String numero_CC, String eleicao)
    {

        //throw(ExportException be);
        return false;
    }

    public Mesa findMesa(String mesa_id) // procura uma mesa pelo id
    {
        synchronized (mesas) {
            for (int i = 0; i < mesas.size(); i++) {
                if (mesas.get(i).GetId().equals(mesa_id)) return mesas.get(i);
            }
            return null;
        }
    }

    public void createMesa(String mesa_id, String departamento) throws DataConflictException
    {
        synchronized (mesas) {
            Mesa m = findMesa(mesa_id);
            if (m != null) { // mesa já exite
                try {
                    m.rm.test();
                }catch(Exception e){
                    m.departamento = departamento;
                    return;
                }
                throw new DataConflictException();
            }
            else{
                m = new Mesa(mesa_id, departamento);
                mesas.add(m);
                System.out.println("Mesa criada");
            }
        }
    }

    public void addMesa(String eleicao, String mesa_id) throws NotFoundException.MesaNF, NotFoundException.EleicaoNF, DataConflictException // adicionar uma mesa a uma eleição
    {
        synchronized (eleicoes) {
            synchronized (mesas) {
                Mesa m = findMesa(mesa_id);
                if (m == null) { // não encontrou a mesa;
                    throw new NotFoundException.MesaNF();
                }
                Eleicao e = findEleicao(eleicao);
                synchronized (e) {
                    if (e == null) { // não encontrou a eleicao
                        throw new NotFoundException.EleicaoNF();
                    }
                    if (e.FindMesa(mesa_id) != null) { // a mesa já foi adicionada
                        throw new DataConflictException();
                    }
                    e.mesas.add(m);
                    m.eleicoes.add(e);
                }
            }
        }
        synchronized (pessoas) {
            synchronized (eleicoes){
                synchronized (mesas){
                    synchronized (notificacoes){
                        SaveDados();
                    }
                }
            }
        }
        System.out.println("Mesa adicionada");
    }

    public void removeMesa(String eleicao, String mesa_id) throws NotFoundException.MesaNF, NotFoundException.EleicaoNF, DataConflictException // remover uma mesa da eleição
    {
        synchronized (eleicoes) {
            synchronized (mesas) {
                Mesa m = findMesa(mesa_id);
                if (m == null) { // não encontrou a mesa;
                    throw new NotFoundException.MesaNF();
                }
                Eleicao e = findEleicao(eleicao);
                synchronized (e) {
                    if (e == null) { // não encontrou a eleicao
                        throw new NotFoundException.EleicaoNF();
                    }
                    if (e.FindMesa(mesa_id) == null) { // nao encontrou a mesa
                        throw new DataConflictException();
                    }
                    e.mesas.remove(m);
                    m.eleicoes.remove(e);
                }
            }
        }
        synchronized (pessoas) {
            synchronized (eleicoes){
                synchronized (mesas){
                    synchronized (notificacoes){
                        SaveDados();
                    }
                }
            }
        }
        System.out.println("Mesa removida");
    }

    public void addLista(String eleicao, String nome) throws NotFoundException.EleicaoNF, DataConflictException, TimeBoundsException.EleicaoAlreadyStarted // acrescenta uma lista nova à eleição
    {
        synchronized (eleicoes) {
            Eleicao e = findEleicao(eleicao);
            synchronized (e) {
                if (e == null) { // não encontrou a eleição
                    throw new NotFoundException.EleicaoNF();
                }
                Lista l = e.FindLista(nome);
                if (l != null) { // já existe uma lista com esse nome
                    throw new DataConflictException();
                }
                l = new Lista(nome);
                if(e.GetInicio().before(Calendar.getInstance())){
                    throw new TimeBoundsException.EleicaoAlreadyStarted();
                }
                e.listas.add(l);
                updateAll("Eleicao|" + eleicao + ";listas|"+ l);
            }
        }
        synchronized (pessoas) {
            synchronized (eleicoes){
                synchronized (mesas){
                    synchronized (notificacoes){
                        SaveDados();
                    }
                }
            }
        }
        System.out.println("Lista criada");
    }

    public void editLista(String eleicao, String nome, String nome_anterior) throws NotFoundException.EleicaoNF, DataConflictException, TimeBoundsException.EleicaoAlreadyStarted, NotFoundException.ListaNF
    {
        synchronized (eleicoes) {
            Eleicao e = findEleicao(eleicao);
            synchronized (e) {
                if (e == null) { // não encontrou a eleição
                    throw new NotFoundException.EleicaoNF();
                }
                Lista l = e.FindLista(nome);
                if(!nome.equals(nome_anterior)) { // caso sseja para mudar o nome
                    if (l != null) { // já existe uma lista com esse nome
                        throw new DataConflictException();
                    }
                }
                l = e.FindLista(nome_anterior);
                if (l == null) { // nao encontrou a lista
                    throw new NotFoundException.ListaNF();
                }
                if(e.GetInicio().before(Calendar.getInstance())){
                    throw new TimeBoundsException.EleicaoAlreadyStarted();
                }
                l.SetNome(nome);
                updateAll("Eleicao|" + eleicao + ";listas|"+ l + "|" + nome_anterior);
            }
        }
        synchronized (pessoas) {
            synchronized (eleicoes){
                synchronized (mesas){
                    synchronized (notificacoes){
                        SaveDados();
                    }
                }
            }
        }
        System.out.println("Lista editada");
    }

    public void addPessoa(String eleicao, String lista, String pessoa) throws NotFoundException.EleicaoNF, NotFoundException.ListaNF, DataConflictException, NotFoundException.PessoaNF, TimeBoundsException.EleicaoAlreadyStarted
    {

        Eleicao e = findEleicao(eleicao);
        if(e==null){// nao encontrou a eleicao
            throw new NotFoundException.EleicaoNF();
        }
        Pessoa p = findPessoaByUsername(pessoa);
        if(p==null){// nao encontrou a pessoa
            throw new NotFoundException.PessoaNF();
        }
        synchronized (e) {
            if(e.GetInicio().before(Calendar.getInstance())){
                throw new TimeBoundsException.EleicaoAlreadyStarted();
            }

            Lista l = e.FindLista(lista);
            if (l == null) { // nao encontrou a lista
                throw new NotFoundException.ListaNF();
            }
            if(l.FindPessoa(pessoa)!=null){ // a pessoa ja foi adicionada
                throw new DataConflictException();
            }
            l.pessoas.add(p);
        }
        System.out.println("Pessoa adicionada");
    }

    public void removePessoa(String eleicao, String lista, String pessoa) throws NotFoundException.EleicaoNF, NotFoundException.ListaNF, TimeBoundsException.EleicaoAlreadyStarted, NotFoundException.PessoaNF
    {
        Eleicao e = findEleicao(eleicao);
        if(e==null){// nao encontrou a eleicao
            throw new NotFoundException.EleicaoNF();
        }
        synchronized (e) {
            if(e.GetInicio().before(Calendar.getInstance())){
                throw new TimeBoundsException.EleicaoAlreadyStarted();
            }
            Lista l = e.FindLista(lista);
            if (l == null) { // nao encontrou a lista
                throw new NotFoundException.ListaNF();
            }
            Pessoa p =  l.FindPessoa(pessoa);
            if(p==null){ // não encontrou a pessoa
                throw new NotFoundException.PessoaNF();
            }
            l.pessoas.remove(p);
        }
        System.out.println("Pessoa removida");
    }

    public void addVoto(String numero_CC, String eleicao, String lista, String mesa_id) throws NotFoundException.PessoaNF, NotFoundException.EleicaoNF, NotFoundException.MesaNF, NotFoundException.ListaNF, DataConflictException, TimeBoundsException.EleicaoAlreadyTerminated, DataConflictException.InvalidType {
        Pessoa p = findPessoaByUsername(numero_CC);
        if(p==null){ // não encountrou a pessoa
            throw new NotFoundException.PessoaNF();
        }
        Eleicao e = findEleicao(eleicao);
        if(e==null){ // não encontrou a eleicao
            throw new NotFoundException.EleicaoNF();
        }
        Mesa m = findMesa(mesa_id);
        if(m==null){ // não encontrou a mesa
            throw new NotFoundException.MesaNF();
        }
        Calendar c = Calendar.getInstance();
        Voto voto = new Voto(p, m, c); // informação do voto
        synchronized (eleicoes) {
            synchronized (e) {
                if(e.FindPessoa(numero_CC)){
                    throw new DataConflictException();
                }
                if(c.after(e.GetFim())){ // a eleção já terminou
                    throw new TimeBoundsException.EleicaoAlreadyTerminated();
                }
                if(!e.GetTipo().equals(p.GetTipo())){ // nao pode votar neste tipo de eleicoes
                    throw new DataConflictException.InvalidType();
                }

                Lista l = e.FindLista(lista);
                if (l == null) { // não encontrou a lista
                    throw new NotFoundException.ListaNF();
                }
                e.votos.add(voto);
                l.AddVoto();
                updateAll("Eleicao|"+eleicao+";eleitores|"+voto);
                updateAll("Eleicao|"+eleicao+";listas|"+l+"|"+lista);
            }
        }

        synchronized (pessoas) {
            synchronized (eleicoes){
                synchronized (mesas){
                    synchronized (notificacoes){
                        SaveDados();
                    }
                }
            }
        }
        System.out.println("voto realizado");
    }

    public void setAccessToken(String username, OAuth2AccessToken accessToken) throws RemoteException, NotFoundException.PessoaNF {
        Pessoa p = findPessoaByUsername(username);
        if(p==null){
            throw new NotFoundException.PessoaNF();
        }

        synchronized (pessoas){
            synchronized (p){
                p.SetAccessToken(accessToken);
            }
        }
        synchronized (pessoas) {
            synchronized (eleicoes){
                synchronized (mesas){
                    synchronized (notificacoes){
                        SaveDados();
                    }
                }
            }
        }
    }

    public OAuth2AccessToken getAccessToken(String username) throws RemoteException, NotFoundException.PessoaNF {
        Pessoa p = findPessoaByUsername(username);
        if(p==null){
            throw new NotFoundException.PessoaNF();
        }

        synchronized (p){
            return p.GetAccessToken();
        }
    }

    public void updateAll(String str){
        boolean erro = false;
        synchronized (clientes){
            for (int i = 0; i<clientes.size(); i++){
                try {
                    clientes.get(i).update(str);
                }catch (RemoteException e){
                    clientes.remove(clientes.get(i));
                    i--;
                    System.out.println("cliente removido");
                    erro = true;
                }
            }
        }
        try {
            if (erro) updateAll(ListarUsers());
        }catch (RemoteException e){
        }
    }

    public String ListarUsers() throws RemoteException{
        synchronized (clientes){
        String str = "";
        int count = 1;
            for(int i=0; i<clientes.size(); i++){
                try {
                    String s = clientes.get(i).getUsername();
                    if(s!=null) {
                        str += ";" + count + "|" + s;
                        count++;
                    }
                }catch (RemoteException e){
                    clientes.remove(clientes.get(i));
                    i--;
                    System.out.println("cliente removido");
                }
            }
        str = "size|" + count + str;
        return str;
        }
    }

    public void SaveDados() // salvar os dados em ficheiro
    {

        //------ descartar conexoes perdidas ------
        boolean erro = false;
        for(int i = 0; i<clientes.size(); i++){
            try{
                clientes.get(i).test();
            }catch (Exception e){
                clientes.remove(clientes.get(i));
                i--;
                System.out.println("cliente removido");
                erro = true;
            }
        }
        try {
            if(erro) updateAll(ListarUsers());
        }catch (RemoteException e){
        }

        //-----------------------------------------

        //------ descartar mesas perdidas ------
        for(int i = 0; i<mesas.size(); i++){
            try{
                mesas.get(i).rm.test();
            }catch (Exception e){
                mesas.get(i).rm = null;
            }
        }
        //-----------------------------------------

        File f = new File("Dados.obj");
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(dados);
            oos.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Erro a criar ficheiro.");
        } catch (IOException ex) {
            System.out.println("Erro a escrever para o ficheiro.");
        }
    }

    public void LoadDados() // ler os dados salvos no ficheiro
    {
        File f = new File("Dados.obj");
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            dados = (Dados)ois.readObject();
            eleicoes = dados.eleicoes;
            pessoas = dados.pessoas;
            mesas = dados.mesas;
            notificacoes = dados.notificacoes;
            clientes = dados.clientes;
            ois.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Erro a abrir ficheiro.");
            dados = new Dados();
            eleicoes = dados.eleicoes;
            pessoas = dados.pessoas;
            mesas = dados.mesas;
            notificacoes = dados.notificacoes;
            clientes = dados.clientes;
        } catch (IOException ex) {
            System.out.println("Erro a ler ficheiro.");
        } catch (ClassNotFoundException ex) {
            System.out.println("Erro a converter objeto.");
        }
    }

    // =======================================================

    static void config() // ler o ficheiro de configuração
    {
        try {
            InputStream is = new FileInputStream("config.properties");
            Properties p = new Properties();
            p.load(is);
            RmiPort = Integer.parseInt(p.getProperty("RmiPort"));
            RmiSleepTime = Integer.parseInt(p.getProperty("RmiSleepTime"));
            //UdpPort = Integer.parseInt(p.getProperty("UdpPort"));
            //UdpAddress = p.getProperty("UdpAddress");
        } catch (FileNotFoundException ex) {
            System.out.println("Erro a abrir ficheiro o de configuracao.");
        } catch (IOException ex) {
            System.out.println("Erro a ler ficheiro de configuracao.");
        }
    }
    /*
    void ping(){
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            String texto = "";
            InputStreamReader input = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(input);

            while(true){
                byte [] m = texto.getBytes();
                InetAddress aHost = InetAddress.getByName(UdpAddress);
                DatagramPacket request = new DatagramPacket(m,m.length,aHost,UdpPort);
                aSocket.send(request);
                byte[] buffer = new byte[1000];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply);
            }
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}
    }

     */

    public static void main(String args[]) throws InterruptedException
    {
        RmiServerImpl s;
        config();
        while(true) {
            try {
                s = new RmiServerImpl();
                Registry r = LocateRegistry.createRegistry(RmiPort);
                r.rebind("server", s);

                s.reconect();

                //Naming.rebind("hello", h);
                System.out.println("RMI Server ready.");
                break;
            } catch (ExportException be) {
                System.out.println(be);
                System.out.println("Servidor secundario");
                Thread.sleep(RmiSleepTime);
                continue;
            } catch (RemoteException re) {
                System.out.println("Exception in HelloImpl.main: " + re);
            }
        }
        s.print();
    }
}

/*
class Ping extends Thread{
    //static String UdpAddress;
    static int UdpPort;
    static int RmiPort;
    public static void run(String args[]){
        DatagramSocket aSocket = null;
        String s;
        try{
            aSocket = new DatagramSocket(UdpPort);
            while(true){
                byte[] buffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);

                //testar
                RmiServerImpl S;
                try {
                    S = new RmiServerImpl();
                    Registry r = LocateRegistry.createRegistry(RmiPort);
                    r.rebind("server", S);

                    //Naming.rebind("hello", h);
                    System.out.println("RMI Server ready.");
                    break;
                } catch (ExportException be) {
                    s=new String(request.getData(), 0, request.getLength());
                    DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort());
                    aSocket.send(reply);
                    continue;
                }
            }
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}
    }
}
*/