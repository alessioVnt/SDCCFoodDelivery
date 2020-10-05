import Controller.RestaurantServiceController;
import org.json.JSONArray;
import sdccFoodDelivery.TAG;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.io.File;

public class InitDataLoader {

    private RestaurantServiceController restaurantServiceController = new RestaurantServiceController();
    private ArrayList<String> cities =  new ArrayList<String>(
            Arrays.asList("Milano",
                    "Torino",
                    "Firenze", "Roma", "Napoli", "Palermo"));

    private ArrayList<String> addresses =  new ArrayList<String>(
            Arrays.asList("Via prova",
                    "Piazzale della prova",
                    "Viale delle province", "Viale dei condotti", "Via montenapoleone", "Piazza prova",
                    "Via Roma", "Via Lazio", "Viale Lombardia", "Piazza Campania", "Via Sardegna", "Piazzale Piemonte"));

    private static ArrayList<String> TAGS =  new ArrayList<String>(
            Arrays.asList("Pizza",
                    "Sushi",
                    "Dessert", "Poke", "Gelato", "Hamburger",
                    "Kebab", "Sandwich", "Americano", "Italiano", "Cinese", "Indiano", "Greco", "", ""));

    private int min = 0;
    private int maxCities = cities.size()-1;
    private int maxAddr = addresses.size()-1;
    private int maxTag = TAGS.size()-1;


    public List<String> readFile(){
        //String path = this.getClass().getClassLoader().getResource("Restaurant.txt").getPath();
        //File file = new File("src/main/resources/Restaurant.txt");
        InputStream is = this.getClass().getResourceAsStream("Restaurant.txt");
        List<String> restaurants = new ArrayList<String>();


        //
        // Create a new Scanner object which will read the data
        // from the file passed in. To check if there are more
        // line to read from it we check by calling the
        // scanner.hasNextLine() method. We then read line one
        // by one till all line is read.
        //
        Scanner scanner = new Scanner(is);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            //store this line to string [] here
            line = line.replace("\"", "");
            restaurants.add(line);
        }

        return restaurants;
    }


    public void fillDB(){
        List<String> restaurants = new ArrayList<String>();
        TAG TAG;
        restaurants = readFile();

        for(int i = 0; i < restaurants.size(); i++){

            JSONArray tag = new JSONArray();

            int cityIndex = (int) Math.floor(Math.random() * (maxCities - min + 1) + min);
            int addrIndex = (int) Math.floor(Math.random() * (maxAddr - min + 1) + min);
            for (int j = 0; j < 3; j++){
                int TAGIndex = (int) Math.floor(Math.random() * (maxTag - min + 1) + min);

                tag.put(TAGS.get(TAGIndex));
            }

            TAG = sdccFoodDelivery.TAG.newBuilder().setTag1(String.valueOf(tag.get(0))).
                    setTag2(String.valueOf(tag.get(1))).setTag3(String.valueOf(tag.get(2))).build();


            restaurantServiceController.addRestaurant(restaurants.get(i), cities.get(cityIndex), addresses.get(addrIndex), TAG, null);

        }
    }

}

