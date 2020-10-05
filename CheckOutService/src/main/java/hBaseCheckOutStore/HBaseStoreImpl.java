package hBaseCheckOutStore;

import interfaces.IHBaseStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.Put;
import java.io.IOException;
import java.util.*;
import java.text.SimpleDateFormat;

public class HBaseStoreImpl implements IHBaseStore {

    protected static final String NAMESPACE_NAME = "PersistentOrder";
    static final TableName TABLE_NAME = TableName.valueOf("OrderTable");
    static final byte[] COLUMN_TIMESTAMP_NAME = Bytes.toBytes("Timestamp");
    static final byte[] COLUMN_USERID_NAME = Bytes.toBytes("UserID");
    static final byte[] COLUMN_RESTAURANTID_NAME = Bytes.toBytes("RestaurantID");
    static final byte[] COLUMN_ORDER_NAME = Bytes.toBytes("Order");
    static final byte[] COLUMN_TOTALPRICE_NAME = Bytes.toBytes("TotalPrice");

    static Connection connection;


    public void initialize() {
        System.out.println("Initializing Hbase..");
        Configuration config = HBaseConfiguration.create();

        //String path = "../hbase-site.xml";
        String path = this.getClass()
        .getClassLoader()
        .getResource("hbase-site.xml")
        .getPath();
        config.addResource(new Path(path));
       
        /*Iterator it = config.iterator();
        while (it.hasNext()) {
             System.out.println(it.next());
        }*/

        try (Connection connection = ConnectionFactory.createConnection();
             Admin admin = connection.getAdmin()) {


            admin.getClusterStatus(); // assure connection successfully established
            System.out.println("\n*** Hello HBase! -- Connection has been "
                    + "established via Zookeeper!!\n");

            this.createNamespaceAndTable(admin);

        } catch (IOException e) {
            e.printStackTrace();
            }

        System.out.println("End of Hbase's initialization");
        try {
            connection = ConnectionFactory.createConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void createNamespaceAndTable(Admin admin) throws IOException {
        if (!nameSpaceExists(admin, NAMESPACE_NAME)) {
            System.out.println("Creating Namespace [" + NAMESPACE_NAME + "].");

            admin.createNamespace(NamespaceDescriptor
                    .create(NAMESPACE_NAME).build());
        }
        if (!admin.tableExists(TABLE_NAME)) {
            System.out.println("Creating Table [" + TABLE_NAME.getNameAsString() + "and colums family");

            admin.createTable(new HTableDescriptor(TABLE_NAME)
                    .addFamily(new HColumnDescriptor(COLUMN_TIMESTAMP_NAME))
            .addFamily(new HColumnDescriptor(COLUMN_USERID_NAME))
            .addFamily(new HColumnDescriptor(COLUMN_RESTAURANTID_NAME))
            .addFamily(new HColumnDescriptor(COLUMN_ORDER_NAME))
            .addFamily(new HColumnDescriptor(COLUMN_TOTALPRICE_NAME)));
        }



    }

    public synchronized  void putRowToTable(String userId, String restaurantId, String order, Double price) throws IOException {
        //Connection connection = ConnectionFactory.createConnection();
        Table table = connection.getTable(TABLE_NAME);

        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());

        Put p = new Put(Bytes.toBytes(timeStamp));

        p.add(COLUMN_USERID_NAME, COLUMN_USERID_NAME, Bytes.toBytes(userId));

        p.add(COLUMN_RESTAURANTID_NAME, COLUMN_RESTAURANTID_NAME, Bytes.toBytes(restaurantId));

        p.add(COLUMN_ORDER_NAME, COLUMN_ORDER_NAME,
                Bytes.toBytes(order));

        p.add(COLUMN_TOTALPRICE_NAME,
                COLUMN_TOTALPRICE_NAME, Bytes.toBytes(price));

        table.put(p);

        System.out.println("Row added correctly \n");
    }

    public ArrayList<Double> getPriceContent() throws IOException {
        Connection connection = ConnectionFactory.createConnection();
        Table table = connection.getTable(TABLE_NAME);
        ArrayList<Double> price = new ArrayList<>();

        Scan scan = new Scan();
        //scan.setCaching(256);
        scan.addFamily(COLUMN_TOTALPRICE_NAME);

        ResultScanner resultScanner = table.getScanner(scan);
        Iterator<Result> iterator = resultScanner.iterator();
        while (iterator.hasNext())
        {
            Result next = iterator.next();
            for(Map.Entry<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> columnFamilyMap : next.getMap().entrySet())
            {
                for (Map.Entry<byte[], NavigableMap<Long, byte[]>> entryVersion : columnFamilyMap.getValue().entrySet())
                {
                    for (Map.Entry<Long, byte[]> entry : entryVersion.getValue().entrySet())
                    {
                        String row = Bytes.toString(next.getRow());
                        String column = Bytes.toString(entryVersion.getKey());
                        byte[] value = entry.getValue();
                        System.out.println(row+ " " + column + " " + Bytes.toString(value));
                        price.add(Bytes.toDouble(value));
                    }
                }
            }
        }

        resultScanner.close();
        return price;
    }

    public boolean nameSpaceExists(final Admin admin, final String namespaceName)  throws IOException {
        try {
            admin.getNamespaceDescriptor(namespaceName);
        } catch (NamespaceNotFoundException e) {
            return false;
        }
        return true;
    }

    private ArrayList<Double> findUserIDOutgoing(final Table table, String userId) throws IOException{
        ArrayList<Double> outgoing = new ArrayList<Double>();
        Filter filter = new SingleColumnValueFilter(COLUMN_USERID_NAME,
               COLUMN_USERID_NAME, CompareFilter.CompareOp.EQUAL, Bytes.toBytes(userId));
        Scan scan = new Scan();
        scan.setFilter(filter);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> iterator = rs.iterator();
        while (iterator.hasNext())
        {
            Result next = iterator.next();
            String row = Bytes.toString(next.getRow());
            String column = Bytes.toString(COLUMN_TOTALPRICE_NAME);
            byte[] value = next.getValue(COLUMN_TOTALPRICE_NAME, COLUMN_TOTALPRICE_NAME);
            System.out.println(row+ " " + column + " " + Bytes.toDouble(value));
            outgoing.add(Bytes.toDouble(value));
        }

        return outgoing;

    }

    public Double getMeanOutgoingFromUser(String userId) throws IOException {
        Connection connection = ConnectionFactory.createConnection();
        Table table = connection.getTable(TABLE_NAME);
        ArrayList<Double> outgoing = findUserIDOutgoing(table, userId);
        double sum = 0.0;
        for (int i = 0; i<outgoing.size(); i++){
            sum += outgoing.get(i);
        }

        Double mean = sum/(outgoing.size());

        System.out.println(mean);
        return mean;
    }

    public Double getMeanOutgoing() throws IOException{
        Connection connection = ConnectionFactory.createConnection();
        Table table = connection.getTable(TABLE_NAME);

        Double mean = 0.0;
        Double sum = 0.0;

        ArrayList<Double> outgoing = getPriceContent();
        for (int i = 0; i<outgoing.size(); i++){
            sum += outgoing.get(i);
        }

        mean = sum/(outgoing.size());

        System.out.println(mean);

        return mean;

    }
}

