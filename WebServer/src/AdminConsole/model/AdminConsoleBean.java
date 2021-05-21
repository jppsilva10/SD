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
import java.util.*;


public class AdminConsoleBean {
    String RmiAddress;
    int RmiPort;
    Calendar Timer;
    AdminConsole console;
    private String username;
    private String titulo;
    private String userDetails;
    private String electionDetails;
    private String descricao;
    private String tipo;
    private Calendar inicio;
    private Calendar fim;
    private String lista;

    //----------------------------------Set Up-----------------------------------------

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

    public AdminConsoleBean() throws RemoteException, NotBoundException
    {
        console = new AdminConsole();
        this.config();
        console.rs = (RmiServer) LocateRegistry.getRegistry(this.RmiAddress,this.RmiPort).lookup("server");
        console.rs.subscribe((RmiClient) console);
    }

    //--------------------------------------------------------------------------------------------

    //---------------------------------------User Details----------------------------------------------
    public String setUserDetails(String username)
    {
        this.username = username;
        return "success";
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public ArrayList<String> getUsersList()
    {

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

    public String getUserDetails()
    {
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
    //-------------------------------------------------------------------------------------------

    //--------------------------------------Election Details------------------------------------

    public String getTitulo(){
        return titulo;
    }

    public String getElectionDescricao()
    {
 	return descricao;
    }

    public String getElectionTipo()
    {
 	return tipo;
    }

    public String setElectionDetails(String title)
    {
        titulo = title;
        return "success";
    }

    public String setListaTitulo(String title){
       	lista = title;
	    return "success";
    }

    public String setElectionDetails2()
    {
        String[] details = electionDetails.split("\n");
        String[] date_details;

        this.descricao = details[1].split(": ")[1];
        this.tipo = details[2].split(": ")[1];
        int i = 4;
        for(; details[i].split(": ").length==1; i++){}

        date_details = details[i].split(": ")[1].split("/"); // [dd/mm/yyy]  para  [dd, mm, yy]
        this.inicio = Calendar.getInstance();
        this.inicio.set(Integer.parseInt(date_details[2]), Integer.parseInt(date_details[1]), Integer.parseInt(date_details[0]), 0, 0 ,0);

        i++;
        date_details = details[i].split(": ")[1].split("/");
        this.fim = Calendar.getInstance();
        this.fim.set(Integer.parseInt(date_details[2]), Integer.parseInt(date_details[1]), Integer.parseInt(date_details[0]),0, 0 ,0);

        return "success";
    }

    public ArrayList<String> getElectionsList()
    {

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

    public String[] getElectionDetails()
    {
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

    public String createElection(String titulo, String descricao, String tipo, Calendar inicio, Calendar fim)
    {
	Timer = Calendar.getInstance();
	Timer.add(Calendar.SECOND, 30);
	
	while(true) {
            try {
                console.rs.createEleicao(titulo, descricao, inicio, fim, tipo);
                break;
            } catch (DataConflictException e) {
                return "";
            } catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                return "";
            }
        }
        return "success";
   }

   public String editEleicao(String titulo, String descricao, Calendar inicio, Calendar fim, String tipo, String tituloAnterior)
   {
       Timer = Calendar.getInstance();
       Timer.add(Calendar.SECOND, 30);

       while(true) {
           try {
               console.rs.editEleicao(titulo, descricao, inicio, fim, tipo, tituloAnterior);
               break;
           } catch (DataConflictException e) {
               return "";
           } catch (RemoteException e) {
               console.rebind();
               if (Timer.after(Calendar.getInstance())) continue;
               return "";
           } catch (TimeBoundsException.EleicaoAlreadyStarted eleicaoAlreadyStarted) {
               eleicaoAlreadyStarted.printStackTrace();
           } catch (NotFoundException.EleicaoNF eleicaoNF) {
               eleicaoNF.printStackTrace();
           }
       }
       return "success";

   }

   public ArrayList<String> getListasList()
   {

	ArrayList<String> list = new ArrayList<String>();

        Properties map = null;

	Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                map = GetHashMap(console.rs.listarListas(titulo));
                break;
            } catch (RemoteException | NotFoundException.EleicaoNF e) {
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

    public ArrayList<String> getPessoasList(){
	ArrayList<String> list = new ArrayList<String>();

        Properties map = null;

	Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                map = GetHashMap(console.rs.listarPessoaPorLista(titulo, lista));
                break;
            } catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                return list;
            } catch (NotFoundException.ListaNF listaNF) {
                listaNF.printStackTrace();
            } catch (NotFoundException.EleicaoNF eleicaoNF) {
                eleicaoNF.printStackTrace();
            }
        }

	    for (int i = 1; i < Integer.parseInt(map.getProperty("size")); i++) {
                list.add(map.getProperty("" + i));
            }
	    return list;
    }


    public String novaLista(String novoTitulo)
    {

        Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                console.rs.addLista(titulo, novoTitulo);
                break;
            } catch (RemoteException | NotFoundException.EleicaoNF e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                return "";
            } catch (DataConflictException e) {
                e.printStackTrace();
            } catch (TimeBoundsException.EleicaoAlreadyStarted eleicaoAlreadyStarted) {
                eleicaoAlreadyStarted.printStackTrace();
            }
        }

        return "success";
    }
    //----------------------------------------------------------------------------------

    //-----------------------------Regist User--------------------------------------------

    public String createPessoa(String tipo, String nome, String username, String password, String departamento, String contacto, String morada, String numero_CC, Calendar validade_CC)
    {
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
    //------------------------------------------------------------------------------

    public boolean getTest()
    {
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

}
