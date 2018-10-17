package no.kreutzer.utils;

import no.kreutzer.utils.JavaWebSocketClient.WebSocketCommandResponse;

public interface WebSocketMessageHandler {
	public void onMessage(String message, WebSocketCommandResponse responseImpl);
}
