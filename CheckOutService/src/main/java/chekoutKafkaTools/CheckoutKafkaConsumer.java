package chekoutKafkaTools;

import KafkaSagaMessaging.KafkaConsumerCreator;
import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import sagaMessages.FinalizeOrderMessage;

import java.time.Duration;
import java.util.Arrays;

public class CheckoutKafkaConsumer implements Runnable {

    private final KafkaConsumer<String, String> consumer;
    private final Gson gson;


    public CheckoutKafkaConsumer() {
        this.consumer = KafkaConsumerCreator.createConsumer();
        consumer.subscribe(Arrays.asList("checkout_service_req_channel"));
        gson = new Gson();
    }

    public void consumeMessages(){
        while (true){
            //Poll on kafka topic for 100ms
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            FinalizeOrderMessage arrivedMessage;
            //If message polled, elaborate
            if(records.count() != 0){
                for (ConsumerRecord<String, String> record : records){
                    //TEST PRINT
                    System.out.printf("CHECKOUT READ FROM KAFKA:  offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                    arrivedMessage = gson.fromJson(record.value(), FinalizeOrderMessage.class);
                    CheckoutKafkaMessageList.getInstance().addMessage(arrivedMessage);
                }
            }
        }
    }

    @Override
    public void run() {
        System.out.println("CHECKOUT SERVICE KAFKA CONSUMER STARTING TO CONSUME");
        consumeMessages();
    }
}
