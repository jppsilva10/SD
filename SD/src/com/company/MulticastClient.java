package com.company;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * The MulticastClient class joins a multicast group and loops receiving
 * messages from that group. The client also runs a MulticastUser thread that
 * loops reading a string from the keyboard and multicasting it to the group.
 * <p>
 * The example IPv4 address chosen may require you to use a VM option to
 * prefer IPv4 (if your operating system uses IPv6 sockets by default).
 * <p>
 * Usage: java -Djava.net.preferIPv4Stack=true MulticastClient
 *
 * @author Raul Barbosa
 * @version 1.0
 */
public class MulticastClient extends Thread {
    String MULTICAST_ADDRESS;
    int PORT;
    String id;

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

    public MulticastClient(String id) {
        super("User " + (long) (Math.random() * 1000));
        this.id = id;
    }

    public static void main(String[] args) {
        Scanner keyboardScanner = new Scanner(System.in);
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
        MulticastClient client = new MulticastClient(Id);
        System.out.println(p.getProperty("Terminal" + client.id + "_mesa") + "_Port");
        client.MULTICAST_ADDRESS = p.getProperty(p.getProperty("Terminal" + client.id + "_mesa") + "_MulticastAddress2");
        client.PORT = Integer.parseInt(p.getProperty(p.getProperty("Terminal" + client.id + "_mesa") + "_Port"));
        MulticastUser user = new MulticastUser(client.id);
        user.MULTICAST_ADDRESS = p.getProperty(p.getProperty("Terminal" + client.id + "_mesa") + "_MulticastAddress");
        user.PORT = Integer.parseInt(p.getProperty(p.getProperty("Terminal" + client.id + "_mesa") + "_Port"));
        client.start();
        user.start();
    }

    public void run() {     // Receives data from multicastServer
        System.out.println(this.getName() + " ready...");
        MulticastSocket socket = null;
        boolean connected = true;
        String id_name;
        try {
            socket = new MulticastSocket();                             // create socket and bind it
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

            while (connected) {
                id_name = "" + id;
                byte[] buffer = id_name.getBytes();
                DatagramPacket id_packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(id_packet);
                connected = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
            System.out.println("Exited connection thread");
        }
    }
}

class MulticastUser extends Thread {
    String MULTICAST_ADDRESS;
    int PORT;
    String id;

