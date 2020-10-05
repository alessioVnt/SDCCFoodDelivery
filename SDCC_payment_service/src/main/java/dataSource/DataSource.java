package dataSource;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

//Class to get connection and session to Cassandra DB
public class DataSource {

    private String ROOT_ADDRESS = "cassandra";
    private Integer ROOT_PORT = 9042;
    private static Cluster cluster;
    private static Session session = null;

    //Get session to provide DB access and query to other classes
    public Session getSession() {
        if (session == null){
            Cluster.Builder b = Cluster.builder().addContactPoint(this.ROOT_ADDRESS);
            if (this.ROOT_PORT != null) {
                b.withPort(this.ROOT_PORT);
            }
            cluster = b.build();
            session = cluster.connect();
            return session;
        }
        else return session;
    }

    //Shutdown the connection
    public void close() {
        session.close();
        cluster.close();
    }
}
