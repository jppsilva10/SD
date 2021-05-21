package com.company;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;

class StatusLista {

    MulticastServerConsole mc;

    Map<String, String> lista = new HashMap<String, String>();  // Dicionario <id, status>

    synchronized boolean exist(String id) {
        return lista.containsKey(id);
    }

    synchronized void add(String id)
    {
        lista.put(id, "ready");
        mc.doNotify();
    }

    synchronized void put(String key, String value){ lista.put(key, value);}

    synchronized void change_status(String id, String new_status)
    {
        lista.replace(id, new_status);
        mc.doNotify();
    }

    synchronized String getStatus(String key){ return lista.get(key);}

    synchronized void remove(String id) {
        lista.remove(id);
    }

    synchronized void assignTerminal(String id, String nome)
    {
        lista.put("using " + id, nome);
        mc.doNotify();
    }

    synchronized void print_all(){
        for (Map.Entry<String, String> entry: lista.entrySet()){
            System.out.println(entry.getKey() + ":" +entry.getValue());
        }
    }

    synchronized String getNomeById(String id){
        for (Map.Entry<String, String> entry: lista.entrySet()){
            if(entry.getKey().equals("using "+id)){
                return entry.getValue();
            }
        }
        return null;
    }

    synchronized String procuraLivre(){   // Devolve o primeiro terminal livre que encontrar
        for (Map.Entry<String, String> entry: lista.entrySet()){
            if(entry.getValue().equals("ready")){
                return entry.getKey();
            }
        }
        return null;
    }

    synchronized Iterable<? extends Map.Entry<String, String>> entrySet() {
        return lista.entrySet();
    }
}


public class MulticastServer extends Thread {
    public String MULTICAST_ADDRESS;
    public int PORT;
    public String Departamento;
    String id;
    String RmiAddress;
    int RmiPort;
    RmiMesaImp rm;
    StatusLista statusList;
    Calendar Timer;

    public static Properties config() // ler o ficheiro de configuração
    {
        try {
            InputStream is = new FileInputStream("config.properties");
            Properties p = new Properties();
            p.load(is);
            return p;

        } catch (FileNotFoundException ex) {
            System.out.println("Erro a abrir ficheiro o de configuracao.");
        } catch (IOException ex) {
            System.out.println("Erro a ler ficheiro de configuracao.");
        }
        return null;
    }

    public void rebind() // reestabelecer ligacao com o servidor
    {
        try {
            Thread.sleep(500);
        }catch (Exception e){
        }
        synchronized (rm.rs) {
            try {
                rm.rs = (RmiServer) LocateRegistry.getRegistry(RmiAddress, RmiPort).lookup("server");
            } catch (Exception e) {
            }
        }
    }

    public void sendMessage(MulticastSocket socket, InetAddress group,String message)
    {
        try {
            socket.setSoTimeout(1000);
            byte[] buffer = message.getBytes();
            DatagramPacket protocol_packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(protocol_packet);
        }catch (Exception e){
        }
    }

    public String receiveMessage(MulticastSocket socket, InetAddress group,String message)
    {
        try {
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            return new String(packet.getData(), 0, packet.getLength());
        }catch (Exception e){

        }
        return null;
    }

    Properties GetHashMap(String str)
    {
        //System.out.println(str);
        String[] parameters = str.split(";");
        Properties m = new Properties();
        String id_oper[] = parameters[0].split("\\|");
        if(id_oper[0].equals("id") && id_oper[1].equals(id)){
            return null;
        }
        for(int i=0; i<parameters.length; i++){
            String[] item = parameters[i].split("\\|");
            m.put(item[0], item[1]);
        }
        return m;
    }

    public static void main(String[] args) {

        Scanner keyboardScanner = new Scanner(System.in);
        try {
            StatusLista statusLista = new StatusLista();                                    // Creates StatusList
            Properties p = config();
            String Id = "";
            while(true){
                System.out.println("Insira id: ");
                try{
                    Id = keyboardScanner.nextLine();
                    Integer.parseInt(Id);
                }catch (Exception e){
                    System.out.println("Valor invalido");
                    continue;
                }
                break;
            }
            MulticastServer server = new MulticastServer(statusLista, Id);
            MulticastServer2 server2 = new MulticastServer2(statusLista, server.id);
            MulticastServerConsole server3 = new MulticastServerConsole(statusLista, server.id);

            server.MULTICAST_ADDRESS = p.getProperty("Mesa" + server.id + "_MulticastAddress");
            server2.MULTICAST_ADDRESS = p.getProperty("Mesa" + server.id + "_MulticastAddress2");
            server3.MULTICAST_ADDRESS = server.MULTICAST_ADDRESS;
            server.PORT = Integer.parseInt(p.getProperty("Mesa" + server.id + "_Port"));
            server2.PORT = server.PORT;
            server3.PORT = server.PORT;
            server.Departamento = p.getProperty("Mesa" + server.id + "_Departamento");
            server.RmiAddress = p.getProperty("RmiAddress");
            server3.RmiAddress = server.RmiAddress;
            server.RmiPort = Integer.parseInt(p.getProperty("RmiPort"));
            server3.RmiPort = server.RmiPort;

            RmiServer s = (RmiServer) LocateRegistry.getRegistry(server.RmiAddress, server.RmiPort).lookup("server");
            //RmiServer s = (RmiServer) Naming.lookup("server");
            RmiMesaImp rm = new RmiMesaImp(s);
            server.rm = rm;
            server3.rm = rm;

            server.start();
            server2.start();
            server3.start();
            statusLista.mc = server3;


        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
        }
    }

