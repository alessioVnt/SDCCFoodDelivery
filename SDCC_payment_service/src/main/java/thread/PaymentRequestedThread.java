package thread;

import constants.StateEnum;
import model.PaymentEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import sagaMessages.SagaAnswer;

import java.util.Random;
import java.util.UUID;

//Thread that handle a payment request
//It can save multiple record to DB according to the success of transactions
//Extends the abstract EventHandle and get his attribute
public class PaymentRequestedThread extends EventHandleThread {

    final private String PAYMENT_REQUESTED = "payment_requested";
    final private String PAYMENT_ABORTED = "payment_aborted";
    private int PROBABILITY_PAYMENT_SUCCESS = 10;
    private String creditCardNumber;
    private String deadLine;
    private String threeDigitCode;

    public PaymentRequestedThread(UUID transactionId, String userId, float price, String creditCardNumber, String deadLine, String threeDigitCode) {
        //call abstract constructor
        super(transactionId, userId, price);
        this.creditCardNumber = creditCardNumber;
        this.deadLine = deadLine;
        this.threeDigitCode = threeDigitCode;
    }

    //Called by starting the thread; thread.start()
    @Override
    public void run(){
        System.out.println("Thread is running...");

        //Save first event occurred
        this.paymentEvent.setStatus(this.PAYMENT_REQUESTED);
        this.paymentController.save(this.paymentEvent);

        //Execute payment
        if(this.executePayment(this.creditCardNumber, this.deadLine, this.threeDigitCode)) this.onPaymentAccepted();
        else{
            //Save a new record to DB
            PaymentEvent abortedPaymentEvent = new PaymentEvent(this.paymentEvent.getTransactionId(),
                    this.paymentEvent.getUserId(), this.paymentEvent.getPrice());
            abortedPaymentEvent.setStatus(this.PAYMENT_ABORTED);
            this.paymentController.save(abortedPaymentEvent);
            this.onPaymentAborted();
        }

        //Make current thread stop itself
        Thread.currentThread().stop();
        return;
    }

    //Let kafka know about payment's result: aborted
    private void onPaymentAborted() {
        SagaAnswer sagaAnswer = new SagaAnswer(this.paymentEvent.getTransactionId(), StateEnum.PAYMENT_DENIED);
        producer.send(new ProducerRecord<String, String>("saga_reply_channel", "PAYMENT_ANSWER", gson.toJson(sagaAnswer)));
    }

    //Let kafka know about payment's result: accepted
    private void onPaymentAccepted() {
        SagaAnswer sagaAnswer = new SagaAnswer(this.paymentEvent.getTransactionId(), StateEnum.PAYMENT_ACCEPTED);
        producer.send(new ProducerRecord<String, String>("saga_reply_channel", "PAYMENT_ANSWER", gson.toJson(sagaAnswer)));
    }

    //Simulating payment results, 10% of probability that payment will be aborted
    private boolean executePayment(String creditCardNumber, String deadLine, String threeDigitCode) {
        return !(new Random().nextInt(this.PROBABILITY_PAYMENT_SUCCESS)==0);
    }

}
