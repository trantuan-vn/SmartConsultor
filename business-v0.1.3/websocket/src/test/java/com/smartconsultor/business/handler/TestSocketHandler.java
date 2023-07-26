package com.smartconsultor.business.handler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

@Component 
public class TestSocketHandler extends TextWebSocketHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(TestSocketHandler.class);
	
	
	
	private WebSocketSession session;
	
	@Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Lưu trạng thái kết nối vào Redis với thời gian timeout
		this.session = session;
		logger.info("Connection established with session ID: {}", session.getId());
    } 
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		logger.info("Message from server: " + message.getPayload());
	}

	public WebSocketSession getSession() {
		// TODO Auto-generated method stub
		return this.session;
	}
	
}