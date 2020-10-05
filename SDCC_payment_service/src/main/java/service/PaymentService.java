package service;

import KafkaSagaMessaging.KafkaConsumerCreator;
import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import sagaMessages.CompensatePaymentRequest;
import sagaMessages.PaymentRequest;
import thread.PaymentRejectedThread;
import thread.PaymentRequestedThread;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

//Payment service class
public class PaymentService {

    private KafkaConsumer<String, String> consumer;
    private Gson gson = new Gson();
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

    //Main thread is always listening to kafka's topic
    //waiting for a payment event to execute and store
    public void kafkaSubscriber(){
        //Create Kafka consumer
        consumer = KafkaConsumerCreator.createConsumer();
        consumer.subscribe(Arrays.asList("payment_service_req_channel"));
        System.out.println("Consumer created!");
        while(true){
            //Poll on kafka topic for 100ms
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            //If message polled, elaborate
            if(records.count() != 0){
                for (ConsumerRecord<String, String> record : records){
                    System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                    if (record.key().equals("PAYMENT_REQUEST"))
                    {
                        PaymentRequest paymentRequest = gson.fromJson(record.value(), PaymentRequest.class);
                        executePaymentTransaction(paymentRequest.transactionID, paymentRequest.userID, paymentRequest.totalPrice, paymentRequest.cardNumber, paymentRequest.cardExpireDate, paymentRequest.cvc);
                    } else {
                        CompensatePaymentRequest compensatePaymentRequest = gson.fromJson(record.value(), CompensatePaymentRequest.class);
                        updateEventStatus(compensatePaymentRequest.transactionID, compensatePaymentRequest.userID, compensatePaymentRequest.totalPrice);
                    }
                }
            }
        }
    }


    //Execute the payment and register the event to the DB
    //Payment can be either accepted or aborted, both result will be stored
    public void executePaymentTransaction(UUID paymentId, String userId, float price, String creditCardNumber, String deadLine, String threeDigitCode){
        //Start a new thread to get this job done
        PaymentRequestedThread paymentRequestedThread = new PaymentRequestedThread(paymentId, userId, price, creditCardNumber, deadLine, threeDigitCode);
        //paymentRequestedThread.start();
        executor.submit(paymentRequestedThread);
    }

    //Store a certain payment with a rejected status to the DB
    public void updateEventStatus(UUID paymentId, String userId, float price){
        //Start a new thread to get this job done
        PaymentRejectedThread paymentRejectedThread = new PaymentRejectedThread(paymentId, userId, price);
        //paymentRejectedThread.start();
        executor.submit(paymentRejectedThread);
    }

}
