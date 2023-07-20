@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    /*
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Cấu hình thêm Serializer/Deserializer, Group ID, v.v. (nếu cần)
        // ...
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Cấu hình thêm Serializer/Deserializer, v.v. (nếu cần)
        // ...
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
    }
}
