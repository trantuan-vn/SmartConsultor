@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.username}")
    private String redisUsername;
    
    @Value("${spring.redis.password}")
    private String redisPassword;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay("/topic")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest");

        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setUsername(redisUsername);
        redisConfig.setPassword(redisPassword);
    
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(redisConfig);
        connectionFactory.afterPropertiesSet();
    
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
    
        return template;
    }

    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("websocket-topic");
    }
}
