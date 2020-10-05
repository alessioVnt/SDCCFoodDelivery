import Entity.Restaurant;
import com.google.protobuf.Descriptors;
import com.mongodb.Mongo;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.json.JSONArray;
import org.json.JSONObject;
import sdccFoodDelivery.RestaurantMessage;
import sdccFoodDelivery.*;
import sdccFoodDelivery.restaurantServiceGrpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;



public class RestaurantServiceServer {

    private static final Logger logger = Logger.getLogger(RestaurantServiceServer.class.getName());

    private Server server;


    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        port = Integer.parseInt(System.getenv("PORT"));
        server = ServerBuilder.forPort(port)
                .addService(new RestaurantImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    RestaurantServiceServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final RestaurantServiceServer server = new RestaurantServiceServer();
        InitDataLoader initDataLoader = new InitDataLoader();
        initDataLoader.fillDB();
        server.start();
        server.blockUntilShutdown();
    }

    static class RestaurantImpl extends restaurantServiceGrpc.restaurantServiceImplBase {

        private void generateResponse(StreamObserver<RestaurantMessage> responseObserver, ArrayList<Restaurant> restaurants) {
            for(int i = 0; i< restaurants.size(); i++){
                JSONArray tags = restaurants.get(i).getTAG();
                TAG tagMessage = TAG.newBuilder().setTag1(String.valueOf(tags.get(0))).setTag2(String.valueOf(tags.get(1))).
                        setTag3(String.valueOf(tags.get(2))).build();
                JSONArray menu = restaurants.get(i).getMenu();


                RestaurantMessage.Builder responseBuilder = RestaurantMessage.newBuilder().setId(restaurants.get(i).getId()).
                        setName(restaurants.get(i).getName()).setCity(restaurants.get(i).getCity()).
                        setAddress(restaurants.get(i).getAddress()).setTAG(tagMessage);

                RestaurantMessage response;

                if (menu != null){
                    ArrayList<RestaurantMenuItem> menuItems = new ArrayList<>();
                    for(int j = 0; j<menu.length(); j++){
                        menuItems.add(parseMenuItemFromJson(String.valueOf(menu.get(j))));
                    }
                    response = responseBuilder.addAllMenuItems(menuItems).build();
                } else {
                    response = responseBuilder.build();
                }

                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        }

        private RestaurantMenuItem parseMenuItemFromJson(String itemString){
            JSONObject jsonObject = new JSONObject(itemString);
            RestaurantMenuItem restaurantMenuItem = RestaurantMenuItem.newBuilder().
                    setName(jsonObject.getString("name")).
                    setDescription(jsonObject.getString("description")).
                    setPrice(Double.parseDouble(jsonObject.get("price").toString())).
                    build();
            return  restaurantMenuItem;
        }

        @Override
        public void getRestaurantInfoByName(RestaurantRequestName request, StreamObserver<RestaurantMessage> responseObserver) {
            MongoDBService mongoDBService = new MongoDBService();
            ArrayList<Restaurant> restaurants = mongoDBService.getRestaurantInfoByName(request.getName());

            generateResponse(responseObserver, restaurants);

        }

        @Override
        public void getRestaurantInfoByAddress(RestaurantRequestAddress request, StreamObserver<RestaurantMessage> responseObserver) {
            MongoDBService mongoDBService = new MongoDBService();
            ArrayList<Restaurant> restaurants = mongoDBService.getRestaurantInfoByAddress(request.getAddress());

            generateResponse(responseObserver, restaurants);
        }

        @Override
        public void getRestaurantInfoByCity(RestaurantRequestCity request, StreamObserver<RestaurantMessage> responseObserver) {
            MongoDBService mongoDBService = new MongoDBService();
            ArrayList<Restaurant> restaurants = mongoDBService.getRestaurantInfoByCity(request.getCity());

            generateResponse(responseObserver, restaurants);
        }

        @Override
        public void getRestaurantInfoByTAG(RestaurantRequestTAG request, StreamObserver<RestaurantMessage> responseObserver) {
            MongoDBService mongoDBService = new MongoDBService();
            ArrayList<Restaurant> restaurants = mongoDBService.getRestaurantInfoByTAG(request.getTAG().getTag1());

            generateResponse(responseObserver, restaurants);
        }

        @Override
        public void getRestaurantMenu(RestaurantRequestID request, StreamObserver<RestaurantMessageMenu> responseObserver) {
            MongoDBService mongoDBService = new MongoDBService();
            Restaurant restaurant = mongoDBService.getRestaurantInfoById(request.getId());

            JSONArray menu = restaurant.getMenu();
            for (int i = 0; i < menu.length(); i++) {
                JSONObject object = (JSONObject) menu.get(i);
                String type = object.getString("type");
                JSONArray menuItem = object.getJSONArray("menu-items");
                for (int j = 0; j < menuItem.length(); j++) {
                    JSONObject dish = (JSONObject) menuItem.get(j);
                    String name = dish.getString("name");
                    String description = dish.getString("description");
                    Double price = dish.getDouble("price");
                    RestaurantMenuItem menuItemMessage = RestaurantMenuItem.newBuilder().setName(name).setDescription(description).setPrice(price).build();
                    RestaurantMessageMenu response = RestaurantMessageMenu.newBuilder().setType(type).setMenuItem(menuItemMessage).
                            build();
                    responseObserver.onNext(response);
                }

            }
            responseObserver.onCompleted();
        }

        @Override
        public void getAllRestaurants(RestaurantsRequest request, StreamObserver<RestaurantMessage> responseObserver) {

            MongoDBService mongoDBService = new MongoDBService();
            ArrayList<Restaurant> restaurants = mongoDBService.getAllRestaurants();

            generateResponse(responseObserver, restaurants);
        }

        @Override
        public void getRestaurantInfoById(RestaurantRequestID request, StreamObserver<RestaurantMessage> responseObserver) {
            System.out.println("ID of desired restaurant: " + request.getId());
            logger.info("ID of desired restaurant: " + request.getId());
            MongoDBService mongoDBService = new MongoDBService();
            Restaurant restaurant = mongoDBService.getRestaurantInfoById(request.getId());
            JSONArray menu = restaurant.getMenu();

            JSONArray tags = restaurant.getTAG();
            TAG tagMessage = TAG.newBuilder().setTag1(String.valueOf(tags.get(0))).setTag2(String.valueOf(tags.get(1))).
                    setTag3(String.valueOf(tags.get(2))).build();




            RestaurantMessage.Builder responseBuilder = RestaurantMessage.newBuilder().setId(restaurant.getId()).
                    setName(restaurant.getName()).setCity(restaurant.getCity()).
                    setAddress(restaurant.getAddress()).setTAG(tagMessage);

            RestaurantMessage response;

            if (menu != null){
                ArrayList<RestaurantMenuItem> menuItems = new ArrayList<>();
                for(int j = 0; j<menu.length(); j++){
                    menuItems.add(parseMenuItemFromJson(String.valueOf(menu.get(j))));
                }
                response = responseBuilder.addAllMenuItems(menuItems).build();
            } else {
                response = responseBuilder.build();
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }


       /*@Override
        public void getRestaurantInfoById(RestaurantRequestID request, StreamObserver<RestaurantMessage> responseObserver) {
            MongoDBService mongoDBService = new MongoDBService();

            Restaurant restaurant= mongoDBService.getRestaurantInfoById(request.getId());
            RestaurantMessage response = RestaurantMessage.newBuilder().setId(restaurant.getId()).setName(restaurant.getName())
                    .setCity(restaurant.getCity()).setAddress(restaurant.getAddress()).setTAG(restaurant.getTAG()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }*/

        @Override
        public void deleteRestaurant(DeleteRestaurantRequest request, StreamObserver<CommitOK> responseObserver) {
            MongoDBService mongoDBService = new MongoDBService();

            boolean ok = mongoDBService.deleteRestaurant(request.getId());
            CommitOK response = CommitOK.newBuilder().setOk(ok).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void modifyRestaurant(ModifyRestaurantRequest request, StreamObserver<CommitOK> responseObserver) {
            MongoDBService mongoDBService = new MongoDBService();

            boolean ok = mongoDBService.modifyRestaurant(request.getId(), request.getName(), request.getCity(), request.getAddress(),
                    request.getTAG());
            CommitOK response = CommitOK.newBuilder().setOk(ok).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void addRestaurant(AddRestaurantRequest request, StreamObserver<CommitOK> responseObserver) {
            MongoDBService mongoDBService = new MongoDBService();

           boolean ok = mongoDBService.addRestaurant(request.getName(), request.getCity(), request.getAddress(), request.getTAG(), null);
           CommitOK response = CommitOK.newBuilder().setOk(ok).build();
           responseObserver.onNext(response);
           responseObserver.onCompleted();
        }

        @Override
        public void modifyRestaurantMenu(ModifyRestaurantMenuRequest request, StreamObserver<CommitOK> responseObserver) {
            MongoDBService mongoDBService = new MongoDBService();

            boolean ok = mongoDBService.modifyRestaurantMenu(request.getId(), request.getItemsToAddList());
            CommitOK response = CommitOK.newBuilder().setOk(ok).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
