
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

// Lớp để quản lý kết nối Websocket
@WebSocket
public class WebSocketManagerMicroservice {
    // Kết nối tới Redis
    private static Jedis jedis = new Jedis("localhost", 6379);

    // handler khi kết nối được mở
    @OnWebSocketConnect
    public void onConnect(Session session) {
        String user = extractUserFromSession(session);

        // lưu thông tin kết nối Websocket của người dùng vào Redis
        jedis.set(user, session.getRemoteAddress().toString());
    }

    // handler khi kết nối bị đóng
    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String user = extractUserFromSession(session);

        // xóa thông tin kết nối Websocket của người dùng từ Redis
        jedis.del(user);
    }

    // handler khi nhận được tin nhắn từ client
    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
		// Tạo producer Kafka
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092"); // Địa chỉ và cổng của Kafka broker
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		Producer<String, String> producer = new KafkaProducer<>(props);

		// Gửi message tới topic "input"
		producer.send(new ProducerRecord<>("input", message), new Callback() {
			@Override
			public void onCompletion(RecordMetadata metadata, Exception exception) {
				if (exception != null) {
					// Xử lý lỗi khi gửi message tới Kafka
					exception.printStackTrace();
				} else {
					// Xử lý thành công
					System.out.println("Gửi message thành công: topic = " + metadata.topic() + ", partition = " + metadata.partition() + ", offset = " + metadata.offset());
				}
			}
		});
		
		producer.close();		
    }

	// Hàm để trích xuất user từ session
	private static String extractUserFromSession(Session session) {
		String remoteAddress = session.getRemoteAddress().toString();

		// Truy vấn Redis để lấy thông tin user từ remoteAddress
		String user = jedis.get(remoteAddress);

		// Xử lý kết quả tìm kiếm (nếu cần)
		
		return user;
	}

    // hàm để gửi dữ liệu tới người dùng qua kết nối Websocket
    private static void sendToUser(String user, String message) {
        // lấy thông tin kết nối Websocket của người dùng từ Redis
        String remoteAddress = jedis.get(user);

        if (remoteAddress != null) {
            Session session = WebSocketClient.connect(remoteAddress);
            try {
                session.getRemote().sendString(message);
            } catch (IOException | WebSocketException e) {
                // xử lý lỗi khi gửi dữ liệu qua Websocket
                e.printStackTrace();
            }
        }
    }
}	
	public static void main(String[] args) throws InterruptedException {
        // Khởi tạo server Websocket
        Server server = new Server(8080);
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");
        server.setHandler(contextHandler);

        // Khởi tạo WebSocketServerContainer
        WebSocketServerContainerInitializer.configureContext(contextHandler).addEndpoint(WebSocketManager.class);

        // Bắt đầu server Websocket
        server.start();
        server.join();		
    }	
}


public class SparkMicroservice {
    public static void main(String[] args) throws InterruptedException {
        // Khởi tạo SparkContext
        SparkConf conf = new SparkConf().setAppName("MySparkApplication");
        JavaSparkContext sparkContext = new JavaSparkContext(conf);

        // Khởi tạo StreamingContext
        JavaStreamingContext streamingContext = new JavaStreamingContext(sparkContext, Durations.seconds(1));

        // Cấu hình Kafka
        String kafkaBrokers = "localhost:9092";
        String kafkaTopic = "iamast";
        SparkConf sparkConf = new SparkConf().setAppName("MySparkApplication");

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", kafkaBrokers);
        kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("group.id", "your_consumer_group");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        Collection<String> topics = Arrays.asList(kafkaTopic);
		
        JavaInputDStream<ConsumerRecord<String, String>> kafkaStream = KafkaUtils.createDirectStream(
                streamingContext,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
        );

        JavaDStream<String> messages = kafkaStream.map(ConsumerRecord::value);

        // Xử lý dữ liệu
        messages.foreachRDD(rdd -> {
            rdd.foreachPartition(partition -> {
                while (partition.hasNext()) {
					ConsumerRecord<String, String> record = partition.next();
					String user = record.key();
					String[] values = record.value().split("\\+"); // Tách value1 và value2
					int value1 = Integer.parseInt(values[0]);
					int value2 = Integer.parseInt(values[1]);
					int sum = value1 + value2;
					String message = String.valueOf(sum);
					// Phân phát dữ liệu cho từng user tương ứng với ứng dụng client
					WebSocketManager.sendToUser(user, message);
                }
            });
        });

        // Bắt đầu xử lý và chờ
        streamingContext.start();
        streamingContext.awaitTermination();
		
        // Khởi tạo server Websocket
        Server server = new Server(8080);
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");
        server.setHandler(contextHandler);

        // Khởi tạo WebSocketServerContainer
        WebSocketServerContainerInitializer.configureContext(contextHandler).addEndpoint(WebSocketManager.class);

        // Bắt đầu server Websocket
        server.start();
        server.join();		
    }
}


//CLIENT

import 'package:flutter/material.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:web_socket_channel/io.dart';

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'WebSocket App',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final channel = IOWebSocketChannel.connect('ws://your-websocket-url');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('WebSocket App'),
      ),
      body: Center(
        child: StreamBuilder(
          stream: channel.stream,
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              return Text(snapshot.data);
            }
            return CircularProgressIndicator();
          },
        ),
      ),
    );
  }

  @override
  void dispose() {
    channel.sink.close();
    super.dispose();
  }
}
