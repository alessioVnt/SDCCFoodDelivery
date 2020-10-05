package chekoutKafkaTools;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import sagaMessages.FinalizeOrderMessage;

import java.util.ArrayList;

public class CheckoutKafkaMessageList {

    private static CheckoutKafkaMessageList instance = new CheckoutKafkaMessageList();

    private ArrayList<FinalizeOrderMessage> kafkaMessageList;

    private CheckoutKafkaMessageList(){
        kafkaMessageList = new ArrayList<>();
    }

    public static synchronized CheckoutKafkaMessageList getInstance(){
        if(instance == null){
            instance = new CheckoutKafkaMessageList();
        }
        return instance;
    }

    public synchronized void addMessage(FinalizeOrderMessage messageToAdd){
        kafkaMessageList.add(messageToAdd);
    }

    public synchronized FinalizeOrderMessage pollResponse(String transactionID){
        for (FinalizeOrderMessage record : kafkaMessageList) {
            if (record.transactionID.equals(transactionID)) return record;
        }
        return null;
    }
}
