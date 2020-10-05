package Entity;

import com.google.gson.JsonArray;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Restaurant {
    private String id;
    private String name;
    private String city;
    private String address;
    private JSONArray TAG;
    public JSONArray menu;

    public Restaurant(String name, String city, String address, JSONArray TAG) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.TAG = TAG;
    }

    public Restaurant(JSONObject restaurantJSON){
        this.id = restaurantJSON.getJSONObject("_id").getString("$oid");
        this.name = restaurantJSON.getString("name");
        this.city = restaurantJSON.getString("city");
        this.address = restaurantJSON.getString("address");
        this.TAG = restaurantJSON.getJSONArray("TAG");
        if (restaurantJSON.keySet().contains("menu")){
            this.menu = restaurantJSON.getJSONArray("menu");
        }

    }

    public String getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = String.valueOf(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public JSONArray getTAG() {
        return TAG;
    }

    public void setTAG(JSONArray TAG) {
        this.TAG = TAG;
    }

    public JSONArray getMenu() {
        return menu;
    }

    public void setMenu(JSONArray menu) {
        this.menu = menu;
    }
}
