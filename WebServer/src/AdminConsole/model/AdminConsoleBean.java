package AdminConsole.model;

import com.company.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;



public class AdminConsoleBean {
    String RmiAddress;
    int RmiPort;
    Calendar Timer;
    AdminConsole console;
    private String username;
    private String titulo;
    private String userDetails;
    private String electionDetails;

    public String getTitulo(){
        return titulo;
    }

    public String setUserDetails(String username) {
        this.username = username;
        return "success";
    }

    public String setElectionDetails(String title) {
        titulo = title;
        return "success";
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private List<String> dayList;
    private String day;

    private void setDayList(){
        dayList = new ArrayList<String>();
        for(int i=0; i<0; i++){
            dayList.add(""+i);
        }
    }

    private void setDay(String day){
        this.day = day;
    }

    public List<String> getDayList() {
        dayList = new ArrayList<String>();
        for(int i=0; i<0; i++){
            dayList.add(""+i);
        }
        return dayList;
    }

    public boolean getTest(){
        Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                console.rs.getVoto("", "");
                break;
            } catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                return false;
            }
        }
        return true;
    }


    public void config() // ler o ficheiro de configuração
    {
        /*
        try {
            InputStream is = new FileInputStream("src\\config.properties");
            Properties p = new Properties();
            p.load(is);
            RmiAddress = p.getProperty("RmiAddress");
            RmiPort = Integer.parseInt(p.getProperty("RmiPort"));
        } catch (FileNotFoundException ex) {
            System.out.println("Erro a abrir ficheiro de configuracao.");
        } catch (IOException ex) {
            System.out.println("Erro a ler ficheiro de configuracao.");
        }
         */
        RmiAddress = "192.168.56.1";
        RmiPort = 5000;


    }

    Properties GetHashMap(String str)
    {
        String[] parameters = str.split(";");
        Properties m = new Properties();
        for(int i=0; i<parameters.length; i++){
            String[] item = parameters[i].split("\\|");
            m.put(item[0], item[1]);
        }
        return m;
    }

    public AdminConsoleBean() throws RemoteException, NotBoundException {
            console = new AdminConsole();
            this.config();
            console.rs = (RmiServer) LocateRegistry.getRegistry(this.RmiAddress,this.RmiPort).lookup("server");
            console.rs.subscribe((RmiClient) console);
    }

    public String createPessoa(String tipo, String nome, String username, String password, String departamento, String contacto, String morada, String numero_CC, Calendar validade_CC){
        Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                console.rs.createPessoa(tipo, nome, username, password, departamento, contacto, morada, numero_CC, validade_CC);
                break;
            } catch (DataConflictException.DuplicatedNumero_CC duplicatedNumero_cc) {
                return "";
            } catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                return "";
            } catch (DataConflictException.DuplicatedUsername duplicatedUsername) {
                return "";
            }
        }
        return "success";
    }
    public ArrayList<String> getUsersList(){

        ArrayList<String> list = new ArrayList<String>();

        Properties map = null;

        Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                map = GetHashMap(console.rs.listarPessoas());
                break;
            } catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                return list;
            }
        }

        for (int i = 1; i < Integer.parseInt(map.getProperty("size")); i++) {
            list.add(map.getProperty("" + i));
        }
        return list;
    }
    public String getUserDetails(){
        Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                userDetails = console.rs.consultarPessoa(username);
                break;
            } catch (NotFoundException.PessoaNF pessoaNF) {
                return null;
            } catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                return null;
            }
        }
        return userDetails;
    }

    public ArrayList<String> getElectionsList(){

        ArrayList<String> list = new ArrayList<String>();

        Properties map = null;

        Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                map = GetHashMap(console.rs.listarEleicoes());
                break;
            } catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                return list;
            }
        }

        for (int i = 1; i < Integer.parseInt(map.getProperty("size")); i++) {
            list.add(map.getProperty("" + i));
        }
        return list;
    }
    public String[] getElectionDetails(){
        Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                this.electionDetails = console.rs.consultarEleicao(titulo);
                break;
            } catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                return null;
            } catch (NotFoundException.EleicaoNF e){
                return null;
            }
        }

        String[] details = electionDetails.split("\n");
        return details;
    }

    public ArrayList<String> getDayslist(){
        ArrayList<String> list = new ArrayList<String>();
        for(int i=1; i<32; i++){
            list.add("" + i);
        }
        return list;
    }

}
