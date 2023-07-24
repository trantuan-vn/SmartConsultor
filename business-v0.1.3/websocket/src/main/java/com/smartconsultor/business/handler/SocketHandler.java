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

@Component
public class SocketHandler extends TextWebSocketHandler {

    @Value("${spring.websocket.timeout}")
    private long WEBSOCKET_TIMEOUT;    
    
    private RedisTemplate<String, Object> redisTemplate;
    private ChannelTopic topic;
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private ScheduledExecutorService executorService;

    public SocketHandler(RedisTemplate<String, Object> redisTemplate, ChannelTopic topic, KafkaTemplate<String, String> kafkaTemplate) {
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
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload=message.getPayload();
        
        if (isHeartbeatMessage(payload)) {
        } else {
            // Đẩy tin nhắn vào topic INPUT của Kafka
            kafkaTemplate.send("INPUT", payload);
        }        
        //session.updateLastAccessedTime();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Xóa trạng thái kết nối khỏi Redis
        redisTemplate.opsForHash().delete("clients", session.getId());
    }
    
    private void removeExpiredSessions() {
        // Lấy danh sách tất cả các session
        Map<Object, Object> sessions = redisTemplate.opsForHash().entries("clients");
        
        // Lặp qua từng session để kiểm tra timeout
        for (Object key : sessions.keySet()) {
            String sessionId = (String) key;
            WebSocketSession session = (WebSocketSession) sessions.get(sessionId);
            
            if (session != null && session.isOpen() && session.getId().equals(sessionId)) {
                try {
                    long lastAccessTimeMillis = 1;//session.getLastAccessedTime();
                    long currentTimeMillis = System.currentTimeMillis();
                    if (lastAccessTimeMillis + WEBSOCKET_TIMEOUT < currentTimeMillis) {
                        session.close();
                        redisTemplate.opsForHash().delete("clients", sessionId);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isHeartbeatMessage(String message) {
        // Kiểm tra nội dung tin nhắn để xác định nó có phải là heartbeat hay không
        // Đây chỉ là một ví dụ đơn giản, bạn có thể điều chỉnh phù hợp với nội dung tin nhắn của mình
        if (message.equals("ping")) {
            return true;
        }
        
        return false;
    }
    
}
