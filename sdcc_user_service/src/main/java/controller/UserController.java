package controller;

import com.google.gson.Gson;
import dataSource.DataSource;
import model.PaymentMethod;
import model.User;

import javax.xml.crypto.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserController {

    public UserController(){
        super();
    }

    //Retrieve user by ID
    @SuppressWarnings("Duplicates")
    public User findByID(int ID){
        DataSource dataSource = new DataSource();
        try {
            PreparedStatement statement = dataSource.getConnection().prepareStatement("SELECT * FROM users WHERE users.id = ?;");
            statement.setInt(1, ID);
            ResultSet resultSet = statement.executeQuery();
            dataSource.closeConnection();
            if (resultSet.next()){
                Gson gson = new Gson();
                return new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5), gson.fromJson(resultSet.getString(6), PaymentMethod.class) , gson.fromJson(resultSet.getString(7), String[].class));
            }
            else {
              System.out.println("NO USER FOUND!!");
              return null;  
            } 
        } catch (SQLException e) {
            System.out.println("Database Exception: " + e);
            return null;
        }
    }

    //Retrieve user by mail
    @SuppressWarnings("Duplicates")
    public User findByMail(String mail){
        DataSource dataSource = new DataSource();
        try{
            PreparedStatement statement = dataSource.getConnection().prepareStatement("SELECT * FROM users WHERE users.mail = ?;");
            statement.setString(1, mail);
            ResultSet resultSet = statement.executeQuery();
            dataSource.closeConnection();
            if (resultSet.next()){
                Gson gson = new Gson();
                return new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5), gson.fromJson(resultSet.getString(6), PaymentMethod.class) , gson.fromJson(resultSet.getString(7), String[].class));
            }
            else {
                System.out.println("NO USER FOUND!!");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Database Exception: " + e);
            return null;
        }
    }

    //Delete users by ID
    public boolean deleteByID(int ID){
        try {
            DataSource db = new DataSource();
            PreparedStatement statement = db.getConnection().prepareStatement("DELETE FROM users WHERE users.id = ?");
            statement.setInt(1, ID);
            statement.executeUpdate();
            db.closeConnection();
            return true;
        } catch (SQLException e) {
            System.out.println("Database Exception");
            return false;
        }
    }

    public boolean updateUser(User newUser){
        Gson gson = new Gson();
        DataSource dataSource = new DataSource();
        try{
            PreparedStatement statement = dataSource.getConnection().prepareStatement("UPDATE users SET username = ?, password = ?, address = ?, mail = ?, paymentMethod = ?, preferiti = ? WHERE id = ?;");
            statement.setString(1, newUser.getUsername());
            statement.setString(2, newUser.getPassword());
            statement.setString(3, newUser.getAddress());
            statement.setString(4, newUser.getMail());
            statement.setString(5, gson.toJson(newUser.getPaymentMethod()));
            statement.setString(6, gson.toJson(newUser.getPreferiti()));
            statement.setInt(7, newUser.getID());
            statement.executeUpdate();
            statement.close();
            return true;
        } catch (SQLException e){
            System.out.println("Database Exception: " + e);
            return false;
        }
    }

    //Save a new user into DB
    public boolean save(User user) {
        Gson gson = new Gson();;
        DataSource dataSource = new DataSource();
        try {
            PreparedStatement statement = dataSource.getConnection().prepareStatement("INSERT INTO users (username, password, address, mail, paymentMethod, preferiti)\n" +
                    "VALUES (?, ?, ?, ?, ?, ?);");
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getAddress());
            statement.setString(4, user.getMail());
            statement.setString(5, gson.toJson(user.getPaymentMethod()));
            statement.setString(6, gson.toJson(user.getPreferiti()));
            statement.executeUpdate();
            statement.close();
            return true;
        } catch (SQLException e) {
            System.out.println("Database Exception");
            return false;
        }
    }

}
