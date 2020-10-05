package service;

import KafkaSagaMessaging.KafkaConsumerCreator;
import com.google.gson.Gson;
import sagaMessages.KitchenRequest;
import model.MenuItem;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import thread.KitchenThread;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class KitchenService {

    private KafkaConsumer<String, String> consumer;
    private Gson gson = new Gson();
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

    //Main thread is always listening to kafka's topic
    //waiting for an order event to execute and store
    public void kafkaSubscriber(){
        //Create Kafka consumer
        consumer = KafkaConsumerCreator.createConsumer();
        consumer.subscribe(Arrays.asList("kitchen_service_req_channel"));
        System.out.println("Consumer subscribed!");
        while(true){
            //Poll on kafka topic for 100ms
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            //If message polled, elaborate
            if(records.count() != 0){
                for (ConsumerRecord<String, String> record : records){
                    System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                    KitchenRequest kitchenRequest = gson.fromJson(record.value(), KitchenRequest.class);
                    getKitchenOrderResponse(kitchenRequest.getTransactionID(), kitchenRequest.getTotalPrice(), kitchenRequest.getMenuItems());
                }
            }
        }
    }

    //Register the event to the DB
    //Order can be either accepted or rejected
    public void getKitchenOrderResponse(UUID transactionId, float price, List<MenuItem> menuItems){
        //Start a new thread to get this job done
        KitchenThread kitchenThread = new KitchenThread(transactionId, price, menuItems);
        //kitchenThread.start();
        executor.submit(kitchenThread);
    }
}
