package no.kreutzer.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.drafts.Draft_6455;

public class WebSocketService {
    private static final Logger logger = LogManager.getLogger(WebSocketService.class);
    final static CountDownLatch messageLatch = new CountDownLatch(1);
    private JavaWebSocketClient client;
    private String uri;

    public WebSocketService(String uri) {
        this.uri = uri;
           
        connect();
    }
    
    private void connect() {
        try {
            logger.info("Open websocket to: "+uri);
            client = new JavaWebSocketClient(new URI(uri),new Draft_6455());
            client.setMsgHandler(new WebSocketMessageHandlerImpl());
            client.connect();
        } catch (URISyntaxException ex) {
            logger.error("URISyntaxException exception: " + ex.getMessage());
        }         
    }
    
    public void sendMessage(String message) {
    	try {
    		client.send(message);
    	} catch (Exception e) {
    	    logger.error("Failed to send: "+e);
    	}
    }

    public void checkAlive() {
        if (client.isClosed()) {
            logger.info("Attempting reconnect..");
            connect();
        }
    }
}