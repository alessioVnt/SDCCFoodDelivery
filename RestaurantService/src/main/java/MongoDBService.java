import Controller.RestaurantServiceController;
import Entity.Restaurant;
import org.json.JSONArray;
import sdccFoodDelivery.RestaurantMenuItem;
import sdccFoodDelivery.TAG;

import java.util.ArrayList;
import java.util.List;

public class MongoDBService {

    private RestaurantServiceController restaurantServiceController;

    public MongoDBService(){
        this.restaurantServiceController = new RestaurantServiceController();
    }

    public ArrayList<Restaurant> getRestaurantInfoByName(String name){
        return restaurantServiceController.findRestaurantByName(name);
    }

    public ArrayList<Restaurant> getRestaurantInfoByCity(String city){
        return restaurantServiceController.findRestaurantByCity(city);
    }

    public ArrayList<Restaurant> getRestaurantInfoByAddress(String address){
        return restaurantServiceController.findRestaurantByAddress(address);
    }

    public ArrayList<Restaurant> getRestaurantInfoByTAG(String tag1){
        return restaurantServiceController.findRestaurantByTAG(tag1);
    }

    public ArrayList<Restaurant> getAllRestaurants(){
        return restaurantServiceController.getAllRestaurants();
    }

    public boolean addRestaurant(String name, String city, String address, TAG TAG, ArrayList<RestaurantMenuItem> menuItems){
        return restaurantServiceController.addRestaurant(name, city, address, TAG, null);
    }

    public boolean deleteRestaurant(String id){
        return restaurantServiceController.deleteRestaurant(id);
    }

    public boolean modifyRestaurant(String id, String name, String city, String address, TAG TAG){
        return restaurantServiceController.modifyRestaurant(id, name, city, address, TAG);
    }

    public Restaurant getRestaurantInfoById(String id){
        return restaurantServiceController.getRestaurantInfoById(id);
    }

    public boolean modifyRestaurantMenu(String id, List<RestaurantMenuItem> itemsToAdd){
        Restaurant toModify = getRestaurantInfoById(id);
        JSONArray restaurantMenu = toModify.menu;
        if (restaurantMenu == null) restaurantMenu = new JSONArray();
        for (RestaurantMenuItem menuItem: itemsToAdd) {

            String itemJson = "{ 'name' : " + menuItem.getName() +
                    ", 'description' : " + menuItem.getDescription() +
                    ", 'price' : " + menuItem.getPrice() + " }";

            restaurantMenu.put(itemJson);
        }
        return restaurantServiceController.modifyMenu(id, restaurantMenu);
    }
   /* private MongoClient mongo;
    private DB db;
    private DBCollection table;

    public MongoDBService() {
        this.mongo = new MongoClient("localhost", 27017);
        this.db = mongo.getDB("FoodDelivery");
        this.table = db.getCollection("Restaurant");

    }*/

}
