@Component
public class WebSocketController implements WebSocketConfigurer {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ChannelTopic topic;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SocketHandler(redisTemplate, topic), "/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
