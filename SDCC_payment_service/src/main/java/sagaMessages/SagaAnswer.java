package sagaMessages;

import constants.StateEnum;

import java.util.UUID;

public class SagaAnswer {
    public UUID transactionID;
    public StateEnum state;

    public SagaAnswer(UUID transactionID, StateEnum state) {
        this.transactionID = transactionID;
        this.state = state;
    }
}
