package no.kreutzer.utils;


import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

public class JavaWebSocketClient extends WebSocketClient {
    private static final Logger logger = LogManager.getLogger(JavaWebSocketClient.class);
	private WebSocketMessageHandler msgHandler;
	
	public interface WebSocketCommandResponse {
		void sendResponse(String response);
	}
	
	public WebSocketCommandResponse responseImpl = new WebSocketCommandResponse() {
		@Override
		public void sendResponse(String response) {
			send(response);
		}
	};
	
	public JavaWebSocketClient( URI serverUri , Draft draft ) {
		super( serverUri, draft );
	}

	public JavaWebSocketClient( URI serverURI ) {
		super( serverURI );
	}

	@Override
	public void onOpen( ServerHandshake handshakedata ) {
		logger.info( "opened connection" );
	}

	@Override
	public void onMessage( String message ) {
        logger.info( "received: " + message );
		if (message.equals("WHOAREYOU")) {
			send("RPI");
		} else if (message.equals("ACK")) {
			send("Watercontroller connected");
		} else if (msgHandler != null) {
			msgHandler.onMessage(message,responseImpl);
		}
	}

	@Override
	public void onFragment( Framedata fragment ) {
		logger.info( "received fragment: " + new String( fragment.getPayloadData().array() ) );
	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
		// The codecodes are documented in class org.java_websocket.framing.CloseFrame
		logger.info( "Connection closed by " + ( remote ? "remote peer" : "us" ) + ", reason: "+reason );
	}

	@Override
	public void onError( Exception ex ) {
		logger.error(ex);
		// if the error is fatal then onClose will be called additionally
	}

	public void setMsgHandler(WebSocketMessageHandler msgHandler) {
		this.msgHandler = msgHandler;
	}
	


	public static void main( String[] args ) throws URISyntaxException {
		JavaWebSocketClient c = new JavaWebSocketClient( new URI( "ws://localhost:8088/dataserver-0.1/websocket" ), new Draft_6455() ); // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
		c.connect();
	}

}