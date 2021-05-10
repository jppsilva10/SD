package ws;

import com.company.AdminConsole;
import com.company.NotFoundException;
import com.company.RmiClient;
import com.company.RmiServer;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/ws")
public class WebSocketAnnotation {

    String RmiAddress;
    int RmiPort;
    Calendar Timer;
    AdminConsole console;

    private static final AtomicInteger sequence = new AtomicInteger(1);
    private final String username;
    private Session session;
    private String titulo;
    private static final Set<WebSocketAnnotation> users = new CopyOnWriteArraySet<>();

    public WebSocketAnnotation() {
        username = "User" + sequence.getAndIncrement();
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


    @OnOpen
    public void start(Session session) {
        this.session = session;
        try {
            console = new AdminConsole();
            this.config();
            console.rs = (RmiServer) LocateRegistry.getRegistry(this.RmiAddress,this.RmiPort).lookup("server");
            console.rs.subscribe((RmiClient) console);
        } catch (RemoteException | NotBoundException e) {
            handleError(e);
        }
        users.add(this);
    }

    @OnClose
    public void end() {
        users.remove(this);
    	// clean up once the WebSocket connection is closed
    }

    @OnMessage
    public void receiveMessage(String message) {
		// one should never trust the client, and sensitive HTML
        // characters should be replaced with &lt; &gt; &quot; &amp;
        if(titulo==null){

            titulo = message;
            setDetails();
        }
        else{
            sendMessage(message);
        }
    }

    public void setDetails(){
        String details= null;
        System.out.println(titulo);

        Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                details = console.rs.consultarEleicao(titulo);
                break;
            } catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                handleError(e);
                return;
            } catch (NotFoundException.EleicaoNF eleicaoNF) {
                handleError(eleicaoNF);
                return;
            }
        }
        String[] info = details.split("\n");
        sendMessage("descricao|"+ info[1].split(": ")[1]);
        sendMessage("tipo|"+ info[2].split(": ")[1]);
        int i = 4;
        for(; info[i].split(": ").length==1; i++){
            sendMessage("listas|"+ info[i]);
        }
        sendMessage("inicio|"+ info[i].split(": ")[1]);
        i++;
        sendMessage("fim|"+ info[i].split(": ")[1]);
        i+=2;
        if (i>=info.length) return;
        for(; info[i].split(": ").length==1 && i<info.length; i++){
            sendMessage("eleitores|"+ info[i]);
        }

    }

    
    @OnError
    public void handleError(Throwable t) {
    	t.printStackTrace();
    }

    private void sendMessage(String text) {
    	// uses *this* object's session to call sendText()
    	try {
    	    this.session.getBasicRemote().sendText(text);
			//this.session.getBasicRemote().sendText(text);
		} catch (IOException e) {
			// clean up once the WebSocket connection is closed
            users.remove(this);
			try {
				this.session.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
    }
}
