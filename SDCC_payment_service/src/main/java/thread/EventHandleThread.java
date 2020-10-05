package thread;

import KafkaSagaMessaging.KafkaConsumerCreator;
import KafkaSagaMessaging.KafkaProducerCreator;
import com.google.gson.Gson;
import controller.PaymentController;
import model.PaymentEvent;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.UUID;

//Abstract thread class
//Extended by paymentRequested and PaymentRejected
public abstract class EventHandleThread extends Thread {

    //Attribute must be protected to let child use it
    protected PaymentEvent paymentEvent;
    protected PaymentController paymentController;
    protected KafkaProducer<String, String> producer;
    protected Gson gson;

    //Constructor used by paymenteRejectedThread
    public EventHandleThread(){
        gson = new Gson();
        producer = KafkaProducerCreator.createProducer();
        this.paymentController = new PaymentController();
    }

    //Constructor used by paymenteRequestedThread
    public EventHandleThread(UUID transactionId, String userId, float price){
        gson = new Gson();
        producer = KafkaProducerCreator.createProducer();
        this.paymentEvent = new PaymentEvent(transactionId, userId, price);
        this.paymentController = new PaymentController();
    }

    //Abstract method is declared in specialized class
    public abstract void run();
}
