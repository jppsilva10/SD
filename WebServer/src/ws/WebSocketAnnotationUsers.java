package ws;

import com.company.AdminConsole;
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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/wsu")
public class WebSocketAnnotationUsers implements WSInterface {

    String RmiAddress;
    int RmiPort;
    Calendar Timer;
    AdminConsole console;
    private Session session;
    private static final Set<WebSocketAnnotationUsers> users = new CopyOnWriteArraySet<>();


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
            console.setWs((WSInterface)this);
        } catch (RemoteException | NotBoundException e) {
            handleError(e);
        }
        users.add(this);
        setDetails();
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
        if(!message.startsWith("Eleicao")) sendMessage(message);
    }

    public void setDetails(){
        String details= null;

        Timer = Calendar.getInstance();
        Timer.add(Calendar.SECOND, 30);
        while(true) {
            try {
                details = console.rs.ListarUsers();
                break;
            } catch (RemoteException e) {
                console.rebind();
                if (Timer.after(Calendar.getInstance())) continue;
                handleError(e);
                return;
            }
        }
        sendMessage("");
        Properties map = GetHashMap(details);
        for(int i= 1; i<Integer.parseInt(map.getProperty("size")); i++){
            sendMessage(map.getProperty(""+i));
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

    @Override
    public void update(String str) {
        if(!str.startsWith("Eleicao")) {
            sendMessage("");
            Properties map = GetHashMap(str);
            for(int i=0; i<Integer.parseInt(map.getProperty("size")); i++){
                sendMessage(map.getProperty(""+i));
            }
        }
    }
}
