package sagaMessages;

import constants.StateEnum;

public class FinalizeOrderMessage {

    public StateEnum state;
    public String transactionID;

    public FinalizeOrderMessage(StateEnum state, String transactionID) {

        this.state = state;
        this.transactionID = transactionID;
    }
}
