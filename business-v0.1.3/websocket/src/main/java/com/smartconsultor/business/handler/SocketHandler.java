package com.smartconsultor.business.handler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
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
    
    private RedisTemplate<String, Object> redisTemplate;
    private ChannelTopic topic;
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private ScheduledExecutorService executorService;

    public SocketHandler(RedisTemplate<String, Object> redisTemplate, ChannelTopic topic, 
    		KafkaTemplate<String, String> kafkaTemplate) throws Exception {
        this.redisTemplate = redisTemplate;
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
        
        this.executorService = Executors.newScheduledThreadPool(1);
        // Lập lịch kiểm tra và loại bỏ session sau 5 phút timeout
        executorService.scheduleAtFixedRate(this::removeExpiredSessions, WEBSOCKET_TIMEOUT, WEBSOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            // Lưu trạng thái kết nối vào Redis với thời gian timeout
            redisTemplate.opsForHash().put("clients", session.getId(), session);
            redisTemplate.opsForHash().put("lastAccessTime", session.getId(), System.currentTimeMillis());
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
        redisTemplate.opsForHash().put("lastAccessTime", session.getId(), System.currentTimeMillis());
        
        logger.info("Received message: {} from session ID: {}", payload, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Xóa trạng thái kết nối khỏi Redis
        redisTemplate.opsForHash().delete("clients", session.getId());
        redisTemplate.opsForHash().delete("lastAccessTime", session.getId());
        logger.info("Connection closed for session ID: {}", session.getId());
    }
    
    private void removeExpiredSessions() throws Exception {
        // Lấy danh sách tất cả các session
        Map<Object, Object> sessions = redisTemplate.opsForHash().entries("clients");
        
        // Lặp qua từng session để kiểm tra timeout
        for (Object key : sessions.keySet()) {
            String sessionId = (String) key;
            WebSocketSession session = (WebSocketSession) sessions.get(sessionId);
            
            if (session != null && session.isOpen() && session.getId().equals(sessionId)) {
                long lastAccessTimeMillis = (long) redisTemplate.opsForHash().get("lastAccessTime", session.getId());
                long currentTimeMillis = System.currentTimeMillis();
                if (lastAccessTimeMillis + WEBSOCKET_TIMEOUT < currentTimeMillis) {
                    session.close();
                    redisTemplate.opsForHash().delete("clients", sessionId);
                    redisTemplate.opsForHash().delete("lastAccessTime", sessionId);
                    logger.info("Removed expired session having sessionid: {}", session.getId());                        
                }
            }
        }
    }

    private boolean isHeartbeatMessage(String message) {
        // Kiểm tra nội dung tin nhắn để xác định nó có phải là heartbeat hay không
        if (message.equals("ping")) {
            return true;
        }
        
        return false;
    }
    private boolean isLogonMessage(String message) {
        // Kiểm tra nội dung tin nhắn để xác định nó có phải là heartbeat hay không
        if (message.equals("logon")) {
            return true;
        }
        
        return false;
    }    
}
