package model;

import java.util.ArrayList;

public class PaymentMethod {

    private ArrayList<CreditCard> creditCards;

    public PaymentMethod(){
        this.creditCards = new ArrayList<>();
    }

    public PaymentMethod(String creditCardNumber, String deadLine, String threeDigitCode) {
        this.creditCards = new ArrayList<>();
        this.creditCards.add(new CreditCard(creditCardNumber, deadLine, threeDigitCode));
    }

    public void addNewPaymentMethod(String creditCardNumber, String deadLine, String threeDigitCode){
        this.creditCards.add(new CreditCard(creditCardNumber, deadLine, threeDigitCode));
    }

    public ArrayList<CreditCard> getCreditCards() {
        return creditCards;
    }

    public void setCreditCards(ArrayList<CreditCard> creditCards) {
        this.creditCards = creditCards;
    }
}
