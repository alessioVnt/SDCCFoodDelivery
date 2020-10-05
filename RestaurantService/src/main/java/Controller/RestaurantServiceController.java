package Controller;

import Entity.Restaurant;
import com.mongodb.*;
import com.mongodb.util.JSON;
import io.grpc.StatusRuntimeException;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import sdccFoodDelivery.RestaurantMenuItem;
import sdccFoodDelivery.TAG;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.Arrays;

public class RestaurantServiceController {

    private MongoClient mongo;
    private DB db;
    private DBCollection table;

    public RestaurantServiceController() {
        MongoCredential credential = MongoCredential.createCredential("root", "admin", System.getenv("MONGODB_ROOT_PASSWORD").toCharArray());
        this.mongo = new MongoClient(new ServerAddress(System.getenv("MONGO_ADDR"), Integer.parseInt(System.getenv("MONGO_PORT"))), Arrays.asList(credential));
        this.db = mongo.getDB("FoodDelivery");
        this.table = db.getCollection("Restaurant");
    }

    public ArrayList<Restaurant> findRestaurantByName (String name){
        ArrayList<Restaurant> restaurants = new ArrayList<>();
        /**** Find and display ****/
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("name",  java.util.regex.Pattern.compile(name, Pattern.CASE_INSENSITIVE));

        DBCursor cursor = table.find(searchQuery);

        while (cursor.hasNext()) {
            String jsonString = cursor.next().toString();
            JSONObject obj = new JSONObject(jsonString);
            restaurants.add(new Restaurant(obj));
            //return cursor.next().toString();
        }
        return restaurants;
    }

    public ArrayList<Restaurant> findRestaurantByAddress (String address) {
        ArrayList<Restaurant> restaurants = new ArrayList<>();
        /**** Find and display ****/
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("address", address);
        DBCursor cursor = table.find(searchQuery);

        while (cursor.hasNext()) {
            String jsonString = cursor.next().toString();
            JSONObject obj = new JSONObject(jsonString);
            restaurants.add(new Restaurant(obj));
            //return cursor.next().toString();
        }
        return restaurants;

    }

    public ArrayList<Restaurant> findRestaurantByTAG (String tag1) {
        ArrayList<Restaurant> restaurants = new ArrayList<>();
        /**** Find and display ****/
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("TAG", tag1);
        DBCursor cursor = table.find(searchQuery);

        while (cursor.hasNext()) {
            String jsonString = cursor.next().toString();
            JSONObject obj = new JSONObject(jsonString);
            restaurants.add(new Restaurant(obj));
            //return cursor.next().toString();
        }
        return restaurants;
    }

    public Restaurant getRestaurantInfoById(String id) {
        /**** Find and display ****/
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", new ObjectId(id));
        DBCursor cursor = table.find(searchQuery);

        while (cursor.hasNext()) {
            String jsonString = cursor.next().toString();
            JSONObject obj = new JSONObject(jsonString);
            return (new Restaurant(obj));
        }
        return null;

    }

    public ArrayList<Restaurant> findRestaurantByCity (String city) {
        ArrayList<Restaurant> restaurants = new ArrayList<>();
        /**** Find and display ****/
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("city", city);
        DBCursor cursor = table.find(searchQuery);

        while (cursor.hasNext()) {
            String jsonString = cursor.next().toString();
            JSONObject obj = new JSONObject(jsonString);
            restaurants.add(new Restaurant(obj));
            //return cursor.next().toString();
        }
        return restaurants;

    }

    public ArrayList<Restaurant> getAllRestaurants() {
        ArrayList<Restaurant> restaurants = new ArrayList<>();
        BasicDBObject searchQuery = new BasicDBObject();
        /**** Find and display ****/
        DBCursor cursor = table.find(searchQuery);
        while (cursor.hasNext()) {
            String jsonString = cursor.next().toString();
            JSONObject obj = new JSONObject(jsonString);
            restaurants.add(new Restaurant(obj));
        }
        return restaurants;
    }

    public boolean addRestaurant(String name, String city, String address, TAG TAG, ArrayList<RestaurantMenuItem> menuItems){
        /**** Insert ****/
        // create a document to store key and value
        BasicDBObject document = new BasicDBObject();
        //document.put("id", restaurant.getId());
        JSONArray tag = new JSONArray();
        tag.put(TAG.getTag1());
        tag.put(TAG.getTag2());
        tag.put(TAG.getTag3());

        document.put("name", name);
        document.put("city", city);
        document.put("address", address);
        document.put("TAG", tag);

        //If menuItems is give not null add the menu if not, add the default item "Acqua liscia"
        if(menuItems != null){
            JSONArray menuJson = new JSONArray();

            for (RestaurantMenuItem menuItem: menuItems) {
                /*JSONObject itemJson = new JSONObject();
                itemJson.put("name", menuItem.getName());
                itemJson.put("description", menuItem.getDescription());
                itemJson.put("price", menuItem.getPrice());*/

                String itemJson = "{ 'name' : " + menuItem.getName() +
                        ", 'description' : " + menuItem.getDescription() +
                        ", 'price' : " + menuItem.getPrice() + " }";

                menuJson.put(itemJson);
            }

            document.put("menu", menuJson);
        } else {

            JSONArray menuJson = new JSONArray();

            /*BasicDBObject itemJson = new BasicDBObject();
            itemJson.put("name", "Acqua liscia");
            itemJson.put("description", "Della rinfrescante acqua sorgiva");
            itemJson.put("price", "2");*/
            String itemJson = "{ 'name' : 'Acqua liscia', 'description' : 'Della rinfrescante acqua sorgiva', 'price' : '2' }";

            menuJson.put(itemJson);

            document.put("menu", menuJson);
        }

        try{
            table.insert(document);
        }catch (StatusRuntimeException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteRestaurant(String id){
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", new ObjectId(id));

        try {
            table.remove(searchQuery);
        } catch (StatusRuntimeException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean modifyRestaurant (String id, String name, String city, String address, TAG TAG){
        /**** Find and display ****/
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.append("_id",  new ObjectId(id));
        JSONArray updateTag = new JSONArray();
        updateTag.put(TAG.getTag1());
        updateTag.put(TAG.getTag2());
        updateTag.put(TAG.getTag3());

        BasicDBObject newDocument = new BasicDBObject();
        newDocument.append("name", name);
        newDocument.append("city", city);
        newDocument.append("address", address);
        newDocument.append("TAG", updateTag);

        BasicDBObject updateObject = new BasicDBObject();
        updateObject.append("$set", newDocument);

        try {
            table.update(searchQuery, updateObject);
        } catch (StatusRuntimeException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean modifyMenu (String id, JSONArray newMenu){
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.append("_id",  new ObjectId(id));

        BasicDBObject newDocument = new BasicDBObject();
        newDocument.append("menu", newMenu);

        BasicDBObject updateObject = new BasicDBObject();
        updateObject.append("$set", newDocument);

        try {
            table.update(searchQuery, updateObject);
        } catch (StatusRuntimeException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
