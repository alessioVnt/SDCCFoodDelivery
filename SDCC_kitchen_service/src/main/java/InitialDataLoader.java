import dataSource.DataSource;

public class InitialDataLoader {

    private String KEYSPACE_NAME = "sdcc_kitchen_event_db";
    private String TABLE_NAME = "kitchen_event";

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
        String query = "CREATE TABLE IF NOT EXISTS " +
                this.KEYSPACE_NAME + "." +
                this.TABLE_NAME + "(" +
                "event_id uuid PRIMARY KEY, " +
                "transaction_id uuid," +
                "price float," +
                "menu_items text);";
        DataSource dataSource = new DataSource();
        dataSource.getSession().execute(query);
    }
}
