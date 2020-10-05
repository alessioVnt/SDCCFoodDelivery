package sagaMessages;

import constants.StateEnum;

public class SagaAnswer {
    public String transactionID;
    public StateEnum state;

    public SagaAnswer(String transactionID, StateEnum state) {
        this.transactionID = transactionID;
        this.state = state;
    }
}
