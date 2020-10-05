package model;

import java.util.ArrayList;

public class User {
    final private int PREFERITI_SIZE = 3;
    private int ID;
    private String username;
    private String password;
    private String address;
    private String mail;
    private PaymentMethod paymentMethod;
    private String[] preferiti;

    //Create a new born user
    public User(String username, String password, String address, String mail, PaymentMethod paymentMethod) {
        this.username = username;
        this.password = password;
        this.address = address;
        this.mail = mail;
        this.paymentMethod = paymentMethod;
        this.preferiti = new String[this.PREFERITI_SIZE];
        for (int I = 0; I < this.PREFERITI_SIZE; I++) this.preferiti[I] = "0";
    }

    //User instance read from DB
    public User(int ID, String username, String password, String address, String mail, PaymentMethod paymentMethod, String[] preferiti){
        this.ID = ID;
        this.username = username;
        this.password = password;
        this.address = address;
        this.mail = mail;
        this.paymentMethod = paymentMethod;
        this.preferiti = preferiti;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String[] getPreferiti() {
        return preferiti;
    }

    public void setPreferiti(String[] preferiti) {
        this.preferiti = preferiti;
    }

    public int getID() {
        return ID;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