    public MulticastServer(StatusLista targ, String mesa_id) {
        super("Server " + (long) (Math.random() * 1000));
        statusList = targ;
        this.id = mesa_id;
    }

    public void run() {

        //-------------- estabelecer ligação com o servidor RMI -----------------
        {
            int erro = 0;
            Timer = Calendar.getInstance();
            Timer.add(Calendar.SECOND, 30);
            while (true) {
                erro = 0;
                try {
                    this.rm.rs.createMesa("" + this.id, this.Departamento);
                } catch (DataConflictException e) {
                    System.out.println("ja existe uma mesa ativa com esse id!");
                    return;
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
                return;
            }

            while (true) {
                erro = 0;
                try {
                    this.rm.rs.subscribe("" + this.id, rm);
                } catch (NotFoundException e) {
                    System.out.println("Erro ao procurar a mesa!");
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
                return;
            }
        }
        //------- Comunicar com os terminais -------

        String protocol_message = "";
        MulticastSocket socket = null;
        Map<String, String> oper_lista = new HashMap<String, String>();
        int erro = 0;
        InetAddress group = null;

        try {
            group = InetAddress.getByName(MULTICAST_ADDRESS);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        try {
            socket = new MulticastSocket(PORT);                                 // create socket and bind it
            socket.joinGroup(group);                                                                // Receives ALL PACKETS

            while (true) {
                // RECEIVE DATA FROM CLIENTS
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                protocol_message = new String(packet.getData(), 0, packet.getLength());
                String protocol_opers[] = protocol_message.split(";");                       // Split protocol message
                String id_oper[] = protocol_opers[0].split("\\|");

                if(id_oper[0].equals("host")){                                                      // Message meant to server
                    String message = "";
                    String client_id = id_oper[1];
                    // Processes message
                    System.out.println(protocol_message);
                    for(int i=0; i<protocol_opers.length; i++){
                        String protocol_elements[] = protocol_opers[i].split("\\|");
                        oper_lista.put(protocol_elements[0], protocol_elements[1]);
                    }
                    for (Map.Entry<String, String> entry: oper_lista.entrySet()){
                        //System.out.println("Lista element.. " + entry.getKey() + " : " +entry.getValue());
                    }

                    if(oper_lista.get("type").equals("login")) {                                     // Receives login information and performs validity
                        String id_status = statusList.getStatus(client_id);
                        String username = oper_lista.get("username");                               // Get username from message
                        String password = oper_lista.get("password");                               // Get password from message

                        Timer = Calendar.getInstance();
                        Timer.add(Calendar.SECOND, 30);
                        while (true) {
                            erro = 0;
                            try {
                                if (rm.rs.login(username, password)) {                                      // Check login validity
                                    message = "id|" + oper_lista.get("host") + ";type|valid";
                                    statusList.change_status(client_id, "election");              // Update Status
                                }
                                else{
                                    message = "id|" + oper_lista.get("host") + ";type|invalid";
                                }
                            } catch (NotFoundException.PessoaNF e) {
                                message = "id|" + oper_lista.get("host") + ";type|invalid";
                                break;
                            }catch (RemoteException e) {
                                rebind();
                                if (Timer.after(Calendar.getInstance())) continue;
                                erro = -1;
                                break;
                            }catch (Exception e) {
                                message = "id|" + oper_lista.get("host") + ";type|invalid";
                                break;
                            }

                            break;
                        }
                        if(erro == -1){
                            message = "id|" + oper_lista.get("host") + ";type|erro;msg|Conexao perdida.";
                        }
                    }else if(oper_lista.get("type").equals("elections")){
                        Timer = Calendar.getInstance();
                        Timer.add(Calendar.SECOND, 30);
                        while (true) {
                            erro = 0;
                            try {
                                message = rm.rs.ListarEleicoesPorMesa(id);                              // Send election list
                                System.out.println(message);
                                message = "id|" + oper_lista.get("host") + ";type|elections;" + message;
                                statusList.change_status(id_oper[1], "list");
                            }catch (RemoteException e) {
                                System.out.println(e);
                                rebind();
                                if (Timer.after(Calendar.getInstance())) continue;
                                erro = -1;
                                break;
                            }catch (NotFoundException.MesaNF e) {
                                message = "id|" + oper_lista.get("host") + ";type|erro;msg|Erro ao procurar a Mesa.";
                                break;
                            }
                            break;
                        }
                        if(erro == -1){
                            message = "id|" + oper_lista.get("host") + ";type|erro;msg|Conexao perdida";
                        }
                    }else if(oper_lista.get("type").equals("lists")){
                        String election = oper_lista.get("vote");                                       // Gets election chosen
                        Timer = Calendar.getInstance();
                        Timer.add(Calendar.SECOND, 30);
                        while (true) {
                            erro = 0;
                            try {
                                message = rm.rs.listarListas(election);
                                message = "id|" + oper_lista.get("host") + ";type|lists;" + message;
                                statusList.change_status(id_oper[1], "voting");
                                statusList.put(statusList.getStatus("using "+ client_id), election);        // Entry for nome : election
                            }catch (RemoteException e) {
                                System.out.println(e);
                                rebind();
                                if (Timer.after(Calendar.getInstance())) continue;
                                erro = -1;
                                break;
                            }catch(NotFoundException.EleicaoNF eleicaoNF){
                                message = "id|" + oper_lista.get("host") + ";type|erro;msg|Eleicao nao encontrada.";
                                break;
                            }
                            break;
                        }
                        if(erro == -1){
                            message = "id|" + oper_lista.get("host") + ";type|erro;msg|Conexao perdida";
                        }
                    }else if(oper_lista.get("type").equals("vote")){                                    // Receives vote information and sends it to server
                        String vote = oper_lista.get("vote");
                        String username = oper_lista.get("username");
                        String election = oper_lista.get("election");
                        Timer = Calendar.getInstance();
                        Timer.add(Calendar.SECOND, 30);
                        while (true) {
                            erro = 0;
                            try {
                                rm.rs.addVoto(username, election, vote, id);                                // Adds new vote
                                message = "id|" + client_id + ";type|accepted";
                            }catch (RemoteException e) {
                                System.out.println(e);
                                rebind();
                                if (Timer.after(Calendar.getInstance())) continue;
                                erro = -1;
                                break;
                            } catch (DataConflictException e) {
                                message = "id|" + oper_lista.get("host") + ";type|erro;msg|Ja votou nessa eleicao";
                                break;
                            } catch (NotFoundException.EleicaoNF eleicaoNF) {
                                message = "id|" + oper_lista.get("host") + ";type|erro;msg|Erro ao procurar a eleicao";
                                break;
                            } catch (NotFoundException.PessoaNF pessoaNF) {
                                message = "id|" + oper_lista.get("host") + ";type|erro;msg|Erro ao procurar a pessoa";
                                break;
                            } catch (NotFoundException.MesaNF mesaNF) {
                                message = "id|" + oper_lista.get("host") + ";type|erro;msg|Erro ao procurar a mesa";
                                break;
                            } catch (TimeBoundsException.EleicaoAlreadyTerminated eleicaoAlreadyTerminated) {
                                message = "id|" + oper_lista.get("host") + ";type|erro;msg|A eleicao ja terminou";
                                break;
                            } catch (DataConflictException.InvalidType invalidType) {
                                message = "id|" + oper_lista.get("host") + ";type|erro;msg|Nao pode votar neste tipo de eleicao";
                                break;
                            } catch (NotFoundException.ListaNF listaNF) {
                                message = "id|" + oper_lista.get("host") + ";type|erro;msg|Erro ao procurar a lista";
                                break;
                            }
                            break;
                        }
                        if(erro == -1){message = "id|" + oper_lista.get("host") + ";type|erro;msg|Conexao perdida";}
                        statusList.change_status(oper_lista.get("host"), "ready");         // Resets terminal availability

                    }
                    System.out.println("MESSAGE TO " + oper_lista.get("host") + " : " + message);
                    byte[] buffer2 = message.getBytes();
                    DatagramPacket validation_packet = new DatagramPacket(buffer2, buffer2.length, group, PORT);
                    socket.send(validation_packet);
                }
                oper_lista.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            socket.close();
        }
    }
}



class MulticastServer2 extends Thread {
    public String MULTICAST_ADDRESS;
    public int PORT;
    String id;
    StatusLista statusList;

    public MulticastServer2(StatusLista targ, String mesa_id) {
        super("Server " + (long) (Math.random() * 1000));
        statusList = targ;
        this.id = mesa_id;
    }

    public void run() {
        MulticastSocket socket = null;
        //System.out.println(this.getName() + " running...");
        String id_name = "";
        try {
            socket = new MulticastSocket(PORT);  // create socket without binding it (only for sending)
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                id_name = new String(packet.getData(), 0, packet.getLength());
                statusList.add(id_name);
                System.out.println("New id connected: "+ id_name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}

class MulticastServerConsole extends Thread {
    String RmiAddress;
    int RmiPort;
    RmiMesaImp rm;
    StatusLista statusList;
    String id;
    Calendar Timer;
    public String MULTICAST_ADDRESS;
    public int PORT;

    public void rebind() // reestabelecer ligacao com o servidor
    {
        try {
            Thread.sleep(500);
        }catch (Exception e){
        }
        synchronized (rm.rs) {
            try {
                rm.rs = (RmiServer) LocateRegistry.getRegistry(RmiAddress, RmiPort).lookup("server");
            } catch (Exception e) {
            }
        }
    }

    public String requestAction(MulticastSocket socket, InetAddress group,String message) throws IOException
    {
        byte[] buffer = message.getBytes();
        byte[] buffer2 = new byte[256];
        int timeout_counter = 0;
        while (true) {
            try {
                DatagramPacket protocol_packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(protocol_packet);

                DatagramPacket packet = new DatagramPacket(buffer2, buffer2.length);
                socket.receive(packet);
                String str = new String(packet.getData(), 0, packet.getLength());
                String[] parameters = str.split(";");
                Properties m = new Properties();
                String id_oper[] = parameters[0].split("\\|");
                while(!id_oper[0].equals("host") || !id_oper[1].equals(id)){
                    socket.receive(packet);
                    str = new String(packet.getData(), 0, packet.getLength());
                    parameters = str.split(";");
                    m = new Properties();
                    id_oper = parameters[0].split("\\|");
                }
                return str;
            } catch (SocketTimeoutException e) {
                System.out.println("Timed out after 10 seconds.");
                timeout_counter++;
                if (timeout_counter == 4) {                   // Server down - Shutdown terminal
                    System.out.println("Conexao perdida!");
                    return null;
                }
            }
        }
    }


    public MulticastServerConsole(StatusLista targ, String mesa_id) {
        super("Mesa " + (long) (Math.random() * 1000));
        statusList = targ;
        this.id= mesa_id;
    }

    synchronized void doWait(){
        try {
            wait();
        }catch (Exception e){
            System.out.println(e);
        }
    }

    synchronized void doNotify(){
        try {
            notify();
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public void run() {
        int erro = 0;
        int timeout_time = 10;
        MulticastSocket socket = null;
        //System.out.println(this.getName() + " running...");
        Scanner keyboardScanner = new Scanner(System.in);


        InetAddress group = null;
        try {
            group = InetAddress.getByName(MULTICAST_ADDRESS);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }



        while (true) {
            System.out.println("Insira o seu nome: ");
            String nome = keyboardScanner.nextLine();
            //verificar se nome existe
            boolean atual = false;

            Timer = Calendar.getInstance();
            Timer.add(Calendar.SECOND, 30);
            while(true) {
                erro = 0;
                try {
                    atual = (rm.rs.findPessoaByName(nome) != null);
                } catch (Exception e) {
                    rebind();
                    if (Timer.after(Calendar.getInstance())) continue;
                    erro = -1;
                    break;
                }
                break;
            }
            if(erro==-1){
                System.out.println("Conexao perdida");
                continue;
            }

            if(atual==false){
                System.out.println("Nome não encontrado.");
                continue;
            }

            while (atual == true){

                String id_name = statusList.procuraLivre();     // Searches for an available terminal

                while(id_name == null){                         // Awaits for an available terminal
                    doWait();
                    id_name = statusList.procuraLivre();
                }
                // Changes terminal stauts
                try {
                    socket = new MulticastSocket(PORT);                                 // create socket and bind it
                    socket.joinGroup(group);
                    socket.setSoTimeout(1000*timeout_time);
                    String str = "id|"+id_name+";type|unlock";
                    while(true) {
                        String validation = requestAction(socket, group, str);
                        if (validation == null) {
                            //timeout
                            break;
                        }
                        String protocol_opers[] = validation.split(";");                       // Split protocol message
                        String id_oper[] = protocol_opers[0].split("\\|");
                        if (id_oper[1].equals(id_name)) {
                            System.out.println("Use o Terminal disponivel " + id_name);
                            atual = false;
                            break;
                        }
                    }

                }catch (Exception e){
                }
                statusList.change_status(id_name, "login");
            }
        }
    }
}