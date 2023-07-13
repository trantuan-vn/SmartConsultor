@Component
public class SocketHandler extends TextWebSocketHandler {

    private RedisTemplate<String, Object> redisTemplate;
    private ChannelTopic topic;

    public SocketHandler(RedisTemplate<String, Object> redisTemplate, ChannelTopic topic) {
        this.redisTemplate = redisTemplate;
        this.topic = topic;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> value = objectMapper.readValue(message.getPayload(), new TypeReference<Map<String, String>>() {
        });
        String username = value.get("username");
        String token = value.get("token");
        
        // Lưu trữ thông tin client vào Redis
        redisTemplate.opsForHash().put("clients", session.getId(), username);
        
        // Gửi tin nhắn đến danh sách client khác
        redisTemplate.convertAndSend(topic.getTopic(), "New user connected: " + username);
    }
}
