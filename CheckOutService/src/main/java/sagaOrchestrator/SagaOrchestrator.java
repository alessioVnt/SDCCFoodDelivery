package sagaOrchestrator;

import KafkaSagaMessaging.KafkaConsumerCreator;
import KafkaSagaMessaging.KafkaProducerCreator;
import com.google.gson.Gson;
import constants.StateEnum;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import sagaMessages.*;

import java.time.Duration;
import java.util.*;


public class SagaOrchestrator implements Runnable {

    //The log will be an HashMap of key: name of the event to log, value: Json string if necessary of the event details
    public Map<String, Map<String,String>> orchestratorLogs;
    //public Map<String, String> orchestratorLog;
    private KafkaProducer<String, String> producer;
    //Json parser
    private Gson gson;

    //Start transaction by sending the payment request
    private void startTransaction(NewOrderMessage newOrderMessage){

        //Create payment request
        PaymentRequest paymentRequest = new PaymentRequest(newOrderMessage.userID, newOrderMessage.cardNumber, newOrderMessage.cvc, newOrderMessage.cardExpireDate, newOrderMessage.totalPrice, newOrderMessage.transactionID);
        //Transform payment request to a Json string
        String jsonPaymentRequest = gson.toJson(paymentRequest);
        //Update log
        updateLog(newOrderMessage.transactionID, "START_PAYMENT", jsonPaymentRequest);
        //Publish payment request on Kafka
        producer.send(new ProducerRecord<String, String>("payment_service_req_channel", "PAYMENT_REQUEST", jsonPaymentRequest));
        printLog(newOrderMessage.transactionID);
    }

    private void sendKitchenApprovalRequest(SagaAnswer sagaAnswer){

        if(orchestratorLogs.get(sagaAnswer.transactionID) == null) return;
        //Get the data needed from the orchestrator log
        NewOrderMessage orderMessage = gson.fromJson(orchestratorLogs.get(sagaAnswer.transactionID).get("NEW_TRANSACTION_REQUEST"), NewOrderMessage.class);
        //Create kitchen request
        KitchenRequest kitchenRequest = new KitchenRequest(orderMessage.restaurantID, orderMessage.menuItemList, orderMessage.totalPrice, orderMessage.transactionID);
        //Transform KitchenRequest to a Json string
        String jsonKitchenRequest = gson.toJson(kitchenRequest);
        //Update the log
        updateLog(orderMessage.transactionID, "START_KITCHEN_APPROVAL", jsonKitchenRequest);
        //Publish kitchen request on Kafka
        producer.send(new ProducerRecord<String, String>("kitchen_service_req_channel", "KITCHEN_REQUEST", jsonKitchenRequest));
    }

    private void sendFinalizeTransactionRequest(SagaAnswer sagaAnswer, boolean isTransactionSuccessfull){
        System.out.println("----->FINALIZING ORDER " + sagaAnswer.state);
        FinalizeOrderMessage finalizeOrderMessage;
        if (isTransactionSuccessfull) {
            finalizeOrderMessage = new FinalizeOrderMessage(StateEnum.APPROVED, sagaAnswer.transactionID);
        } else {
            finalizeOrderMessage = new FinalizeOrderMessage(StateEnum.REJECTED,  sagaAnswer.transactionID);
        }
        String jsonFinalizeOrderMessage = gson.toJson(finalizeOrderMessage);

        //This logging operation shouldn't be necessary since the log concerning this transaction is going to be dropped in this method
        //updateLog(sagaAnswer.transactionID ,"END_SAGA_TRANSACTION", jsonFinalizeOrderMessage);

        //Publish finalize request on Kafka
        producer.send(new ProducerRecord<String, String>("checkout_service_req_channel", "FINALIZE_REQUEST", jsonFinalizeOrderMessage));

        dropTransactionLog(sagaAnswer.transactionID);
        System.out.println("---->FINISHED SAGA ORCHESTRATOR FINALIZING ORDER");
    }

