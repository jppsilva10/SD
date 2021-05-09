package ws;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/ws")
public class WebSocketAnnotation {
    private static final AtomicInteger sequence = new AtomicInteger(1);
    private final String username;
    private Session session;
    private String room;
    private static final Set<WebSocketAnnotation> users = new CopyOnWriteArraySet<>();

    public WebSocketAnnotation() {
        username = "User" + sequence.getAndIncrement();
    }

    @OnOpen
    public void start(Session session) {
        this.session = session;
        this.room="0";
        users.add(this);
        String message = "*" + username + "* connected.";
        sendMessage(message);
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
    	String upperCaseMessage = message.toUpperCase();
    	sendMessage("[" + username + "] " + upperCaseMessage);
    }

    
    @OnError
    public void handleError(Throwable t) {
    	t.printStackTrace();
    }

    private void sendMessage(String text) {
    	// uses *this* object's session to call sendText()
    	try {
    	    if(text.contains("R:")){
    	        String[] str = text.split("R:");
                String room;
    	        if(str.length>1) {
                    room = str[1];
                }
    	        else{
    	            room = "0";
                }
    	        if (!this.room.equals(room)) {
    	            this.room = room;
                    for (WebSocketAnnotation ws : users) {
                        synchronized (ws) {
                            if (ws.room.equals(this.room))
                                ws.session.getBasicRemote().sendText("[" + username + "] " + "room " + this.room);
                        }
                    }
                }
            }
    	    else {
                for (WebSocketAnnotation ws : users) {
                    synchronized (ws) {
                        if (ws.room.equals(this.room))
                            ws.session.getBasicRemote().sendText(text);
                    }
                }
            }
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
