package model;

public class CreditCard {

    private String creditCardNumber;
    private String deadLine;
    private String threeDigitCode;

    CreditCard(String creditCardNumber, String deadLine, String threeDigitCode){
        this.creditCardNumber = creditCardNumber;
        this.deadLine = deadLine;
        this.threeDigitCode = threeDigitCode;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(String deadLine) {
        this.deadLine = deadLine;
    }

    public String getThreeDigitCode() {
        return threeDigitCode;
    }

    public void setThreeDigitCode(String threeDigitCode) {
        this.threeDigitCode = threeDigitCode;
    }
}
