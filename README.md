📘 Redis Pub/Sub – Spring Boot (Java 21) Example

This project demonstrates a simple Redis Pub/Sub messaging system using Spring Boot 3/4, Java 21, and Redis.

Multiple Spring Boot processes subscribe to the same channel, and messages published by any instance are received by all subscribers in real time.

This is a lightweight distributed message demo without requiring Kafka or RabbitMQ.

🚀 Features

Redis Pub/Sub messaging

Multiple publishers and subscribers

Ultra-simple REST endpoint to publish messages

Zero message persistence (true Pub/Sub)

Easy to run with local Redis or Docker Redis

🧱 Architecture Overview
           ┌───────────────────┐
           │ Spring Boot App 1 │  (Subscriber + Publisher)
           └──────────┬────────┘
                      │  receives
                      ▼
                ┌──────────────┐
                │ Redis Server │
  publish --->  │ Channel:     │ ---> message received by all apps
                │ demo-channel │
                └──────────────┘
                      ▲
                      │  receives
           ┌──────────┴────────┐
           │ Spring Boot App 2 │  (Subscriber + Publisher)
           └────────────────────┘


All running Spring Boot instances connect to one Redis channel named demo-channel.

🛠 Prerequisites
1️⃣ Install Redis locally

macOS:

brew install redis
brew services start redis


Verify:

redis-cli ping


Expect:

PONG

OR use Docker (recommended)
docker run -d --name redis-server -p 6379:6379 redis:latest

📦 Project Setup
Clone the project
git clone https://github.com/<your-user>/redis-pubsub-demo.git
cd redis-pubsub-demo

Build
./mvnw clean install

⚙️ Configuration

application.yml:

spring:
  data:
    redis:
      host: localhost
      port: 6379

🧩 Core Components
1. Subscriber

RedisMessageSubscriber.java
Listens to messages on demo-channel.

@Component
public class RedisMessageSubscriber implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println("📩 Received message: " + message.toString());
    }
}

2. Redis Configuration

RedisConfig.java

@Configuration
public class RedisConfig {

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("demo-channel"));
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(RedisMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }
}

3. Publisher

RedisPublisher.java

@Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisPublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(String message) {
        System.out.println("📤 Publishing: " + message);
        redisTemplate.convertAndSend("demo-channel", message);
    }
}

4. REST API to publish messages

PublishController.java

@RestController
@RequestMapping("/publish")
public class PublishController {

    private final RedisPublisher publisher;

    public PublishController(RedisPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/{msg}")
    public String publishMessage(@PathVariable String msg) {
        publisher.publish(msg);
        return "Message published -> " + msg;
    }
}

▶️ How to Run Multiple Instances

Start Instance 1

./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081


Start Instance 2

./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8082


Start Instance 3

./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8083


All three processes are subscribers listening on the same Redis channel.

📤 Publish a message

Send a message using any instance:

curl -X POST http://localhost:8081/publish/hello

Console Output (all instances):
📤 Publishing: hello        (only instance 8081)
📩 Received message: hello  (8081)
📩 Received message: hello  (8082)
📩 Received message: hello  (8083)


This confirms Redis Pub/Sub works across multiple processes.

📝 Notes
Topic	Value
Message persistence	❌ No persistence (Redis Pub/Sub is fire-and-forget)
Subscribers	All active subscribers receive every message
Ordering	Guaranteed per connection
Use case	Real-time messaging, notifications, events
🧪 Useful Redis Commands

Open Redis CLI:

redis-cli


Subscribe manually:

SUBSCRIBE demo-channel


Publish manually:

PUBLISH demo-channel "hello world"

🧰 Troubleshooting
❗ Message not received?

Check:

Redis is running: redis-cli ping

All apps use the same channel name

Port 6379 is not blocked

❗ Publisher works but no subscriber gets messages?

Make sure the subscriber bean is loaded:

@Component
public class RedisMessageSubscriber implements MessageListener { ... }
