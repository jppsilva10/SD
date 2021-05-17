package AdminConsole.model;

import com.company.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.AccessTokenRequestParams;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class UserBean implements UserInterface {
    String RmiAddress;
    int RmiPort;
    Calendar Timer;
    AdminConsole console;
    private String username;
    private String password;
    private String titulo;
    private String electionPage;
    private String userDetails;
    private String electionDetails;

    private static final String NETWORK_NAME = "Facebook";
    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/v3.2/me";

    private final String appKey = "463509174943899";
    private final String appSecret = "22b58424ddda756bdd4faa0baed6dee3";
    private String secretState;
    private String authorizationUrl;
    private OAuth20Service service;

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    //------------------------------Set Up--------------------------------------
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

    public UserBean() throws RemoteException, NotBoundException
    {
        console = new AdminConsole();
        this.config();
        console.rs = (RmiServer) LocateRegistry.getRegistry(this.RmiAddress,this.RmiPort).lookup("server");
        console.rs.subscribe((RmiClient) console);
        console.setUser((UserInterface) this);

    }
    //-----------------------------------------------------------------

    //-------------------Log In------------------------

    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean validate(String username, String password) throws RemoteException, NotFoundException.PessoaNF
    {
        Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                return console.rs.login(username, password);
            } catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                throw new RemoteException();
            }
        }
    }

    public boolean getLogin(){

        if(username!=null && password!=null){
            Timer = Calendar.getInstance();
            Timer.add(Calendar.SECOND, 30);
            while(true) {
                try {
                    return console.rs.login(username, password);
                } catch (RemoteException e) {
                    console.rebind();
                    if (Timer.after(Calendar.getInstance())) continue;
                    return false;
                }catch (NotFoundException.PessoaNF e){
                    username = null;
                    username = null;
                    return false;
                }
            }
        }

        String str = "";

        service = getService();

        Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                str = console.rs.facebookLogin();
                break;
            } catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                return false;
            }
        }

        Properties p = GetHashMap(str);
        for(int i=1; i<Integer.parseInt(p.getProperty("size")); i++){
            String[] info = p.getProperty(""+i).split(",");

            OAuth2AccessToken accessToken = null;
            Timer = Calendar.getInstance();
            Timer.add(Calendar.SECOND, 30);
            while(true) {
                try {
                    accessToken = console.rs.getAccessToken(info[0]);
                    break;
                } catch (NotFoundException.PessoaNF pessoaNF) {
                    break;
                } catch (RemoteException e) {
                    console.rebind();
                    if (Timer.after(Calendar.getInstance())) continue;
                    return false;
                }
            }

            if(accessToken==null) continue;

            final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
            service.signRequest(accessToken, request);
            try (Response response = service.execute(request)) {
                System.out.println(response.getCode());
                System.out.println(response.getBody());
                if(response.getCode()>300) continue;
                username = info[0];
                password = info[1];
                return true;
            } catch (IOException e) {
            } catch (ExecutionException e) {
            } catch (InterruptedException e) {
            }
        }
        return false;
    }

    //------------------------------------Election Details-------------------------------------

    public String getTitulo(){
        return titulo;
    }

    public String setElectionDetails(String title)
    {
        titulo = title;
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

    //----------------------------------------------------------------------------------------------

    //---------------------------------------Connect ----------------------------------------------

    public OAuth20Service getService(){
        if(service==null){
            service = new ServiceBuilder(appKey)
                    .apiSecret(appSecret)
                    .callback("http://localhost:8080/WebServer/ConnectAction.action")
                    .build(FacebookApi.instance());
        }
        return service;
    }

    public void setLink()
    {
        secretState = "secret" + new Random().nextInt(999_999);
        service = getService();

        System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
        System.out.println();

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        authorizationUrl = service.getAuthorizationUrl(secretState);
        System.out.println(authorizationUrl);
    }

    public String getLink() {
        if (authorizationUrl == null) {
            setLink();
        }

        return authorizationUrl;
    }

    public void setAccessToken(String codigo) throws NotFoundException.PessoaNF, IOException, InterruptedException, ExecutionException {


        //System.out.println(codigo);
        //System.out.println(secretState);

        final OAuth2AccessToken accessToken = service.getAccessToken(codigo);
        //final OAuth2AccessToken accessToken = service.extractAuthorization(service.getAuthorizationUrl(secretState)).getCode();

        Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                console.rs.setAccessToken(username, accessToken);
                break;
            }catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                throw new RemoteException();
            }
        }

    }

    /*
    public String getSecretState(){
        return secretState;
    }
     */

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    //-------------------------------------Share-----------------------------------------------------
    public boolean canShare(){
        if(getLogin()){
            OAuth2AccessToken accessToken = null;
            Timer = Calendar.getInstance();
            Timer.add(Calendar.SECOND, 30);
            while(true) {
                try {
                    accessToken = console.rs.getAccessToken(username);
                    break;
                } catch (NotFoundException.PessoaNF pessoaNF) {
                    break;
                } catch (RemoteException e) {
                    console.rebind();
                    if (Timer.after(Calendar.getInstance())) continue;
                    return false;
                }
            }
            if(accessToken!=null) return true;
            return false;
        }
        return false;
    }

    public String share(){
        OAuth2AccessToken accessToken = null;
        Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                accessToken = console.rs.getAccessToken(username);
                break;
            } catch (NotFoundException.PessoaNF pessoaNF) {
                return "erro";
            } catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                return "fail";
            }
        }
        if(accessToken==null) return "connect";

        OAuth20Service s = new ServiceBuilder(appKey)
                .callback("http://localhost:8080/WebServer/ElectionPage.action?titulo="+titulo)
                .build(FacebookApi.instance());

        final OAuthRequest request = new OAuthRequest(Verb.POST, "https://graph.facebook.com/me/feed" +
                                                                        "?message=PublishingAction");
        service.signRequest(accessToken, request);
        try (Response response = service.execute(request)) {
            System.out.println(response.getCode());
            System.out.println(response.getBody());
            return "success";
        } catch (IOException e) {
            System.out.println(e);
        } catch (ExecutionException e) {
            System.out.println(e);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        return "erro";
    }

    public void setElectionPage(String electionPage) {
        this.electionPage = electionPage;
        System.out.println(electionPage);
    }

    public String getElectionPage(){
        OAuth20Service s = new ServiceBuilder(appKey)
                .callback("http://localhost:8080/WebServer/ElectionPage.action?titulo=aaa")
                .build(FacebookApi.instance());
        String url = s.getAuthorizationUrl();
        System.out.println(url);
        String[] info = url.split("oauth\\?response_type=code&client");
        System.out.println(info[0]);
        url = info[0] + "share?app" +info[1];
        System.out.println(url);
        info = url.split("redirect_uri");
        url = info[0] + "display=popup&href" +info[1] + "&redirect_uri" + info[1];
        System.out.println(url);
        return url;
    }

    //--------------------------------------------------------------------------------------

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
