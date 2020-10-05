package thread;

import constants.StateEnum;
import model.PaymentEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import sagaMessages.SagaAnswer;

import java.util.UUID;

//Thread that handle a payment reject event
//Extends the abstract EventHandle and get his attribute
public class PaymentRejectedThread extends EventHandleThread{

    final private String PAYMENT_REJECTED = "payment rejected";

    public PaymentRejectedThread(UUID paymentId, String userId, float price) {
        //call abstract constructor
        super();
        //this.paymentEvent = this.paymentController.findById(paymentId);
        this.paymentEvent = new PaymentEvent(paymentId, userId, price);
    }

    //Called by starting the thread; thread.start()
    @Override
    public void run(){
        System.out.println("Thread is running...");

        //Save a new event record to DB
        paymentEvent.setStatus(this.PAYMENT_REJECTED);
        this.paymentController.save(paymentEvent);
        onRollbackFinalized();

        //Make current thread stop itself
        Thread.currentThread().stop();
        return;
    }

    //Let kafka know about payment's result: accepted
    private void onRollbackFinalized() {
        SagaAnswer sagaAnswer = new SagaAnswer(this.paymentEvent.getTransactionId(), StateEnum.PAYMENT_ROLLBACK_SUCCESS);
        producer.send(new ProducerRecord<String, String>("saga_reply_channel", "PAYMENT_ROLLBACK_ANSWER", gson.toJson(sagaAnswer)));
    }
}
