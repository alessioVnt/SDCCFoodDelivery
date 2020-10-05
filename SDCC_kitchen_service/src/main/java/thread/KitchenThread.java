package thread;

import KafkaSagaMessaging.KafkaProducerCreator;
import com.google.gson.Gson;
import constants.StateEnum;
import controller.KitchenController;
import model.KitchenEvent;
import model.MenuItem;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import sagaMessages.SagaAnswer;

import java.util.List;
import java.util.Random;
import java.util.UUID;

//Thread that handle a request event
public class KitchenThread extends Thread{

    final private int PROBABILITY_ORDER_ACCEPTED = 10;
    private KitchenEvent kitchenEvent;
    private KitchenController kitchenController;

    protected KafkaProducer<String, String> producer;
    protected Gson gson;

    public KitchenThread(UUID transactionId, float price, List<MenuItem> menuItems) {
        //call Thread class constructor
        super();
        this.kitchenController = new KitchenController();
        this.kitchenEvent = new KitchenEvent(transactionId, price, menuItems);
        gson = new Gson();
        producer = KafkaProducerCreator.createProducer();
    }

    //Called by starting the thread; thread.start()
    @Override
    public void run(){
        System.out.println("Thread is running...");

        //Save a new event record to DB
        this.kitchenController.save(this.kitchenEvent);

        //Simulate Kitchen response
        if(this.isOrderAccepted(this.kitchenEvent.getMenuItems())) this.onOrderAccepted();
        else this.onOrderRejected();

        //Make current thread stop itself
        Thread.currentThread().stop();
    }

    //Let kafka know about kitchen's response: aborted
    private void onOrderRejected() {
        SagaAnswer sagaAnswer = new SagaAnswer(this.kitchenEvent.getTransactionID().toString(), StateEnum.KITCHEN_REFUSED);
        producer.send(new ProducerRecord<String, String>("saga_reply_channel", "KITCHEN_ANSWER", gson.toJson(sagaAnswer)));
    }

    //Let kafka know about kitchen's response: accepted
    private void onOrderAccepted() {
        SagaAnswer sagaAnswer = new SagaAnswer(this.kitchenEvent.getTransactionID().toString(), StateEnum.KITCHEN_ACCEPTED);
        producer.send(new ProducerRecord<String, String>("saga_reply_channel", "KITCHEN_ANSWER", gson.toJson(sagaAnswer)));
    }

    //Simulating received order, 10% of probability that order will be accepted
    private boolean isOrderAccepted(List<MenuItem> menuItems) {
        return !(new Random().nextInt(this.PROBABILITY_ORDER_ACCEPTED)==0);
    }
}
