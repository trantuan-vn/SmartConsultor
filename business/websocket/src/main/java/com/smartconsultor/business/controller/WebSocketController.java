@Component
public class WebSocketController implements WebSocketConfigurer {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ChannelTopic topic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SocketHandler(redisTemplate, topic, kafkaTemplate), "/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
