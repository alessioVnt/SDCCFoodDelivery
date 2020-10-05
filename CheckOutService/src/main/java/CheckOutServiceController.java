import CassandraEventStore.CassandraStoreImpl;
import KafkaSagaMessaging.KafkaConsumerCreator;
import KafkaSagaMessaging.KafkaProducerCreator;
import chekoutKafkaTools.CheckoutKafkaMessageList;
import com.google.gson.Gson;
import constants.StateEnum;
import entities.MenuItem;
import hBaseCheckOutStore.HBaseStoreImpl;
import interfaces.ICassandraStore;
import interfaces.IHBaseStore;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import sagaMessages.FinalizeOrderMessage;
import sagaMessages.NewOrderMessage;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

//Controller for the various checkout methods
public class CheckOutServiceController {

    private final ICassandraStore cassandraStore;
    private final IHBaseStore hbaseStore;
    private final Gson gson;
    private final KafkaProducer<String, String> producer;
    private NewOrderMessage newOrderMessage;

    public CheckOutServiceController(){
        System.out.println("Istantiating CheckOutServiceController");
        hbaseStore = new HBaseStoreImpl();
        hbaseStore.initialize();
        cassandraStore = new CassandraStoreImpl();
        cassandraStore.initialize();
        gson = new Gson();
        producer = KafkaProducerCreator.createProducer();
    }

    //Elaborates the message consumed on Kafka topic (last part of Saga pattern)
    private boolean elaborateMessage(FinalizeOrderMessage finalizeOrderMessage){
        System.out.println("------>FINALIZING ORDER IN CHECKOUT CONTROLLER");

        if(finalizeOrderMessage.state == StateEnum.APPROVED){
            cassandraStore.putEvent(newOrderMessage.userID, newOrderMessage.restaurantID, newOrderMessage.menuItemList, newOrderMessage.totalPrice, finalizeOrderMessage.state, finalizeOrderMessage.transactionID);
            try {
                //todo: refactor the totalprice to be double or the putRowToTable method to accept float
                hbaseStore.putRowToTable(newOrderMessage.userID, newOrderMessage.restaurantID, gson.toJson(newOrderMessage.menuItemList), Double.parseDouble("" + newOrderMessage.totalPrice));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("------>FINALIZED ORDER IN CHECKOUT CONTROLLER");
            return true;
        } else {
            //In this case the transaction is refused, no need to save it in Hbase
            cassandraStore.putEvent(newOrderMessage.userID, newOrderMessage.restaurantID, newOrderMessage.menuItemList, newOrderMessage.totalPrice, finalizeOrderMessage.state, newOrderMessage.transactionID);
            System.out.println("------>FINALIZED ORDER IN CHECKOUT CONTROLLER");
            return false;
        }
    }

    //Takes the first request for a checkout, publishes it on Kafka topic and starts to wait for a reply
    public boolean initiateCheckout(String userID, String restaurantID, List<MenuItem> menuItemList, String cardNumber, String cvc, String cardExpireDate){

        newOrderMessage = new NewOrderMessage(userID, restaurantID, menuItemList, cardNumber, cvc, cardExpireDate);
        //Create event store entry (eventID, transactionID, userID, restaurantID, menuItemsList, totalPrice) and retrieve the transaction ID
        String transactionID = cassandraStore.putEvent(userID, restaurantID, menuItemList, newOrderMessage.totalPrice, StateEnum.PENDING, "");
        //Insert transaction ID in NewOrderMessage
        newOrderMessage.setTransactionID(transactionID);
        //Publish the request on Kafka
        producer.send(new ProducerRecord<String, String>("saga_reply_channel", "NEW_TRANSACTION_REQUEST", gson.toJson(newOrderMessage)));

        FinalizeOrderMessage finalizeOrderMessage = null;

        //Loop waiting for a response on Kafka topic
        while(finalizeOrderMessage == null){
            finalizeOrderMessage = CheckoutKafkaMessageList.getInstance().pollResponse(transactionID);
        }
        return elaborateMessage(finalizeOrderMessage);

    }
}
