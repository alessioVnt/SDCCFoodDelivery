import dataSource.DataSource;
import service.PaymentService;

public class InitialDataLoader {

    private String KEYSPACE_NAME = "sdcc_payment_event_db";
    private String TABLE_NAME = "payment_event";

    public void createDB() {
        this.createKeyspace();
        this.createTable();
    }

    private void createKeyspace() {
        String query = "CREATE KEYSPACE IF NOT EXISTS " +
                this.KEYSPACE_NAME + " WITH replication = {" +
                "'class':'" + "SimpleStrategy" +
                "','replication_factor':" + 1 +
                "};";
        DataSource dataSource = new DataSource();
        dataSource.getSession().execute(query);
    }

    private void createTable() {
        String query =
                " CREATE TABLE IF NOT EXISTS " +
                        this.KEYSPACE_NAME + "." +
                this.TABLE_NAME + " (" +
                "event_id timeuuid PRIMARY KEY, " +
                "transaction_id timeuuid," +
                "status text," +
                "user_id text," +
                "price float);";
        DataSource dataSource = new DataSource();
        dataSource.getSession().execute(query);
    }

    public static void main(String[] args){
        //Init DB
        InitialDataLoader initialDataLoader = new InitialDataLoader();
        initialDataLoader.createDB();

        //Init Kafka consumer
        PaymentService paymentService = new PaymentService();
        paymentService.kafkaSubscriber();

    }
}
