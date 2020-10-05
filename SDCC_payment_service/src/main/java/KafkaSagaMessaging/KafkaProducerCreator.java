package KafkaSagaMessaging;

import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;

public class KafkaProducerCreator {

    public static KafkaProducer<String, String> createProducer(){
        Properties props = new Properties();
        props.put("bootstrap.servers", "my-release-kafka-0.my-release-kafka-headless.default.svc.cluster.local:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        return new KafkaProducer<>(props);
    }
}
