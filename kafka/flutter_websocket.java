import 'dart:async';
import 'package:flutter/material.dart';
import 'package:web_socket_channel/io.dart';

const int MAX_MISSED_HEARTBEATS = 3;

class WebSocketExample extends StatefulWidget {
  @override
  _WebSocketExampleState createState() => _WebSocketExampleState();
}

class _WebSocketExampleState extends State<WebSocketExample> {
  IOWebSocketChannel channel;
  bool isConnected = false;
  List<String> messages = [];
  Timer heartbeatTimer;
  int missedHeartbeats = 0;

  @override
  void initState() {
    super.initState();
    connect();
    startHeartbeat();
  }

  void connect() {
    channel = IOWebSocketChannel.connect('wss://your-websocket-url.com');
    channel.stream.listen(
      (message) {
        if (message == 'heartbeat') {
          // Nhận được heartbeat từ máy chủ, đặt lại số lần đã bỏ lỡ
          missedHeartbeats = 0;
          return;
        }

        setState(() {
          messages.add(message);
        });
      },
      onError: (error) {
        setState(() {
          isConnected = false;
          missedHeartbeats++;
          if (missedHeartbeats > MAX_MISSED_HEARTBEATS) {
            reconnect();
          }
        });
      },
      onDone: () {
        setState(() {
          isConnected = false;
        });
        reconnect();
      },
    );
    setState(() {
      isConnected = true;
    });
  }

  void sendMessage(String message) {
    if (isConnected) {
      channel.sink.add(message);
    }
  }

  void disconnect() {
    channel.sink.close();
    heartbeatTimer.cancel();
    setState(() {
      isConnected = false;
      missedHeartbeats = 0;
    });
  }

  void startHeartbeat() {
    heartbeatTimer = Timer.periodic(Duration(seconds: 5), (timer) {
      if (isConnected) {
        channel.sink.add('heartbeat');
        missedHeartbeats++;
        if (missedHeartbeats > MAX_MISSED_HEARTBEATS) {
          reconnect();
        }
      }
    });
  }

  void reconnect() {
    disconnect();
    connect();
  }

  @override
  void dispose() {
    disconnect();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('WebSocket Example'),
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              itemCount: messages.length,
              itemBuilder: (context, index) {
                return ListTile(
                  title: Text(messages[index]),
                );
              },
            ),
          ),
          Padding(
            padding: EdgeInsets.all(16.0),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    decoration: InputDecoration(labelText: 'Message'),
                    onSubmitted: sendMessage,
                  ),
                ),
                IconButton(
                  icon: Icon(Icons.send),
                  onPressed: () {
                    sendMessage(TextField().controller.text);
                  },
                ),
                IconButton(
                  icon: Icon(Icons.power),
                  onPressed: disconnect,
                ),
              ],
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: reconnect,
        child: Icon(Icons.refresh),
      ),
    );
  }
}