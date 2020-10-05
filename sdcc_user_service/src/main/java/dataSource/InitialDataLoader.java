package dataSource;

import controller.UserController;
import dataSource.DataSource;
import encoder.Encoder;
import model.PaymentMethod;
import model.User;

import java.sql.*;

import static dataSource.DataSource.ROOT_PASSWORD;
import static dataSource.DataSource.ROOT_USERNAME;

public class InitialDataLoader {

    private UserController userController = new UserController();

    //Creating DB for SDCC user
    public void createDB() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://postgres:5432/", ROOT_USERNAME, ROOT_PASSWORD);
        Statement statement = con.createStatement();
        statement.execute("CREATE DATABASE SDCC_User\n" +
                "    WITH \n" +
                "    OWNER = " + ROOT_USERNAME + "\n" +
                "    ENCODING = 'UTF8'\n" +
                "    CONNECTION LIMIT = -1;");
        statement.close();
        this.createUsers();
    }

        public void createUsers() throws SQLException {
        Encoder encoder = new Encoder();
        DataSource dataSource = new DataSource();
        Statement statement = dataSource.getConnection().createStatement();
        statement.executeUpdate("CREATE TABLE users(\n" +
                "   id SERIAL NOT NULL,\n" +
                "   username VARCHAR(255),\n" +
                "   password VARCHAR(255),\n" +
                "   address VARCHAR(255),\n" +
                "   mail VARCHAR(255),\n" +
                "   paymentMethod VARCHAR(1024),\n" +
                "   preferiti VARCHAR(255),\n" +
                "   PRIMARY KEY (id));");
        statement.close();
        this.userController.save(new User("Luca Menzolini",encoder.encode("password"), "Via prova","luca.menzolini@gmail.com", new PaymentMethod("prova","prova","prova")));
        this.userController.save(new User("Alessio Vintari",encoder.encode("password"), "Via prova","alessio.vintari@gmail.com", new PaymentMethod("prova","prova","prova")));
        this.userController.save(new User("Luca Tabacchino",encoder.encode("password"), "Via prova","luca.tabacchino@gmail.com", new PaymentMethod("prova","prova","prova")));
        this.userController.save(new User("Admin",encoder.encode("admin"), null ,"Admin@mail.it", new PaymentMethod()));
    }
}