    public String requestAction(MulticastSocket socket, InetAddress group,String message) throws IOException
    {
        byte[] buffer = message.getBytes();
        byte[] buffer2 = new byte[256];
        int timeout_counter = 0;
        int count =0;
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
                while(!id_oper[0].equals("id") || !id_oper[1].equals(id)){
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

    public void sendMessage(MulticastSocket socket, InetAddress group, String message) throws IOException
    {
            byte[] buffer = message.getBytes();
            DatagramPacket protocol_packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(protocol_packet);
    }

    public String receiveMessage(MulticastSocket socket, InetAddress group) throws IOException
    {
        int timeout_counter = 0;
        int count= 0;
        while(true) {
            try {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String str = new String(packet.getData(), 0, packet.getLength());
                String[] parameters = str.split(";");
                Properties m = new Properties();
                String id_oper[] = parameters[0].split("\\|");
                while(!id_oper[0].equals("id") || !id_oper[1].equals(id)){
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

    Properties GetHashMap(String str)
    {
        //System.out.println(str);
        String[] parameters = str.split(";");
        Properties m = new Properties();
        String id_oper[] = parameters[0].split("\\|");
        if(!id_oper[0].equals("id") || !id_oper[1].equals(id)){
            return null;
        }
        for(int i=0; i<parameters.length; i++){
            String[] item = parameters[i].split("\\|");
            m.put(item[0], item[1]);
        }
        return m;
    }

    public MulticastUser(String id) {
        super("User " + (long) (Math.random() * 1000));
        this.id = id;
    }

    public void run() {        // Sends data to multicastServer
        MulticastSocket socket = null;
        //System.out.println(this.getName() + " MulticastUser ready...");
        // Vars
        String electionVote = "";
        String vote = "";
        String username = "";
        String keyboard = "";
        String password = "";
        //STATES
        boolean unlocked = false;
        boolean connected = true;
        boolean logged = false;
        boolean elections_listed = false;
        boolean election_chosen = false;
        boolean lists_listed = false;
        boolean loggedin = false;
        int timeout_counter = 0;
        int timeout_time = 10;                  // In seconds

        Properties lista = null;

        while(connected) {
            int erro = 0;
            try {
                socket = new MulticastSocket(PORT);                     // create socket without binding it (only for sending)
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);
                socket.setSoTimeout(1000*timeout_time); // DISCONECTAR AFTER 5 SEGUNDOS
                Scanner keyboardScanner = new Scanner(System.in);
                //Map<String, String> lista = new HashMap<String, String>();


                String protocol_message = "";

                while(!unlocked){

                    String validation_answer = receiveMessage(socket, group);
                    if(validation_answer==null){
                        //timeout
                        continue;
                    }

                    lista = GetHashMap(validation_answer);
                    if(lista==null){
                        continue;
                    }

                    protocol_message = "host|" + id + ";type|unlocked";
                    if(lista.getProperty("type").equals("unlock")){
                        sendMessage(socket, group, protocol_message);
                        unlocked = true;
                    }
                }

                // Performs login and sends data to multicast server - Enters logging state
                while (!logged) {
                    if(loggedin== false){
                        loggedin= true;
                        System.out.println("Insira unsername:");            // Include try..catch
                        username = keyboardScanner.nextLine();
                        System.out.println("Insira password");
                        password = keyboardScanner.nextLine();
                    }
                    protocol_message = "host|" + id + ";type|login;username|" + username + ";password|" + password;
                    //System.out.println("MESSAGE TO HOST : " + protocol_message);
                    String validation_answer = requestAction(socket, group, protocol_message);
                    if(validation_answer==null){
                        // timeout
                        connected = false;
                        erro = 1;
                        break;
                    }
                    lista = GetHashMap(validation_answer);
                    if(lista==null){
                        continue;
                    }
                    if (lista.getProperty("type").equals("valid")) {                                    // Login succeeded
                        logged = true;
                        loggedin = true;
                        break;
                    }
                    else if(lista.getProperty("type").equals("erro")){
                        System.out.println(lista.getProperty("msg"));
                        erro = 1;
                        break;
                    }
                }

                if(erro == 1){
                    logged = false;
                    elections_listed = false;
                    election_chosen = false;
                    lists_listed = false;
                    loggedin = false;
                    continue;
                }


                if(!elections_listed){
                    protocol_message = "host|" + id + ";type|elections";
                    //System.out.println("MESSAGE TO HOST : " + keyboard);

                    while(true){
                        String validation_answer = requestAction(socket, group, protocol_message);
                        if( validation_answer == null){
                           // timeout
                            connected = false;
                            erro = 1;
                            break;
                        }
                        lista = GetHashMap(validation_answer);
                        if (lista == null){
                            continue;
                        }

                        Set<String> keys = lista.stringPropertyNames();
                        for (String key : keys) {
                            //System.out.println(key + " : " + lista.getProperty(key));
                        }
                        if(lista.getProperty("type").equals("elections")){
                            elections_listed = true;
                            break;
                        }
                        else if(lista.getProperty("type").equals("erro")){
                            System.out.println(lista.getProperty("msg"));
                            // BACK TO PREVIOUS STATE
                            erro = 1;
                            break;
                        }
                    }
                }

                if(erro == 1){
                    logged = false;
                    elections_listed = false;
                    election_chosen = false;
                    lists_listed = false;
                    loggedin = false;
                    continue;
                }

                // Choosing Election - Shows Available Election on Screen
                if(!election_chosen){
                    while(true){
                        System.out.println("Eleicoes:");
                        for (int i = 1; i < Integer.parseInt(lista.getProperty("size")); i++) {
                            System.out.println(i + " - " + lista.getProperty(Integer.toString(i)));
                        }
                        keyboard = keyboardScanner.nextLine();
                        if(lista.getProperty(keyboard) != null){
                            break;
                        }else{
                            System.out.println("Opcao invalida.");
                        }
                    }
                    electionVote = lista.getProperty(keyboard);
                    protocol_message = "host|" + id + ";type|lists;vote|" + electionVote;
                    while(true) {
                        String validation_answer = requestAction(socket, group, protocol_message);
                        if( validation_answer == null){
                            // timeout
                            connected = false;
                            erro = 1;
                            break;
                        }
                        lista = GetHashMap(validation_answer);
                        if(lista == null){
                            continue;
                        }
                        if(lista.getProperty("type").equals("lists")){
                            election_chosen = true;
                            break;
                        }
                        else if(lista.getProperty("type").equals("erro")){
                            System.out.println(lista.getProperty("msg"));
                            // BACK TO PREVIOUS STATE
                            erro = 1;
                            break;
                        }
                    }
                }
                // Enters choosing_election state

                if(erro == 1){
                    logged = false;
                    elections_listed = false;
                    election_chosen = false;
                    lists_listed = false;
                    loggedin = false;
                    continue;
                }

                // Choosing Candidate - Shows Candidate List on  Screen
                if(!lists_listed) {
                    while(true){
                        System.out.println("Listas:");
                        for (int i = 1; i < Integer.parseInt(lista.getProperty("size")); i++) {
                            System.out.println(i + " - " + lista.getProperty(Integer.toString(i)));
                        }
                        keyboard = keyboardScanner.nextLine();
                        if(lista.getProperty(keyboard) != null){
                            break;
                        }else{
                            System.out.println("Opcao invalida.");
                        }
                    }
                    vote = lista.getProperty(keyboard);
                    protocol_message = "host|" + id + ";type|vote;vote|" + vote + ";election|" + electionVote+ ";username|"+ username;

                    // Enters voting state
                    while(true){
                        String validation_answer = requestAction(socket, group, protocol_message);
                        if( validation_answer == null){
                            // timeout
                            connected = false;
                            erro = 1;
                            break;

                        }
                        lista = GetHashMap(validation_answer);
                        if(lista == null){
                            continue;
                        }
                        if(lista.getProperty("type").equals("accepted")){
                            System.out.println("Voto efetuado com sucesso!");
                            lists_listed = true;
                            break;
                        }
                        else if(lista.getProperty("type").equals("erro")){
                            System.out.println(lista.getProperty("msg"));
                            // BACK TO PREVIOUS STATE
                            erro = 1;
                            break;
                        }
                    }
                }

                //if(erro == 1){choosing_election= false; continue;}
                // Reset States and Loop. Waits for another user to start again
                logged = false;
                elections_listed = false;
                election_chosen = false;
                lists_listed = false;
                loggedin = false;
                continue;

            }catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        }
    }

    /*
    public void run() {        // Sends data to multicastServer
        MulticastSocket socket = null;
        //System.out.println(this.getName() + " MulticastUser ready...");
        // Vars
        String electionVote = "";
        String vote = "";
        String username = "";
        String keyboard = "";
        String password = "";
        //STATES
        boolean bloqueado = true;
        boolean connected = true;
        boolean logged = false;
        boolean login_validation;
        boolean choosing_election = false;
        boolean choosing_election_validation = false;
        boolean voting = false;
        boolean loggedin = false;
        boolean listar = false;
        int timeout_counter = 0;
        int timeout_time = 10;                  // In seconds

        while(connected) {
            int erro = 0;
            try {
                socket = new MulticastSocket(PORT);                     // create socket without binding it (only for sending)
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);
                socket.setSoTimeout(1000*timeout_time); // DISCONECTAR AFTER 5 SEGUNDOS
                Scanner keyboardScanner = new Scanner(System.in);
                //Map<String, String> lista = new HashMap<String, String>();
                Properties lista = new Properties();

                String protocol_message = "";

                while(bloqueado){
                    lista.clear();                                                                          // Clears list
                    byte[] buffer3 = new byte[256];
                    DatagramPacket packet_answer = new DatagramPacket(buffer3, buffer3.length);
                    try{
                        socket.receive(packet_answer);
                    } catch (SocketTimeoutException e){
                    }
                    String validation_answer = new String(packet_answer.getData(), 0, packet_answer.getLength());
                    String protocol_opers[] = validation_answer.split(";");
                    String id_oper[] = protocol_opers[0].split("\\|");
                    if(id_oper[0].equals("id") && id_oper[1].equals(id)){
                        bloqueado = false;
                    }
                }

                // Performs login and sends data to multicast server - Enters logging state
                while (!logged) {
                    login_validation = false;
                    if(loggedin== false){
                        System.out.println("Insira unsername:");            // Include try..catch
                        username = keyboardScanner.nextLine();
                        System.out.println("Insira password");
                        password = keyboardScanner.nextLine();
                    }
                    protocol_message = "host|" + id + ";type|login;username|" + username + ";password|" + password;
                    //System.out.println("MESSAGE TO HOST : " + protocol_message);
                    byte[] buffer2 = protocol_message.getBytes();
                    DatagramPacket protocol_packet = new DatagramPacket(buffer2, buffer2.length, group, PORT);
                    socket.send(protocol_packet);

                    // Enters login validation state
                    while (!login_validation) {
                        lista.clear();                                                                          // Clears list
                        byte[] buffer3 = new byte[256];
                        DatagramPacket packet_answer = new DatagramPacket(buffer3, buffer3.length);
                        socket.receive(packet_answer);                                                         // Waits for packet
                        String validation_answer = new String(packet_answer.getData(), 0, packet_answer.getLength());
                        String protocol_opers[] = validation_answer.split(";");
                        String id_oper[] = protocol_opers[0].split("\\|");
                        // If message is meant for the client
                        if (id_oper[0].equals("id") && id_oper[1].equals(id)) {
                            timeout_counter = 0;                                                                   // Reset timeoutcounter
                            // Processes Message
                            for (int i = 0; i < protocol_opers.length; i++) {
                                String protocol_elements[] = protocol_opers[i].split("\\|");
                                lista.setProperty(protocol_elements[0], protocol_elements[1]);
                            }

                            Set<String> keys = lista.stringPropertyNames();
                            for (String key : keys) {
                                //System.out.println(key + " : " + lista.getProperty(key));
                            }
                            try{
                                if (lista.getProperty("type").equals("valid")) {                                    // Login succeeded
                                    logged = true;
                                    loggedin = true;
                                }
                                else if(lista.getProperty("type").equals("erro")){
                                    System.out.println(lista.getProperty("msg"));
                                    return;
                                }
                                else if (lista.getProperty("type").equals("end")) {                               // Maximum number of retries reached
                                    //..disconnect
                                    System.out.println("Nome não existe.");
                                    logged = true;
                                    choosing_election = true;
                                    voting = true;
                                    listar = false;

                                }
                            }catch(NullPointerException e){
                                System.out.println("Erro ao validar conta. Conta nao existe.");
                                break;
                            }
                            login_validation = true;
                        } // Else waits again for validation answer
                        //socket.send(protocol_packet);                                                     // RESEND
                    }
                }

                if(erro == 1){
                    logged = false;
                    choosing_election = false;
                    choosing_election_validation = false;
                    voting = false;
                    loggedin = false;
                    listar = false;
                    continue;
                }
                /*
                if(listar){
                    keyboard = "host|" + id + ";type|list_election";
                    //System.out.println("MESSAGE TO HOST : " + keyboard);
                    byte[] buffer = keyboard.getBytes();
                    DatagramPacket vote_packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                    socket.send(vote_packet);
                    while(true){
                        byte[] buffer3 = new byte[256];
                        DatagramPacket packet_answer = new DatagramPacket(buffer3, buffer3.length);
                        socket.receive(packet_answer);
                        String validation_answer = new String(packet_answer.getData(), 0, packet_answer.getLength());
                        String protocol_opers[] = validation_answer.split(";");
                        String id_oper[] = protocol_opers[0].split("\\|");
                        if (id_oper[0].equals("id") && id_oper[1].equals(id)) {
                            lista.clear();
                            for (int i = 0; i < protocol_opers.length; i++) {
                                String protocol_elements[] = protocol_opers[i].split("\\|");
                                lista.setProperty(protocol_elements[0], protocol_elements[1]);
                            }
                            Set<String> keys = lista.stringPropertyNames();
                            for (String key : keys) {
                                //System.out.println(key + " : " + lista.getProperty(key));
                            }
                            if(lista.getProperty("type").equals("erro")){
                                System.out.println("Erro: "+lista.getProperty("erro"));
                                // BACK TO PREVIOUS STATE
                                erro = 1;
                            }
                            break;
                        }
                    }
                }
                if(erro == 1){
                    logged = false;
                    choosing_election = false;
                    choosing_election_validation = false;
                    voting = false;
                    loggedin = false;
                    listar = false;
                    continue;
                }
                // Choosing Election - Shows Available Election on Screen
                if(timeout_counter ==0 && choosing_election == false){
                    System.out.println("Eleicoes:");
                    for (int i = 1; i < Integer.parseInt(lista.getProperty("size")); i++) {
                        System.out.println(i + " - " + lista.getProperty(Integer.toString(i)));
                    }
                    boolean election_input = false;
                    while(!election_input){
                        keyboard = keyboardScanner.nextLine();
                        if(lista.getProperty(keyboard) != null){
                            election_input = true;
                        }else{
                            System.out.println("Opcao invalida.");
                        }
                    }
                    electionVote = lista.getProperty(keyboard);
                }
                // Enters choosing_election state
                while (!choosing_election) {
                    if (electionVote != "") {
                        keyboard = "host|" + id + ";type|election;vote|" + electionVote;
                        //System.out.println("MESSAGE TO HOST : " + keyboard);
                        byte[] buffer = keyboard.getBytes();
                        DatagramPacket vote_packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                        socket.send(vote_packet);
                        choosing_election = true;
                        while (!choosing_election_validation) {
                            byte[] buffer2 = new byte[256];
                            DatagramPacket packet_answer = new DatagramPacket(buffer2, buffer2.length);
                            socket.receive(packet_answer);
                            String validation_answer = new String(packet_answer.getData(), 0, packet_answer.getLength());
                            String protocol_opers[] = validation_answer.split(";");
                            String id_oper[] = protocol_opers[0].split("\\|");
                            if (id_oper[0].equals("id") && id_oper[1].equals(id)) {
                                timeout_counter = 0;                                                                   // Reset timeoutcounter
                                //System.out.println("PACKET RECEIVED");
                                lista.clear();
                                for (int i = 0; i < protocol_opers.length; i++) {
                                    String protocol_elements[] = protocol_opers[i].split("\\|");
                                    lista.setProperty(protocol_elements[0], protocol_elements[1]);
                                }
                                Set<String> keys = lista.stringPropertyNames();
                                for (String key : keys) {
                                    //System.out.println(key + " : " + lista.getProperty(key));
                                }
                                if(lista.getProperty("type").equals("erro")){
                                    System.out.println(lista.getProperty("msg"));
                                    // BACK TO PREVIOUS STATE
                                    erro = 1;
                                }

                                choosing_election_validation = true;
                            }
                            //socket.send(vote_packet);                                               // RESEND
                        }
                    } else {
                        System.out.println("Ocorreu um erro com a escolha da eleicao.");
                    }
                }

                if(erro == 1){
                    logged = false;
                    choosing_election = false;
                    choosing_election_validation = false;
                    voting = false;
                    loggedin = false;
                    listar = false;
                    continue;
                }

                listar = false;

                // Choosing Candidate - Shows Candidate List on  Screen
                if(timeout_counter == 0 && voting == false) {
                    System.out.println("Listas:");
                    for (int i = 1; i < Integer.parseInt(lista.getProperty("size")); i++) {
                        System.out.println(i + " - " + lista.getProperty(Integer.toString(i)));
                    }
                    boolean vote_input = false;
                    while(!vote_input){
                        keyboard = keyboardScanner.nextLine();
                        if(lista.getProperty(keyboard) != null){
                            vote_input = true;
                        }else{System.out.println("Opcao invalida.");}

                    }
                    vote = lista.getProperty(keyboard);
                }
                // Enters voting state
                while (!voting) {
                    choosing_election_validation=false;
                    if (vote != null) {
                        vote = "host|" + id + ";type|vote;vote|" + vote+ ";election|" + electionVote+ ";username|"+ username;
                        //System.out.println("MESSAGE TO HOST : " + vote);
                        byte[] buffer = vote.getBytes();
                        DatagramPacket vote_packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                        socket.send(vote_packet);
                        voting = true;
                        while (!choosing_election_validation) {
                            byte[] buffer2 = new byte[256];
                            DatagramPacket packet_answer = new DatagramPacket(buffer2, buffer2.length);
                            socket.receive(packet_answer);
                            String validation_answer = new String(packet_answer.getData(), 0, packet_answer.getLength());
                            String protocol_opers[] = validation_answer.split(";");
                            String id_oper[] = protocol_opers[0].split("\\|");
                            if (id_oper[0].equals("id") && id_oper[1].equals(id)) {
                                timeout_counter = 0;                                                                   // Reset timeoutcounter
                                lista.clear();
                                for (int i = 0; i < protocol_opers.length; i++) {
                                    String protocol_elements[] = protocol_opers[i].split("\\|");               // Process Message
                                    lista.setProperty(protocol_elements[0], protocol_elements[1]);
                                }
                                Set<String> keys = lista.stringPropertyNames();
                                for (String key : keys) {
                                    //System.out.println(key + " : " + lista.getProperty(key));
                                }
                                if (lista.getProperty("type").equals("valid")) {
                                    System.out.println("Voto efetuado com sucesso!");
                                }else if(lista.getProperty("type").equals("erro")){
                                    System.out.println(lista.getProperty("msg"));
                                    // BACK TO PREVIOUS STATE
                                    erro = 1;

                                }
                                choosing_election_validation = true;
                                break;
                            }
                            //socket.send(vote_packet);
                        }
                    } else {
                        System.out.println("Ocorreu um erro na escolha do voto.");
                    }
                    break;
                }
                //if(erro == 1){choosing_election= false; continue;}
                // Reset States and Loop. Waits for another user to start again
                logged = false;
                choosing_election = false;
                choosing_election_validation = false;
                voting = false;
                loggedin = false;
                listar = false;

            } catch (SocketTimeoutException e){
                System.out.println("Timed out after 10 seconds.");
                timeout_counter++;
                if(timeout_counter == 4){                   // Server down - Shutdown terminal
                    System.out.println("Conexao perdida!");
                    connected = false;
                }
            }catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        }
    }

     */
}
