package com.company;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AdminConsole extends UnicastRemoteObject implements RmiClient, Serializable {
    String RmiAddress;
    int RmiPort;
    public RmiServer rs;
    Calendar Timer;
    WSInterface ws;
    UserInterface user;

    private static final String NETWORK_NAME = "Facebook";
    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/v3.2/me";

    public AdminConsole() throws RemoteException{
        super();
    }

    public void test() throws RemoteException // vere se a consola esta conectada
    {
    }

    public void print(String str) throws RemoteException // imprimir notificacoes
    {
        System.out.println(str);
    }

    public void setRs(RmiServer rs)
    {
        synchronized (this.rs) {
            this.rs = rs;
        }
    }

    @Override
    public void update(String str) throws RemoteException {
        if(this.ws!=null) this.ws.update(str);
    }

    public void setWs(WSInterface ws) throws RemoteException {
        this.ws = ws;
    }

    public String getUsername() throws RemoteException {
        if(user!=null) return user.getUsername();
        return null;
    }

    public String getPassword() throws RemoteException {
        if(user!=null) return user.getPassword();
        return null;
    }

    public void setUser(UserInterface user) throws RemoteException {
        this.user = user;
    }

    public void config() // ler o ficheiro de configuração
    {
        try {
            InputStream is = new FileInputStream("config.properties");
            Properties p = new Properties();
            p.load(is);
            RmiAddress = p.getProperty("RmiAddress");
            RmiPort = Integer.parseInt(p.getProperty("RmiPort"));
        } catch (FileNotFoundException ex) {
            System.out.println("Erro a abrir ficheiro de configuracao.");
        } catch (IOException ex) {
            System.out.println("Erro a ler ficheiro de configuracao.");
        }
    }

    Properties GetHashMap(String str)
    {
        //System.out.println(str);
        String[] parameters = str.split(";");
        Properties m = new Properties();
        for(int i=0; i<parameters.length; i++){
            String[] item = parameters[i].split("\\|");
            m.put(item[0], item[1]);
        }
        return m;
    }

    public void rebind() // reconectar com o servidor
    {
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        synchronized (rs) {
            try {
                rs = (RmiServer) LocateRegistry.getRegistry(RmiAddress, RmiPort).lookup("server");
            } catch (Exception e) {
            }
        }
    }

    public void AccessToken() throws IOException, ExecutionException, InterruptedException {
        // Step 1: Create Facebook Account
        // Step 2: Create application (https://developers.facebook.com/ Log In -> Get Started)
        // Step 3: Replace below with your app key and secret
        final String appKey = "463509174943899";
        final String appSecret = "22b58424ddda756bdd4faa0baed6dee3";
        final String secretState = "secret" + new Random().nextInt(999_999);
        final OAuth20Service service = new ServiceBuilder(appKey)
                .apiSecret(appSecret)
                .callback("https://eden.dei.uc.pt/~fmduarte/echo.php")
                .build(FacebookApi.instance());

        final Scanner in = new Scanner(System.in, "UTF-8");

        System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
        System.out.println();

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        final String authorizationUrl = service.getAuthorizationUrl(secretState);
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize ScribeJava here:");
        System.out.println(authorizationUrl);
        System.out.println("And paste the authorization code here");
        System.out.print(">>");
        final String code = in.nextLine();
        System.out.println();

        System.out.println("And paste the state from server here. We have set 'secretState'='" + secretState + "'.");
        System.out.print(">>");
        final String value = in.nextLine();
        if (secretState.equals(value)) {
            System.out.println("State value does match!");
        } else {
            System.out.println("Ooops, state value does not match!");
            System.out.println("Expected = " + secretState);
            System.out.println("Got      = " + value);
            System.out.println();
        }

        System.out.println("Trading the Authorization Code for an Access Token...");
        final OAuth2AccessToken accessToken = service.getAccessToken(code);
        System.out.println("Got the Access Token!");
        System.out.println("(The raw response looks like this: " + accessToken.getRawResponse() + "')");
        System.out.println();

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
        service.signRequest(accessToken, request);
        try (Response response = service.execute(request)) {
            System.out.println("Got it! Lets see what we found...");
            System.out.println();
            System.out.println(response.getCode());
            System.out.println(response.getBody());
        }
        System.out.println();
        System.out.println("Thats it man! Go and build something awesome with ScribeJava! :)");
    }

    public void interfaceConsola()
    {
        Scanner keyboardScanner = new Scanner(System.in);
        int erro;
        while(true) {
            erro = 0;
            System.out.println("Menu:");
            System.out.println("\t1 - Registar pessoa");
            System.out.println("\t2 - Editar pessoa");
            System.out.println("\t3 - Criar Eleicao");
            System.out.println("\t4 - Editar Eleicao");
            System.out.println("\t5 - Listar Pessoas");
            System.out.println("\t6 - Litar Eleicoes");
            System.out.println("\t7 - Litar Mesas");
            //System.out.println("\t8 - Conectar aoa facebook");
            switch (keyboardScanner.nextLine()) {
                case "1": // ----------------------------- criar pessoa ------------------------------------
                    erro = 0;
                    System.out.println("Registar pessoa:");
                    String tipo = "";
                    while(true) {
                        erro = 0;
                        System.out.print("\tTipo(Aluno|Docente|Funcionario): ");
                        tipo = keyboardScanner.nextLine();
                        if(tipo.equals("<<")){
                            erro = 1;
                            break;
                        }
                        switch(tipo){
                            case "Aluno":
                                break;
                            case "Docente":
                                break;
                            case "Funcionario":
                                break;
                            default:
                                System.out.println("Tipo invalido!");
                                continue;
                        }
                        break;
                    }
                    if(erro ==1){
                        break;
                    }
                    System.out.print("\tNome: ");
                    String nome = keyboardScanner.nextLine();
                    if(nome.equals("<<")) break;
                    String username;
                    while (true) {
                        erro = 0;
                        System.out.print("\tUsername: ");
                        username = keyboardScanner.nextLine();
                        if(username.equals("<<")){
                            erro = 1;
                            break;
                        }
                        Timer = Calendar.getInstance();
                        Timer.add(Calendar.SECOND, 30);
                        while (true) {
                            erro = 0;
                            try {
                                if (rs.findPessoaByUsername(username) != null) {
                                    System.out.println("Ja existe uma pessoa com esse username!");
                                    erro = 2;
                                    break;
                                }
                            } catch (Exception e) {
                                System.out.println(e);
                                rebind();
                                if (Timer.after(Calendar.getInstance())) continue;
                                erro = -1;
                                break;
                            }
                            break;
                        }
                        if (erro == 2) continue;
                        break;
                    }
                    if (erro == -1) {
                        System.out.print("Conexao perdida");
                        break;
                    } else if (erro == 1) { // voltar atraz
                        break;
                    }
                    System.out.print("\tPassword: ");
                    String password = keyboardScanner.nextLine();
                    if(password.equals("<<")) break;
                    System.out.print("\tDepartamento: ");
                    String departamento = keyboardScanner.nextLine();
                    if(departamento.equals("<<")) break;
                    System.out.print("\tContacto: ");
                    String contacto = keyboardScanner.nextLine();
                    if(contacto.equals("<<")) break;
                    System.out.print("\tMorada: ");
                    String morada = keyboardScanner.nextLine();
                    if(morada.equals("<<")) break;
                    String numero_CC;
                    while (true) {
                        erro = 0;
                        System.out.print("\tNumero do cartao de cidadao: ");
                        numero_CC = keyboardScanner.nextLine();
                        if(numero_CC.equals("<<")){
                            erro = 1;
                            break;
                        }
                        Timer = Calendar.getInstance();
                        Timer.add(Calendar.SECOND, 30);
                        while (true) {
                            erro = 0;
                            try {
                                if (rs.findPessoa(numero_CC) != null) {
                                    System.out.println("Ja existe uma pessoa com esse numero de cartao de cidadao!");
                                    erro = 2;
                                    break;
                                }
                            } catch (Exception e) {
                                rebind();
                                if (Timer.after(Calendar.getInstance())) continue;
                                erro = -1;
                                break;
                            }
                            break;
                        }
                        if (erro == 2) continue;
                        break;
                    }
                    if (erro == -1) {
                        System.out.println("Conexao perdida!");
                        break;
                    } else if (erro == 1) { // voltar atraz
                        break;
                    }
                    String validade;
                    GregorianCalendar validade_CC = null;
                    while (true) {
                        erro = 0;
                        System.out.print("\tValidade do cartao de cidadao(dd/mm/yyyy): ");
                        validade = keyboardScanner.nextLine();
                        if(validade.equals("<<")){
                            erro = 1;
                            break;
                        }
                        String[] v = validade.split("/");
                        if (v.length != 3) {
                            System.out.println("Formato invalido!");
                            continue;
                        }
                        try {
                            int dia = Integer.parseInt(v[0]);
                            int mes = Integer.parseInt(v[1]);
                            int ano = Integer.parseInt(v[2]);
                            validade_CC = new GregorianCalendar(ano, mes-1, dia);
                        } catch (Exception e) {
                            System.out.println("Formato invalido!");
                            continue;
                        }
                        break;
                    }
                    if (erro == 1) { // voltar atraz
                        break;
                    }
                    Timer = Calendar.getInstance();
                    Timer.add(Calendar.SECOND, 30);
                    while (true) {
                        erro = 0;
                        try {
                            rs.createPessoa(tipo, nome, username, password, departamento, contacto, morada, numero_CC, validade_CC);
                            System.out.println("Peessoa registada");
                        } catch (DataConflictException.DuplicatedUsername e) {
                            System.out.println("Ja existe uma pessoa com esse username!");
                            break;
                        } catch (DataConflictException.DuplicatedNumero_CC e) {
                            System.out.println("Ja existe uma pessoa com esse numero de cartao de cidadao!");
                            break;
                        } catch (Exception e) {
                            rebind();
                            if (Timer.after(Calendar.getInstance())) continue;
                            erro = -1;
                            break;
                        }
                        break;
                    }
                    if (erro == -1) {
                        System.out.println("Conexao perdida!");
                        break;
                    }
                    break;

                case "2": // ----------------------------- editar pessoa ------------------------------------
                    Properties map = null;
                    Timer = Calendar.getInstance();
                    Timer.add(Calendar.SECOND, 30);
                    while(true){
                        erro=0;
                        try {
                            map = GetHashMap(rs.listarPessoas());
                        }catch (Exception e){
                            rebind();
                            if (Timer.after(Calendar.getInstance())) continue;
                            erro = -1;
                            break;
                        }
                        map.put("<<", "Voltar");
                        while(true) {
                            erro = 0;
                            System.out.println("Editar Pessoa:");
                            for(int i=1; i< Integer.parseInt(map.getProperty("size")); i++){
                                System.out.println("\t" + i + " - " + map.getProperty(""+i));
                            }
                            System.out.println("\t<< - Voltar");
                            username = map.getProperty(keyboardScanner.nextLine(), "0");
                            if(username.equals("0")){
                                System.out.println("Opcao invalida!");
                                continue;
                            }
                            else if(username.equals("Voltar")){
                                erro = 1;
                            }
                            break;
                        }
                        if(erro == 1){ // voltar a traz
                            break;
                        }
                        Pessoa pessoa= null;
                        Timer = Calendar.getInstance();
                        Timer.add(Calendar.SECOND, 30);
                        while(true){
                            erro = 0;
                            try{
                                pessoa = rs.findPessoaByUsername(username);
                            }catch (Exception e) {
                                rebind();
                                if (Timer.after(Calendar.getInstance())) continue;
                                erro = -1;
                                break;
                            }
                            break;
                        }
                        if(erro==-1){
                            break;
                        }
                        if(pessoa==null){
                            System.out.println("Erro ao encontrar a eleicao");
                        }
                        String numero_CC_anterior = new String(pessoa.GetNumero_CC());
                        while(true) {
                            erro = 0;
                            System.out.println("Parametro:");
                            System.out.println("\t1 - Nome");
                            System.out.println("\t2 - Username");
                            System.out.println("\t3 - Password");
                            System.out.println("\t4 - Departamento");
                            System.out.println("\t5 - Contacto");
                            System.out.println("\t6 - Morada");
                            System.out.println("\t7 - Numero do cartao de cidadao");
                            System.out.println("\t8 - Validade do cartao de cidadao");
                            System.out.println("\t<< - Voltar");
                            switch(keyboardScanner.nextLine()){
                                case "1":
                                    System.out.print("\tNome: ");
                                    nome = keyboardScanner.nextLine();
                                    pessoa.SetNome(nome);
                                    if(nome.equals("<<")){
                                        erro = 1;
                                        break;
                                    }
                                    break;
                                case "2":
                                    while (true) {
                                        erro = 0;
                                        System.out.print("\tUsername: ");
                                        username = keyboardScanner.nextLine();
                                        if(username.equals("<<")){
                                            erro = 1;
                                            break;
                                        }
                                        if(username.equals(pessoa.GetUsername())){
                                            break;
                                        }
                                        Timer = Calendar.getInstance();
                                        Timer.add(Calendar.SECOND, 30);
                                        while (true) {
                                            erro = 0;
                                            try {
                                                if (rs.findPessoaByUsername(username) != null) {
                                                    System.out.println("Ja existe uma pessoa com esse username!");
                                                    erro = 2;
                                                    break;
                                                }
                                            } catch (Exception e) {
                                                rebind();
                                                if (Timer.after(Calendar.getInstance())) continue;
                                                erro = -1;
                                                break;
                                            }
                                            break;
                                        }
                                        if (erro == 2){
                                            continue;
                                        }
                                        break;
                                    }
                                    if(erro == 1){ // voltar a traz
                                        break;
                                    }
                                    if(erro == -1) {
                                        break;
                                    }
                                    pessoa.SetUsername(username);
                                    break;
                                case "3":
                                    System.out.print("\tPassword: ");
                                    password = keyboardScanner.nextLine();
                                    if(password.equals("<<")){
                                        erro = 1;
                                        break;
                                    }
                                    pessoa.SetPassword(password);
                                    break;
                                case "4":
                                    System.out.print("\tDepartamento: ");
                                    departamento = keyboardScanner.nextLine();
                                    pessoa.SetDepartamento(departamento);
                                    if(departamento.equals("<<")){
                                        erro = 1;
                                        break;
                                    }
                                    break;
                                case "5":
                                    System.out.print("\tContacto: ");
                                    contacto = keyboardScanner.nextLine();
                                    pessoa.SetContacto(contacto);
                                    if(contacto.equals("<<")){
                                        erro = 1;
                                        break;
                                    }
                                    break;
                                case "6":
                                    System.out.print("\tMorada: ");
                                    morada = keyboardScanner.nextLine();
                                    pessoa.SetMorada(morada);
                                    if(morada.equals("<<")){
                                        erro = 1;
                                        break;
                                    }
                                    break;
                                case "7":
                                    while (true) {
                                        erro = 0;
                                        System.out.print("\tNumero do cartao de cidadao: ");
                                        numero_CC = keyboardScanner.nextLine();
                                        if(numero_CC.equals("<<")){
                                            erro = 1;
                                            break;
                                        }
                                        if(numero_CC.equals(pessoa.GetNumero_CC())){
                                            break;
                                        }
                                        Timer = Calendar.getInstance();
                                        Timer.add(Calendar.SECOND, 30);
                                        while (true) {
                                            erro = 0;
                                            try {
                                                if (rs.findPessoa(numero_CC) != null) {
                                                    System.out.println("Ja existe uma pessoa com esse numero de cartao de cidadao!");
                                                    erro = 2;
                                                    break;
                                                }
                                            } catch (Exception e) {
                                                rebind();
                                                if (Timer.after(Calendar.getInstance())) continue;
                                                erro = -1;
                                                break;
                                            }
                                            break;
                                        }
                                        if (erro == 2) continue;
                                        break;
                                    }
                                    if(erro == 1){ // voltar a traz
                                        break;
                                    }
                                    if(erro == -1) {
                                        break;
                                    }
                                    pessoa.SetNumero_CC(numero_CC);
                                    break;
                                case "8":
                                    while (true) {
                                        erro = 0;
                                        System.out.print("\tValidade do cartao de cidadao: ");
                                        validade = keyboardScanner.nextLine();
                                        if(validade.equals("<<")){
                                            erro = 1;
                                            break;
                                        }
                                        String[] v = validade.split("/");

                                        try {
                                            int dia = Integer.parseInt(v[0]);
                                            int mes = Integer.parseInt(v[1]);
                                            int ano = Integer.parseInt(v[2]);
                                            pessoa.SetValidade_CC(new GregorianCalendar(ano, mes-1, dia));
                                        } catch (Exception ex) {
                                            System.out.println("Formato invalido!");
                                            continue;
                                        }
                                        break;
                                    }
                                    break;
                                case "<<":
                                    erro = 1;
                                    break;
                                default:
                                    System.out.println("Opcao invalida!");
                                    continue;
                            }
                            if(erro == 1) { // voltar a traz
                                break;
                            }
                            if(erro == -1) {
                                break;
                            }

                            Timer = Calendar.getInstance();
                            Timer.add(Calendar.SECOND, 30);
                            while(true){
                                erro = 0;
                                try {
                                    rs.editPessoa(pessoa.GetNome(), pessoa.GetUsername(), pessoa.GetPassword(), pessoa.GetDepartamento(), pessoa.GetContacto(), pessoa.GetMorada(), pessoa.GetNumero_CC(), pessoa.GetValidade_CC(), numero_CC_anterior);
                                    System.out.println("Pessoa editada");
                                }catch (DataConflictException.DuplicatedUsername e){
                                    System.out.println("Ja existe uma pessoa com esse username!");
                                    break;
                                }catch (DataConflictException.DuplicatedNumero_CC e){
                                    System.out.println("Ja existe uma pessoa com esse numero de cartao de cidadao!");
                                    break;
                                }catch (NotFoundException.PessoaNF e){
                                    System.out.println("Erro ao procurar a pessoa!");
                                    break;
                                }catch (Exception e){
                                    rebind();
                                    if (Timer.after(Calendar.getInstance())) continue;
                                    erro = -1;
                                    break;
                                }
                                break;
                            }
                            if(erro==-1){
                                break;
                            }
                            break;
                        }
                        if(erro == 1){ // voltar atraz
                            Timer = Calendar.getInstance();
                            Timer.add(Calendar.SECOND, 30);
                            continue;
                        }
                        break;
                    }
                    if(erro==-1){
                        System.out.println("Conexao perdida!");
                        break;
                    }
                    break;

                case "3":   // ----------------------------- criar eleição ------------------------------------
                    System.out.println("Criar Eleicao:");
                    String titulo;
                    while (true) {
                        erro = 0;
                        System.out.print("\tTitulo: ");
                        titulo = keyboardScanner.nextLine();
                        if(titulo.equals("<<")){
                            erro = 1;
                            break;
                        }
                        Timer = Calendar.getInstance();
                        Timer.add(Calendar.SECOND, 30);
                        while (true) {
                            erro = 0;
                            try {
                                if (rs.findEleicao(titulo) != null) {
                                    System.out.println("Ja existe uma eleicao com esse titulo!");
                                    erro = 2;
                                    break;
                                }
                            } catch (Exception e) {
                                rebind();
                                if (Timer.after(Calendar.getInstance())) continue;
                                erro = -1;
                                break;
                            }
                            break;
                        }
                        if (erro == 2) continue;
                        break;
                    }
                    if (erro == -1) {
                        System.out.println("Conexao perdida!");
                        break;
                    } else if (erro == 1) { // voltar atraz
                        break;
                    }
                    System.out.print("\tDescricao: ");
                    String descricao = keyboardScanner.nextLine();
                    if(descricao.equals("<<")) break;
                    tipo = "";
                    while(true) {
                        erro = 0;
                        System.out.print("\tTipo(Aluno|Docente|Funcionario): ");
                        tipo = keyboardScanner.nextLine();
                        if(tipo.equals("<<")){
                            erro = 1;
                            break;
                        }
                        switch(tipo){
                            case "Aluno":
                                break;
                            case "Docente":
                                break;
                            case "Funcionario":
                                break;
                            default:
                                System.out.println("Tipo invalido!");
                                continue;
                        }
                        break;
                    }
                    if(erro ==1){
                        break;
                    }
                    String start;
                    GregorianCalendar inicio = null;
                    while (true) {
                        erro = 0;
                        System.out.print("\tInicio da Eleicao(dd/mm/yyyy hh:mm): ");
                        start = keyboardScanner.nextLine();
                        if(start.equals("<<")){
                            erro = 1;
                            break;
                        }
                        String[] s1 = start.split(" ");
                        if (s1.length != 2) {
                            System.out.println("Formato invalido!");
                            continue;
                        }
                        String[] s2 = s1[0].split("/");
                        if (s2.length != 3) {
                            System.out.println("Formato invalido!");
                            continue;
                        }
                        String[] s3 = s1[1].split(":");
                        if (s3.length != 2) {
                            System.out.println("Formato invalido!");
                            continue;
                        }
                        try {
                            int dia = Integer.parseInt(s2[0]);
                            int mes = Integer.parseInt(s2[1]);
                            int ano = Integer.parseInt(s2[2]);
                            int hora = Integer.parseInt(s3[0]);
                            int minuto = Integer.parseInt(s3[1]);
                            inicio = new GregorianCalendar(ano, mes-1, dia, hora, minuto);
                        } catch (Exception e) {
                            System.out.println("Formato invalido!");
                            continue;
                        }
                        break;
                    }
                    if (erro == 1) { // voltar atraz
                        break;
                    }
                    String end;
                    GregorianCalendar fim = null;
                    while (true) {
                        erro = 0;
                        System.out.print("\tFim da Eleicao(dd/mm/yyyy hh:mm): ");
                        end = keyboardScanner.nextLine();
                        if(end.equals("<<")){
                            erro = 1;
                            break;
                        }
                        String[] e1 = end.split(" ");
                        if (e1.length != 2) {
                            System.out.println("Formato invalido!");
                            continue;
                        }
                        String[] e2 = e1[0].split("/");
                        if (e2.length != 3) {
                            System.out.println("Formato invalido!");
                            continue;
                        }
                        String[] e3 = e1[1].split(":");
                        if (e3.length != 2) {
                            System.out.println("Formato invalido!");
                            continue;
                        }
                        try {
                            int dia = Integer.parseInt(e2[0]);
                            int mes = Integer.parseInt(e2[1]);
                            int ano = Integer.parseInt(e2[2]);
                            int hora = Integer.parseInt(e3[0]);
                            int minuto = Integer.parseInt(e3[1]);
                            fim = new GregorianCalendar(ano, mes-1, dia, hora, minuto);
                            if (fim.before(inicio)) {
                                System.out.println("O fim da eleicao nao pode ser antes do inicio!");
                                continue;
                            }
                        } catch (Exception ex) {
                            System.out.println("Formato invalido!");
                            continue;
                        }
                        break;
                    }
                    if (erro == 1) { // voltar atraz
                        break;
                    }
                    Timer = Calendar.getInstance();
                    Timer.add(Calendar.SECOND, 30);
                    while (true) {
                        erro = 0;
                        try {
                            rs.createEleicao(titulo, descricao, inicio, fim, tipo);
                            System.out.println("Eleicao criada");
                        } catch (DataConflictException e) {
                            System.out.println("Ja existe uma eleicao com esse titulo!");
                            break;
                        } catch (Exception e) {
                            rebind();
                            if (Timer.after(Calendar.getInstance())) continue;
                            erro = -1;
                            break;
                        }
                        break;
                    }
                    if (erro == -1) {
                        System.out.println("Conexao perdida!");
                        break;
                    }
                    break;

                case "4": // ----------------------------- editar eleição ------------------------------------
                    map = null;
                    Timer = Calendar.getInstance();
                    Timer.add(Calendar.SECOND, 30);
                    while(true){
                        erro=0;
                        try {
                            map = GetHashMap(rs.listarEleicoesNaoIniciadas());
                        }catch (Exception e){
                            rebind();
                            if (Timer.after(Calendar.getInstance())) continue;
                            erro = -1;
                            break;
                        }

                        //...
                        map.put("<<", "Voltar");
                        while(true) {
                            erro = 0;
                            System.out.println("Editar Eleicao:");
                            for(int i=1; i< Integer.parseInt(map.getProperty("size")); i++){
                                System.out.println("\t" + i + " - " + map.getProperty(""+i));
                            }
                            System.out.println("\t<< - Voltar");
                            titulo = map.getProperty(keyboardScanner.nextLine(), "0");
                            if(titulo.equals("0")){
                                System.out.println("Opcao invalida!");
                                continue;
                            }
                            else if(titulo.equals("Voltar")){
                                erro = 1;
                            }
                            break;
                        }
                        if(erro == 1){ // voltar a traz
                            break;
                        }
                        Eleicao eleicao = null;
                        Timer = Calendar.getInstance();
                        Timer.add(Calendar.SECOND, 30);
                        while(true){
                            erro = 0;
                            try{
                                eleicao = rs.findEleicao(titulo);
                            }catch (Exception e) {
                                rebind();
                                if (Timer.after(Calendar.getInstance())) continue;
                                erro = -1;
                                break;
                            }
                            break;
                        }
                        if(erro==-1){
                            break;
                        }
                        if(eleicao==null){
                            System.out.println("Erro ao encontrar a eleicao");
                            erro=3;
                            break;
                        }
                        String titulo_anterior = new String(eleicao.GetTitulo());
                        while(true) {
                            erro = 0;
                            System.out.println("Parametro:");
                            System.out.println("\t1 - Titulo");
                            System.out.println("\t2 - Descricao");
                            System.out.println("\t3 - Inicio");
                            System.out.println("\t4 - Fim");
                            System.out.println("\t<< - Voltar");
                            switch(keyboardScanner.nextLine()){
                                case "1":
                                    while (true) {
                                        erro = 0;
                                        System.out.print("\tTitulo: ");
                                        titulo = keyboardScanner.nextLine();
                                        if(titulo.equals("<<")){
                                            erro = 1;
                                            break;
                                        }
                                        if(titulo.equals(eleicao.GetTitulo())){
                                            break;
                                        }
                                        Timer = Calendar.getInstance();
                                        Timer.add(Calendar.SECOND, 30);
                                        while (true) {
                                            erro = 0;
                                            try {
                                                if (rs.findEleicao(titulo) != null) {
                                                    System.out.println("Ja existe uma eleicao com esse titulo!");
                                                    erro = 2;
                                                    break;
                                                }
                                            } catch (Exception e) {
                                                rebind();
                                                if (Timer.after(Calendar.getInstance())) continue;
                                                erro = -1;
                                                break;
                                            }
                                            break;
                                        }
                                        if (erro == 2) continue;
                                        break;
                                    }
                                    if(erro == 1){ // voltar a traz
                                        break;
                                    }
                                    if(erro == -1) {
                                        break;
                                    }
                                    eleicao.SetTitulo(titulo);
                                    break;
                                case "2":
                                    System.out.print("\tDescricao: ");
                                    descricao = keyboardScanner.nextLine();
                                    if(titulo.equals("<<")){
                                        erro = 1;
                                        break;
                                    }
                                    eleicao.SetDescricao(descricao);
                                    break;
                                case "3":
                                    while (true) {
                                        erro = 0;
                                        System.out.print("\tInicio da Eleicao(dd/mm/yyyy hh:mm): ");
                                        start = keyboardScanner.nextLine();
                                        if(start.equals("<<")){
                                            erro = 1;
                                            break;
                                        }
                                        String[] s1 = start.split(" ");
                                        if (s1.length != 2) {
                                            System.out.println("Formato invalido!");
                                            continue;
                                        }
                                        String[] s2 = s1[0].split("/");
                                        if (s2.length != 3) {
                                            System.out.println("Formato invalido!");
                                            continue;
                                        }
                                        String[] s3 = s1[1].split(":");
                                        if (s3.length != 2) {
                                            System.out.println("Formato invalido!");
                                            continue;
                                        }
                                        try {
                                            int dia = Integer.parseInt(s2[0]);
                                            int mes = Integer.parseInt(s2[1]);
                                            int ano = Integer.parseInt(s2[2]);
                                            int hora = Integer.parseInt(s3[0]);
                                            int minuto = Integer.parseInt(s3[1]);
                                            eleicao.SetInicio(new GregorianCalendar(ano, mes-1, dia, hora, minuto));
                                            if(eleicao.GetFim().before(eleicao.GetInicio())){
                                                System.out.println("O inicio da eleicao nao pode ser depois do fim!");
                                                continue;
                                            }
                                        } catch (Exception e) {
                                            System.out.println("Formato invalido!");
                                            continue;
                                        }
                                        break;
                                    }
                                    break;
                                case "4":
                                    while (true) {
                                        erro = 0;
                                        System.out.print("\tFim da Eleicao(dd/mm/yyyy hh:mm): ");
                                        end = keyboardScanner.nextLine();
                                        if(end.equals("<<")){
                                            erro = 1;
                                            break;
                                        }
                                        String[] e1 = end.split(" ");
                                        if (e1.length != 2) {
                                            System.out.println("Formato invalido!");
                                            continue;
                                        }
                                        String[] e2 = e1[0].split("/");
                                        if (e2.length != 3) {
                                            System.out.println("Formato invalido!");
                                            continue;
                                        }
                                        String[] e3 = e1[1].split(":");
                                        if (e3.length != 2) {
                                            System.out.println("Formato invalido!");
                                            continue;
                                        }
                                        try {
                                            int dia = Integer.parseInt(e2[0]);
                                            int mes = Integer.parseInt(e2[1]);
                                            int ano = Integer.parseInt(e2[2]);
                                            int hora = Integer.parseInt(e3[0]);
                                            int minuto = Integer.parseInt(e3[1]);
                                            eleicao.SetFim(new GregorianCalendar(ano, mes-1, dia, hora, minuto));
                                            if(eleicao.GetFim().before(eleicao.GetInicio())){
                                                System.out.println("O fim da eleicao nao pode ser antes do inicio!");
                                                continue;
                                            }
                                        } catch (Exception ex) {
                                            System.out.println("Formato invalido!");
                                            continue;
                                        }
                                        break;
                                    }
                                    break;
                                case "<<":
                                    erro = 1;
                                    break;
                                default:
                                    System.out.println("Opcao invalida!");
                                    continue;
                            }
                            if(erro == 1) { // voltar a traz
                                break;
                            }
                            if(erro == -1) {
                                break;
                            }
                            Timer = Calendar.getInstance();
                            Timer.add(Calendar.SECOND, 30);
                            while(true){
                                erro = 0;
                                try {
                                    rs.editEleicao(eleicao.GetTitulo(), eleicao.GetDescricao(), eleicao.GetInicio(), eleicao.GetFim(), eleicao.GetTipo(), titulo_anterior);
                                    System.out.println("Eleicao editada");
                                }catch (TimeBoundsException.EleicaoAlreadyStarted e){
                                    System.out.println("A eleicao ja comecou!");
                                    break;
                                }catch (DataConflictException e){
                                    System.out.println("Ja existe uma eleicao com esse titulo!");
                                    break;
                                }catch (NotFoundException.EleicaoNF e){
                                    System.out.println("Erro ao procurar a eleicao!");
                                    break;
                                }catch (Exception e){
                                    rebind();
                                    if (Timer.after(Calendar.getInstance())) continue;
                                    erro = -1;
                                    break;
                                }
                                break;
                            }
                            break;
                        }
                        if(erro == 1){ // voltar atraz
                            Timer = Calendar.getInstance();
                            Timer.add(Calendar.SECOND, 30);
                            continue;
                        }
                        break;
                    }
                    if(erro==-1){
                        System.out.println("Conexao perdida!");
                        break;
                    }
                    if(erro==3){
                        break;
                    }
                    break;
                case "5": // ----------------------------- listar pessoas ------------------------------------
                    map = null;
                    Timer = Calendar.getInstance();
                    Timer.add(Calendar.SECOND, 30);
                    while(true) {
                        erro = 0;
                        try {
                            map = GetHashMap(rs.listarPessoas());
                        } catch (Exception e) {
                            rebind();
                            if (Timer.after(Calendar.getInstance())) continue;
                            erro = -1;
                            break;
                        }

                        map.put("<<", "Voltar");
                        while (true) {
                            erro = 0;
                            System.out.println("Selecione uma pessoa:");
                            for (int i = 1; i < Integer.parseInt(map.getProperty("size")); i++) {
                                System.out.println("\t" + i + " - " + map.getProperty("" + i));
                            }
                            System.out.println("\t<< - Voltar");
                            username = map.getProperty(keyboardScanner.nextLine(), "0");
                            if (username.equals("0")) {
                                System.out.println("Opcao invalida!");
                                continue;
                            } else if (username.equals("Voltar")) {
                                erro = 1;
                                break;
                            }
                            Timer = Calendar.getInstance();
                            Timer.add(Calendar.SECOND, 30);
                            while(true){
                                erro = 0;
                                try {
                                    System.out.println(rs.consultarPessoa(username));
                                    keyboardScanner.nextLine();
                                    erro = 1;
                                }catch (NotFoundException.PessoaNF e){
                                    System.out.println("Erro ao procurar a pessoa!");
                                    erro = 3;
                                    break;
                                }catch (Exception e){
                                    rebind();
                                    if (Timer.after(Calendar.getInstance())) continue;
                                    erro = -1;
                                    break;
                                }
                                break;
                            }
                            if (erro == 1){ // voltar a traz
                                Timer = Calendar.getInstance();
                                Timer.add(Calendar.SECOND, 30);
                                continue;
                            }
                            break;
                        }
                        if (erro == 1) { // voltar a traz
                            break;
                        }
                        break;
                    }
                    if(erro == -1){
                        System.out.println("Conexao perdida!");
                        break;
                    }
                    if(erro==3){
                        break;
                    }
                    break;
                case "6": // ----------------------------- listar eleicoes ------------------------------------
                    map = null;
                    Timer = Calendar.getInstance();
                    Timer.add(Calendar.SECOND, 30);
                    while(true) {
                        erro = 0;
                        try {
                            map = GetHashMap(rs.listarEleicoes());
                        } catch (Exception e) {
                            rebind();
                            if (Timer.after(Calendar.getInstance())) continue;
                            erro = -1;
                            break;
                        }

                        map.put("<<", "Voltar");
                            System.out.println("Selecione uma Eleicao:");
                            for (int i = 1; i < Integer.parseInt(map.getProperty("size")); i++) {
                                System.out.println("\t" + i + " - " + map.getProperty("" + i));
                            }
                            System.out.println("\t<< - Voltar");
                            titulo = map.getProperty(keyboardScanner.nextLine(), "0");
                            if (titulo.equals("0")) {
                                System.out.println("Opcao invalida!");
                                Timer = Calendar.getInstance();
                                Timer.add(Calendar.SECOND, 30);
                                continue;
                            } else if (titulo.equals("Voltar")) {
                                erro = 1;
                                break;
                            }

                            while(true){
                                erro = 0;
                                System.out.println(titulo + ":");
                                System.out.println("\t1 - Consultar");
                                System.out.println("\t2 - Adicionar lista");
                                System.out.println("\t3 - Consultar lista");
                                System.out.println("\t4 - Editar lista");
                                System.out.println("\t5 - Adicionar mesa");
                                System.out.println("\t6 - Remover mesa");
                                System.out.println("\t<< - Voltar");
                                switch (keyboardScanner.nextLine()){
                                    case "1":
                                        Timer = Calendar.getInstance();
                                        Timer.add(Calendar.SECOND, 30);
                                        while(true){
                                            erro = 0;
                                            try {
                                                System.out.println(rs.consultarEleicao(titulo));
                                                keyboardScanner.nextLine();
                                                erro = 1;
                                            }catch (NotFoundException.EleicaoNF e){
                                                System.out.println("Erro ao procurar a eleicao!");
                                                break;
                                            }catch (Exception e){
                                                rebind();
                                                if (Timer.after(Calendar.getInstance())) continue;
                                                erro = -1;
                                                break;
                                            }
                                            break;
                                        }
                                        if(erro == 1){ // voltar a traz;
                                            continue;
                                        }
                                        break;
                                    case "2":
                                        while(true){
                                            erro = 0;
                                            System.out.print("\tNome: ");
                                            nome = keyboardScanner.nextLine();
                                            if(nome.equals("<<")){
                                                erro = 1;
                                                break;
                                            }
                                            Timer = Calendar.getInstance();
                                            Timer.add(Calendar.SECOND, 30);
                                            while(true){
                                                erro =0;
                                                try{
                                                    rs.addLista( titulo, nome);
                                                    System.out.println("Lista adicionada");
                                                }catch (NotFoundException.EleicaoNF e){
                                                    System.out.println("Erro ao procurar a eleicao");
                                                    erro = 3;
                                                    break;
                                                }catch (DataConflictException e){
                                                    System.out.println("Ja existe uma lista com esse nome");
                                                    erro = 2;
                                                    break;
                                                }catch (TimeBoundsException.EleicaoAlreadyStarted e){
                                                    System.out.println("A eleição ja comecou");
                                                    erro = 3;
                                                    break;
                                                }catch (Exception e){
                                                    rebind();
                                                    System.out.println(e);
                                                    if (Timer.after(Calendar.getInstance())) continue;
                                                    erro = -1;
                                                    break;
                                                }
                                                break;
                                            }
                                            if(erro==2) continue;
                                            break;
                                        }
                                        if(erro==-1){
                                            break;
                                        }
                                        if(erro==3){
                                            break;
                                        }
                                        continue;
                                    case "3":
                                        Timer = Calendar.getInstance();
                                        Timer.add(Calendar.SECOND, 30);
                                        while(true) {
                                            erro = 0;
                                            try {
                                                map = GetHashMap(rs.listarListas(titulo));
                                            }catch (NotFoundException.EleicaoNF e){
                                                System.out.println("Erro ao procurar a eleicao!");
                                                erro=3;
                                                break;
                                            } catch (Exception e) {
                                                rebind();
                                                if (Timer.after(Calendar.getInstance())) continue;
                                                erro = -1;
                                                break;
                                            }
                                            map.put("<<", "Voltar");
                                            while(true){
                                                erro = 0;
                                                System.out.println("Selecione uma lista:");
                                                for (int i = 1; i < Integer.parseInt(map.getProperty("size")); i++) {
                                                    System.out.println("\t" + i + " - " + map.getProperty("" + i));
                                                }
                                                System.out.println("\t<< - Voltar");
                                                nome = map.getProperty(keyboardScanner.nextLine(), "0");
                                                if (nome.equals("0")) {
                                                    System.out.println("Opcao invalida!");
                                                    continue;
                                                } else if (nome.equals("Voltar")) {
                                                    erro = 1;
                                                    break;
                                                }
                                                break;
                                            }
                                            if(erro ==1){ // voltar a traz
                                                break;
                                            }
                                            break;
                                        }
                                        if(erro==-1){
                                            break;
                                        }
                                        if(erro==3){
                                            break;
                                        }
                                        continue;
                                    case "4":
                                        Timer = Calendar.getInstance();
                                        Timer.add(Calendar.SECOND, 30);
                                        while(true) {
                                            erro = 0;
                                            try {
                                                map = GetHashMap(rs.listarListas(titulo));
                                            }catch (NotFoundException.EleicaoNF e){
                                                System.out.println("Erro ao procurar a eleicao!");
                                                break;
                                            } catch (Exception e) {
                                                rebind();
                                                if (Timer.after(Calendar.getInstance())) continue;
                                                erro = -1;
                                                break;
                                            }
                                            map.put("<<", "Voltar");
                                            while(true){
                                                erro = 0;
                                                System.out.println("Selecione uma lista:");
                                                for (int i = 1; i < Integer.parseInt(map.getProperty("size")); i++) {
                                                    System.out.println("\t" + i + " - " + map.getProperty("" + i));
                                                }
                                                System.out.println("\t<< - Voltar");
                                                nome = map.getProperty(keyboardScanner.nextLine(), "0");
                                                if (nome.equals("0")) {
                                                    System.out.println("Opcao invalida!");
                                                    continue;
                                                } else if (nome.equals("Voltar")) {
                                                    erro = 1;
                                                    break;
                                                }
                                                break;
                                            }
                                            if(erro == 1){
                                                break;
                                            }
                                            while(true){
                                                erro = 0;
                                                System.out.println(nome + ":");
                                                System.out.println("\t1 - Mudar o nome");
                                                System.out.println("\t2 - Adicionar pessoa");
                                                System.out.println("\t3 - Remover pessoa");
                                                System.out.println("\t<< - Voltar");
                                                switch (keyboardScanner.nextLine()){
                                                    case "1":
                                                        while(true){
                                                            String nome_anterior = new String(nome);
                                                            erro = 0;
                                                            System.out.print("\tNome: ");
                                                            String n = keyboardScanner.nextLine();
                                                            if(n.equals("<<")){
                                                                erro = 1;
                                                                break;
                                                            }
                                                            nome = n;
                                                            Timer = Calendar.getInstance();
                                                            Timer.add(Calendar.SECOND, 30);
                                                            while(true){
                                                                erro =0;
                                                                try{
                                                                    rs.editLista( titulo, nome, nome_anterior);
                                                                }catch (NotFoundException.EleicaoNF e){
                                                                    System.out.println("Erro ao procurar a eleicao");
                                                                    erro = 3;
                                                                    break;
                                                                }catch (NotFoundException.ListaNF e){
                                                                    System.out.println("Erro ao procurar a lista");
                                                                    erro = 3;
                                                                    break;
                                                                }catch (DataConflictException e){
                                                                    System.out.println("Ja existe uma lista com esse nome");
                                                                    erro = 2;
                                                                    break;
                                                                }catch (TimeBoundsException.EleicaoAlreadyStarted e){
                                                                    System.out.println("A eleição ja comecou");
                                                                    erro = 3;
                                                                    break;
                                                                }catch (Exception e){
                                                                    rebind();
                                                                    if (Timer.after(Calendar.getInstance())) continue;
                                                                    erro = -1;
                                                                    break;
                                                                }
                                                                break;
                                                            }
                                                            if(erro==2) continue;
                                                            break;
                                                        }
                                                        if(erro==-1){
                                                            break;
                                                        }
                                                        if(erro==3){
                                                            break;
                                                        }
                                                        continue;
                                                    case "2":
                                                        Timer = Calendar.getInstance();
                                                        Timer.add(Calendar.SECOND, 30);
                                                        while(true) {
                                                            erro = 0;
                                                            try {
                                                                map = GetHashMap(rs.listarPessoaPorEleicao(titulo));
                                                            } catch (NotFoundException.EleicaoNF e) {
                                                                System.out.println("Erro ao procurar a eleicao!");
                                                            } catch (Exception e) {
                                                                rebind();
                                                                if (Timer.after(Calendar.getInstance())) continue;
                                                                erro = -1;
                                                                break;
                                                            }
                                                            map.put("<<", "Voltar");
                                                            while (true) {
                                                                erro = 0;
                                                                System.out.println("Selecione uma pessoa:");
                                                                for (int i = 1; i < Integer.parseInt(map.getProperty("size")); i++) {
                                                                    System.out.println("\t" + i + " - " + map.getProperty("" + i));
                                                                }
                                                                System.out.println("\t<< - Voltar");
                                                                username = map.getProperty(keyboardScanner.nextLine(), "0");
                                                                if (username.equals("0")) {
                                                                    System.out.println("Opcao invalida!");
                                                                    continue;
                                                                } else if (username.equals("Voltar")) {
                                                                    erro = 1;
                                                                    break;
                                                                }
                                                                break;
                                                            }
                                                            if(erro==1){
                                                                break;
                                                            }
                                                            Timer = Calendar.getInstance();
                                                            Timer.add(Calendar.SECOND, 30);
                                                            while (true) {
                                                                erro = 0;
                                                                try {
                                                                    rs.addPessoa(titulo, nome, username);
                                                                    erro=1;
                                                                } catch (NotFoundException.EleicaoNF e) {
                                                                    System.out.println("Erro ao procurar a eleicao!");
                                                                    erro = 3;
                                                                } catch (NotFoundException.ListaNF e) {
                                                                    System.out.println("Erro ao procurar a lista!");
                                                                    erro=3;
                                                                } catch (NotFoundException.PessoaNF e) {
                                                                    System.out.println("Erro ao procurar a pessoa!");
                                                                    erro = 3;
                                                                } catch (DataConflictException e) {
                                                                    System.out.println("Essa pessoa ja foi adicionada!");
                                                                    erro = 2;
                                                                } catch (Exception e) {
                                                                    rebind();
                                                                    if (Timer.after(Calendar.getInstance())) continue;
                                                                    erro = -1;
                                                                    break;
                                                                }
                                                                break;
                                                            }
                                                            if (erro == 2) {
                                                                Timer = Calendar.getInstance();
                                                                Timer.add(Calendar.SECOND, 30);
                                                                continue;
                                                            }
                                                            break;
                                                        }
                                                        if(erro==-1){
                                                            break;
                                                        }
                                                        if(erro==3){
                                                            break;
                                                        }
                                                        continue;
                                                    case "3":
                                                        Timer = Calendar.getInstance();
                                                        Timer.add(Calendar.SECOND, 30);
                                                        while(true) {
                                                            erro = 0;
                                                            try {
                                                                map = GetHashMap(rs.listarPessoaPorLista(titulo, nome));
                                                            } catch (NotFoundException.EleicaoNF e) {
                                                                System.out.println("Erro ao procurar a eleicao!");
                                                                erro = 3;
                                                                break;
                                                            } catch (NotFoundException.ListaNF e) {
                                                                System.out.println("Erro ao procurar a lista!");
                                                                erro = 3;
                                                                break;
                                                            } catch (Exception e) {
                                                                rebind();
                                                                if (Timer.after(Calendar.getInstance())) continue;
                                                                erro = -1;
                                                                break;
                                                            }
                                                            map.put("<<", "Voltar");
                                                            while (true) {
                                                                erro = 0;
                                                                System.out.println("Selecione uma pessoa:");
                                                                for (int i = 1; i < Integer.parseInt(map.getProperty("size")); i++) {
                                                                    System.out.println("\t" + i + " - " + map.getProperty("" + i));
                                                                }
                                                                System.out.println("\t<< - Voltar");
                                                                username = map.getProperty(keyboardScanner.nextLine(), "0");
                                                                if (username.equals("0")) {
                                                                    System.out.println("Opcao invalida!");
                                                                    continue;
                                                                } else if (username.equals("Voltar")) {
                                                                    erro = 1;
                                                                    break;
                                                                }
                                                                break;
                                                            }
                                                            if(erro == 1){
                                                                break;
                                                            }

                                                            Timer = Calendar.getInstance();
                                                            Timer.add(Calendar.SECOND, 30);
                                                            while (true) {
                                                                erro = 0;
                                                                try {
                                                                    rs.removePessoa(titulo, nome, username);
                                                                    erro=1;
                                                                } catch (NotFoundException.EleicaoNF e) {
                                                                    System.out.println("Erro ao procurar a eleicao!");
                                                                    erro = 3;
                                                                } catch (NotFoundException.ListaNF e) {
                                                                    System.out.println("Erro ao procurar a lista!");
                                                                    erro = 3;
                                                                } catch (NotFoundException.PessoaNF e) {
                                                                    System.out.println("Erro ao procurar a pessoa!");
                                                                } catch (Exception e) {
                                                                    rebind();
                                                                    if (Timer.after(Calendar.getInstance())) continue;
                                                                    erro = -1;
                                                                    break;
                                                                }
                                                                break;
                                                            }
                                                            break;
                                                        }
                                                        if(erro==-1){
                                                            break;
                                                        }
                                                        if(erro==3){
                                                            break;
                                                        }
                                                        continue;
                                                    case "<<":
                                                        erro = 1;
                                                        break;
                                                    default:
                                                        System.out.println("Opção invalida!");
                                                        continue;
                                                }

                                                break;
                                            }

                                            if(erro ==1){ // voltar a traz
                                                Timer = Calendar.getInstance();
                                                Timer.add(Calendar.SECOND, 30);
                                                continue;
                                            }
                                            break;
                                        }
                                        if(erro==-1){ // voltar a traz
                                            break;
                                        }
                                        if(erro==3){
                                            break;
                                        }
                                        Timer = Calendar.getInstance();
                                        Timer.add(Calendar.SECOND, 30);
                                        continue;
                                    case "5":
                                        Timer = Calendar.getInstance();
                                        Timer.add(Calendar.SECOND, 30);
                                        while(true){
                                            String id ="";
                                            erro = 0;
                                            try {
                                                map = GetHashMap(rs.listarMesasFuncionais());
                                            } catch (Exception e) {
                                                rebind();
                                                if (Timer.after(Calendar.getInstance())) continue;
                                                erro = -1;
                                                break;
                                            }
                                            map.put("<<", "Voltar");
                                            while(true){
                                                erro = 0;
                                                System.out.println("Selecione uma Mesa:");
                                                for (int i = 1; i < Integer.parseInt(map.getProperty("size")); i++) {
                                                    System.out.println("\t" + i + " - " + map.getProperty("" + i));
                                                }
                                                System.out.println("\t<< - Voltar");
                                                id = map.getProperty(keyboardScanner.nextLine(), "0");
                                                if (id.equals("0")) {
                                                    System.out.println("Opcao invalida!");
                                                    continue;
                                                } else if (id.equals("Voltar")) {
                                                    erro = 1;
                                                    break;
                                                }
                                                break;
                                            }
                                            if(erro == 1){ // voltar atraz
                                                break;
                                            }

                                            Timer = Calendar.getInstance();
                                            Timer.add(Calendar.SECOND, 30);
                                            while(true){
                                                erro = 0;
                                                try{
                                                    rs.addMesa(titulo, id);
                                                    System.out.println("Mesa adicionada");
                                                    erro = 2;
                                                } catch (NotFoundException.MesaNF e){
                                                    System.out.println("Erro ao procurar a mesa");
                                                    erro = 3;
                                                } catch (DataConflictException e){
                                                    System.out.println("Essa mesa ja foi adicionada");
                                                    erro = 2;
                                                }catch (NotFoundException.EleicaoNF e){
                                                    System.out.println("Erro ao procurar a eleicao");
                                                    erro = 3;
                                                }catch (Exception e){
                                                    rebind();
                                                    if (Timer.after(Calendar.getInstance())) continue;
                                                    erro = -1;
                                                    break;
                                                }
                                                break;
                                            }
                                            if(erro == 2){
                                                Timer = Calendar.getInstance();
                                                Timer.add(Calendar.SECOND, 30);
                                                continue;
                                            }
                                            break;
                                        }
                                        if(erro==-1){ // voltar a traz
                                            break;
                                        }
                                        if(erro==3){
                                            break;
                                        }
                                        continue;
                                    case "6":
                                        Timer = Calendar.getInstance();
                                        Timer.add(Calendar.SECOND, 30);
                                        while(true){
                                            String id = "";
                                            erro = 0;
                                            try {
                                                map = GetHashMap(rs.listarMesasPorEleicao(titulo));
                                            } catch (NotFoundException.EleicaoNF e) {
                                                System.out.println("Erro ao procurar a eleicao");
                                                erro = 3;
                                                break;
                                            }catch (Exception e) {
                                                rebind();
                                                if (Timer.after(Calendar.getInstance())) continue;
                                                erro = -1;
                                                break;
                                            }
                                            map.put("<<", "Voltar");
                                            while(true){
                                                erro = 0;
                                                System.out.println("Selecione uma Mesa:");
                                                for (int i = 1; i < Integer.parseInt(map.getProperty("size")); i++) {
                                                    System.out.println("\t" + i + " - " + map.getProperty("" + i));
                                                }
                                                System.out.println("\t<< - Voltar");
                                                id = map.getProperty(keyboardScanner.nextLine(), "0");
                                                if (id.equals("0")) {
                                                    System.out.println("Opcao invalida!");
                                                    continue;
                                                } else if (id.equals("Voltar")) {
                                                    erro = 1;
                                                    break;
                                                }
                                                break;
                                            }
                                            if(erro==1){ // voltar a traz
                                                break;
                                            }

                                            Timer = Calendar.getInstance();
                                            Timer.add(Calendar.SECOND, 30);
                                            while(true){
                                                erro = 0;
                                                try{
                                                    rs.removeMesa(titulo, id);
                                                    System.out.println("Mesa removida");
                                                    erro = 2;
                                                } catch (NotFoundException.MesaNF e){
                                                    System.out.println("Erro ao procurar a mesa");
                                                    erro = 3;
                                                } catch (DataConflictException e){
                                                    System.out.println("Erro ao procurar a mesa");
                                                    erro = 2;
                                                }catch (NotFoundException.EleicaoNF e){
                                                    System.out.println("Erro ao procurar a eleicao");
                                                    erro = 3;
                                                }catch (Exception e){
                                                    rebind();
                                                    if (Timer.after(Calendar.getInstance())) continue;
                                                    erro = -1;
                                                    break;
                                                }
                                                break;
                                            }
                                            if(erro == 2){
                                                Timer = Calendar.getInstance();
                                                Timer.add(Calendar.SECOND, 30);
                                                continue;
                                            }
                                            break;
                                        }
                                        if(erro==-1){ // voltar a traz
                                            break;
                                        }
                                        if(erro==3){
                                            break;
                                        }
                                        continue;
                                    case "<<":
                                        erro = 1;
                                        break;
                                    default:
                                        System.out.println("Opção invalida!");
                                        continue;
                                }
                                break;
                            }
                            if (erro == 1) { // voltar a traz
                                Timer = Calendar.getInstance();
                                Timer.add(Calendar.SECOND, 30);
                                continue;
                            }
                        break;
                    }
                    if(erro == -1){
                        System.out.println("Conexao perdida!");
                        break;
                    }
                    break;
                case "7":
                    map = null;
                    String id;
                    Timer = Calendar.getInstance();
                    Timer.add(Calendar.SECOND, 30);
                    while(true) {
                        erro = 0;
                        try {
                            map = GetHashMap(rs.listarMesas());
                        } catch (Exception e) {
                            rebind();
                            if (Timer.after(Calendar.getInstance())) continue;
                            erro = -1;
                            break;
                        }

                        map.put("<<", "Voltar");
                        System.out.println("Selecione uma Mesa:");
                        for (int i = 1; i < Integer.parseInt(map.getProperty("size")); i++) {
                            System.out.println("\t" + i + " - " + map.getProperty("" + i));
                        }
                        System.out.println("\t<< - Voltar");
                        id = map.getProperty(keyboardScanner.nextLine(), "0");
                        if (id.equals("0")) {
                            System.out.println("Opcao invalida!");
                            Timer = Calendar.getInstance();
                            Timer.add(Calendar.SECOND, 30);
                            continue;
                        } else if (id.equals("Voltar")) {
                            erro = 1;
                            break;
                        }

                        Timer = Calendar.getInstance();
                        Timer.add(Calendar.SECOND, 30);
                        while(true){
                            erro = 0;
                            try{
                                System.out.println(rs.consultarMesa(id));
                                keyboardScanner.nextLine();
                                erro = 1;
                            }catch (NotFoundException.MesaNF e){
                                System.out.println("Erro ao procurar a mesa!");
                                break;
                            } catch (Exception e){
                                rebind();
                                if (Timer.after(Calendar.getInstance())) continue;
                                erro = -1;
                                break;
                            }
                            break;
                        }
                        if(erro == 1){ // voltar a traz
                            Timer = Calendar.getInstance();
                            Timer.add(Calendar.SECOND, 30);
                            continue;
                        }
                        break;
                    }
                    if(erro==-1){
                        System.out.println("Conexao perdida!");
                        break;
                    }
                    break;
                    /*
                case "8":
                    try {
                        AccessToken();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                     */
                default:
                    System.out.println("Opção invalida!");
                    continue;
            }
        }
    }

    public static void main(String[] args) {

        try {
            AdminConsole console= new AdminConsole();
            console.config();
            //RmiServer s = (RmiServer) LocateRegistry.getRegistry("192.168.56.1",7000).lookup("server");
            RmiServer s = (RmiServer) LocateRegistry.getRegistry(console.RmiAddress,console.RmiPort).lookup("server");
            console.rs = s;
            console.rs.subscribe((RmiClient) console);

            console.interfaceConsola();

        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
        }
    }
}