    private void sendCompensationRequest(SagaAnswer sagaAnswer){
        if(orchestratorLogs.get(sagaAnswer.transactionID) == null) return;
        //Get the data needed from the orchestrator log
        NewOrderMessage orderMessage = gson.fromJson(orchestratorLogs.get(sagaAnswer.transactionID).get("NEW_TRANSACTION_REQUEST"), NewOrderMessage.class);
        //Create compensation request
        CompensatePaymentRequest compensatePaymentRequest = new CompensatePaymentRequest(orderMessage.userID, sagaAnswer.transactionID, -1 * orderMessage.totalPrice);
        //Transform CompensatePaymentRequest to Json string
        String jsonCompensatePaymentRequest = gson.toJson(compensatePaymentRequest);
        //Update log
        updateLog(sagaAnswer.transactionID, "START_COMPENSATE_REQUEST", jsonCompensatePaymentRequest);
        //Publish request on Kafka
        producer.send(new ProducerRecord<String, String>("payment_service_req_channel", "COMPENSATE_REQUEST", jsonCompensatePaymentRequest));
    }

    private void updateLog(String transactionID, String key, String value){
        if(key.equals("NEW_TRANSACTION_REQUEST"))
        {
            Map<String, String> mapToInsert = new HashMap<>();
            orchestratorLogs.put(transactionID, mapToInsert);
        }
        if(orchestratorLogs.get(transactionID) != null) orchestratorLogs.get(transactionID).put(key, value);
    }

    private void initializeLog(){
        orchestratorLogs = new HashMap<String, Map<String, String>>();
    }

    private void dropTransactionLog(String transactionID){
        orchestratorLogs.remove(transactionID);
    }

    //For test purpose
    private void printLog(String transactionID){
        //ToDo: leva i commenti, disattivato solo per i test
        /*String jsonLog = gson.toJson(orchestratorLogs.get(transactionID));
        System.out.println(jsonLog);*/
        int i = 1;
    }

    private void elaborateMessage(String messageKey, String messageConsumed){
        System.out.println("Elaborating: " + messageConsumed);
        if (messageKey == null || messageKey.equals("NEW_TRANSACTION_REQUEST")){
            //Start new transaction
            NewOrderMessage newOrder = gson.fromJson(messageConsumed, NewOrderMessage.class);
            updateLog(newOrder.transactionID, "NEW_TRANSACTION_REQUEST", messageConsumed);
            startTransaction(newOrder);
        }
        else {
            SagaAnswer sagaAnswer = gson.fromJson(messageConsumed, SagaAnswer.class);
            switch (sagaAnswer.state){
                case PAYMENT_ACCEPTED:
                    updateLog(sagaAnswer.transactionID, "END_PAYMENT ", gson.toJson(sagaAnswer));
                    sendKitchenApprovalRequest(sagaAnswer);
                    break;
                case PAYMENT_DENIED:
                    updateLog(sagaAnswer.transactionID,"END_PAYMENT", gson.toJson(sagaAnswer));
                    sendFinalizeTransactionRequest(sagaAnswer, false);
                    break;
                case PAYMENT_ROLLBACK_SUCCESS:
                    updateLog(sagaAnswer.transactionID,"END_COMPENSATE_REQUEST", gson.toJson(sagaAnswer));
                    sendFinalizeTransactionRequest(sagaAnswer, false);
                    break;
                case KITCHEN_ACCEPTED:
                    updateLog(sagaAnswer.transactionID,"END_KITCHEN_APPROVAL", gson.toJson(sagaAnswer));
                    sendFinalizeTransactionRequest(sagaAnswer, true);
                    break;
                case KITCHEN_REFUSED:
                    updateLog(sagaAnswer.transactionID,"END_KITCHEN_APPROVAL", gson.toJson(sagaAnswer));
                    sendCompensationRequest(sagaAnswer);
                    break;
            }
        }
    }

    public void run() {
        initializeLog();
        gson = new Gson();
        //Connect to Kafka
        KafkaConsumer<String, String> consumer = KafkaConsumerCreator.createConsumer();
        consumer.subscribe(Arrays.asList("saga_reply_channel"));
        producer = KafkaProducerCreator.createProducer();

        //Infinite loop listening on Kafka topic
        while (true){
            //Poll on kafka topic for 100ms
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            //If message polled, elaborate
            if(records.count() != 0){
                for (ConsumerRecord<String, String> record : records){
                    System.out.printf("SAGA ORCHESTRATOR READ FROM KAFKA:  offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                    elaborateMessage(record.key(), record.value());
                }
            }
        }
    }
}
