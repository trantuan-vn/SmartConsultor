package com.smartconsultor.business.handler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

@Component 
public class SocketHandler extends TextWebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);
    
	
	@Value("${spring.websocket.timeout}")
    private long WEBSOCKET_TIMEOUT;    

	@Value("${spring.websocket.servername}")
    private String SERVER_NAME;    

	@Value("${spring.websocket.lastaccesstime}")
    private String LASTACCESSTIME;   	
	
    private RedisTemplate<String, Object> redisTemplate;
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private ScheduledExecutorService executorService;

    public SocketHandler(RedisTemplate<String, Object> redisTemplate, KafkaTemplate<String, String> kafkaTemplate) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        
        this.executorService = Executors.newScheduledThreadPool(1);
        // Lập lịch kiểm tra và loại bỏ session sau 5 phút timeout
        executorService.scheduleAtFixedRate(() -> {
			try {
				removeExpiredSessions();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("An error occurred while removing expired sessions: {}", e.getMessage());
			}
		}, WEBSOCKET_TIMEOUT, WEBSOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            // Lưu trạng thái kết nối vào Redis với thời gian timeout
            redisTemplate.opsForHash().put(SERVER_NAME, session.getId(), session);
            redisTemplate.opsForHash().put(LASTACCESSTIME, session.getId(), System.currentTimeMillis());
            logger.info("Connection established with session ID: {}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload=message.getPayload();
        
        if (isHeartbeatMessage(payload)) {
        } 
        else if (isLogonMessage(payload)) {
            // Đẩy tin nhắn vào topic INPUT của Kafka
            kafkaTemplate.send("logon", payload);        	
        }
        else {

        }        
        redisTemplate.opsForHash().put(LASTACCESSTIME, session.getId(), System.currentTimeMillis());
        
        logger.info("Received message: {} from session ID: {}", payload, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Xóa trạng thái kết nối khỏi Redis
        redisTemplate.opsForHash().delete(SERVER_NAME, session.getId());
        redisTemplate.opsForHash().delete(LASTACCESSTIME, session.getId());
        logger.info("Connection closed for session ID: {}", session.getId());
    }
    
    private void removeExpiredSessions() throws Exception {
        // Lấy danh sách tất cả các session
        Map<Object, Object> sessions = redisTemplate.opsForHash().entries(SERVER_NAME);
        
        // Lặp qua từng session để kiểm tra timeout
        for (Object key : sessions.keySet()) {
            String sessionId = (String) key;
            WebSocketSession session = (WebSocketSession) sessions.get(sessionId);
            
            if (session.isOpen()) {
                long lastAccessTimeMillis = (long) redisTemplate.opsForHash().get(LASTACCESSTIME, session.getId());
                long currentTimeMillis = System.currentTimeMillis();
                if (lastAccessTimeMillis + WEBSOCKET_TIMEOUT < currentTimeMillis) {
                    session.close();
                    redisTemplate.opsForHash().delete(SERVER_NAME, sessionId);
                    redisTemplate.opsForHash().delete(LASTACCESSTIME, sessionId);
                    logger.info("Removed expired session having sessionid: {}", session.getId());                        
                }
            }
        }
    }

    private boolean isHeartbeatMessage(String message) {
        // Kiểm tra nội dung tin nhắn để xác định nó có phải là heartbeat hay không
        if (message.contains("ping")) {
            return true;
        }
        
        return false;
    }
    private boolean isLogonMessage(String message) {
        // Kiểm tra nội dung tin nhắn để xác định nó có phải là heartbeat hay không
        if (message.contains("logon")) {
            return true;
        }
        
        return false;
    }    
}
