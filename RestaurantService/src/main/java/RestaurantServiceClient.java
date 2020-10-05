import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.json.JSONArray;
import sdccFoodDelivery.*;
import sdccFoodDelivery.restaurantServiceGrpc;

import java.util.Iterator;

public class RestaurantServiceClient {
    final restaurantServiceGrpc.restaurantServiceBlockingStub blockingStub;

    public RestaurantServiceClient(ManagedChannel channel) {
        this.blockingStub = restaurantServiceGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC client");
        final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        final RestaurantServiceClient client = new RestaurantServiceClient(channel);
        client.run(channel);
    }

    private void run(ManagedChannel channel) {
        String name = "pizza express";

        String address = "piazza annibaliano";

        String city = "roma";

        JSONArray TAG = new JSONArray();

        TAG.put("pizza");
        TAG.put("fast food");
        TAG.put("");


        //addRestaurant(name, city, address, TAG);

        //deleteRestaurant("5eafeb2d71b17d077c0705fa");

        //modifyRestaurant("5eae9e52f1921916285c87d2", name, "milano", address, TAG);

        //findRestaurantByName("sora lella");

        //findRestaurantByAddress(address);

        findAllRestaurants();

        //findRestaurantMenu("5eb2cb479618ca244c20e3ea");

        //getRestaurantInfoById("5eb2cb479618ca244c20e3ea");

        //findRestaurantByTAG(TAG);

        System.out.println("Shutting down channel");
        channel.shutdown();

    }

    private void findRestaurantByName(String name) {

        // Server Streaming
        // we prepare the request
        final RestaurantRequestName restaurantRequestName =
                RestaurantRequestName.newBuilder()
                        .setName(name)
                        .build();

        // we stream the responses (in a blocking manner)
        Iterator<RestaurantMessage> restaurantMessageIterator = blockingStub.getRestaurantInfoByName(restaurantRequestName);
        if (!restaurantMessageIterator.hasNext()) {
            System.out.println("Restaurant not found");
        } else {
            restaurantMessageIterator.forEachRemaining(RestaurantMessage -> {
                System.out.println("Menu: \n" +
                        "id: " + RestaurantMessage.getId() + "\nname: " + RestaurantMessage.getName() +
                        "\ncity: " + RestaurantMessage.getCity() + "\naddress: " + RestaurantMessage.getAddress()
                        + "\nTAG:\n" +
                        RestaurantMessage.getTAG().getTag1() + "\n" +
                        RestaurantMessage.getTAG().getTag2() + "\n" +
                RestaurantMessage.getTAG().getTag3());
            });
        }
    }

    private void getRestaurantInfoById(String id) {
        final RestaurantRequestID restaurantRequestID =
                RestaurantRequestID.newBuilder()
                        .setId(id)
                        .build();

        RestaurantMessage response;
        try {
            response = blockingStub.getRestaurantInfoById(restaurantRequestID);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Found restaurant!\n" + "id: " + response.getId() +
                "\nName: " + response.getName() + "\ncity: " + response.getCity() +
                "\nAddress: " + response.getAddress() + "\nTAG: " + response.getTAG());


    }

    private void findRestaurantMenu(String id) {

        // Server Streaming
        // we prepare the request
        final RestaurantRequestID restaurantRequestID =
                RestaurantRequestID.newBuilder()
                        .setId(id)
                        .build();
        System.out.println("Menu: \n");
        // we stream the responses (in a blocking manner)
        blockingStub.getRestaurantMenu(restaurantRequestID)
                .forEachRemaining(RestaurantMessageMenu -> {
                    System.out.println("type: " + RestaurantMessageMenu.getType() + "\nname: " + RestaurantMessageMenu.getMenuItem().getName() +
                            "\ndescription: " + RestaurantMessageMenu.getMenuItem().getDescription() +
                            "\nprice: " + RestaurantMessageMenu.getMenuItem().getPrice() + "\n");
                });
    }

    private void findRestaurantByCity(String city) {

        // Server Streaming
        // we prepare the request
        final RestaurantRequestCity restaurantRequestCity =
                RestaurantRequestCity.newBuilder()
                        .setCity(city)
                        .build();
        // we stream the responses (in a blocking manner)
        Iterator<RestaurantMessage> restaurantMessageIterator = blockingStub.getRestaurantInfoByCity(restaurantRequestCity);
        if (!restaurantMessageIterator.hasNext()) {
            System.out.println("Restaurant not found!");
        } else {
            restaurantMessageIterator.forEachRemaining(RestaurantMessage -> {
                System.out.println("Founded Restaurant: \n" +
                        "id: " + RestaurantMessage.getId() + "\n name: " + RestaurantMessage.getName() +
                        "\n city: " + RestaurantMessage.getCity() + "\n address: " + RestaurantMessage.getAddress()
                        + "\n TAG: " + RestaurantMessage.getTAG());
            });
        }
    }

    private void findRestaurantByAddress(String address) {

        // Server Streaming
        // we prepare the request
        final RestaurantRequestAddress restaurantRequestAddress =
                RestaurantRequestAddress.newBuilder()
                        .setAddress(address)
                        .build();

        // we stream the responses (in a blocking manner)
        Iterator<RestaurantMessage> restaurantMessageIterator = blockingStub.getRestaurantInfoByAddress(restaurantRequestAddress);
        if (!restaurantMessageIterator.hasNext()) {
            System.out.println("Restaurant not found!");
        } else {
            restaurantMessageIterator.forEachRemaining(RestaurantMessage -> {
                System.out.println("Founded Restaurant: \n" +
                        "id: " + RestaurantMessage.getId() + "\n name: " + RestaurantMessage.getName() +
                        "\n city: " + RestaurantMessage.getCity() + "\n address: " + RestaurantMessage.getAddress()
                        + "\n TAG: " + RestaurantMessage.getTAG());
            });
        }
    }

    private void findRestaurantByTAG(JSONArray TAG) {

        // Server Streaming
        // we prepare the request
        final RestaurantRequestTAG restaurantRequestTAG =
                RestaurantRequestTAG.newBuilder()
                        .setTAG(sdccFoodDelivery.TAG.newBuilder().setTag1(String.valueOf(TAG.get(0))).setTag2(String.valueOf(TAG.get(1)))
                         .setTag3(String.valueOf(TAG.get(2))).build())
                        .build();

        // we stream the responses (in a blocking manner)
        Iterator<RestaurantMessage> restaurantMessageIterator = blockingStub.getRestaurantInfoByTAG(restaurantRequestTAG);
        if (!restaurantMessageIterator.hasNext()) {
            System.out.println("Restaurant not found!");
        } else {
            restaurantMessageIterator.forEachRemaining(RestaurantMessage -> {
                System.out.println("Founded Restaurant: \n" +
                        "id: " + RestaurantMessage.getId() + "\n name: " + RestaurantMessage.getName() +
                        "\n city: " + RestaurantMessage.getCity() + "\n address: " + RestaurantMessage.getAddress()
                        + "\n TAG: " + RestaurantMessage.getTAG());
            });
        }
    }

    private void findAllRestaurants(){

        //Server streaming
        //prepare the request
        final RestaurantsRequest restaurantsRequest =
                RestaurantsRequest.newBuilder().setMessage("all")
                        .build();

        //stream the responses(in a blocking manner)
        blockingStub.getAllRestaurants(restaurantsRequest)
                .forEachRemaining(RestaurantMessage -> {
                    System.out.println("Found Restaurant: \n" +
                            " id: " + RestaurantMessage.getId() + "\n name: " + RestaurantMessage.getName() +
                            "\n city: " + RestaurantMessage.getCity() + "\n address: " + RestaurantMessage.getAddress()
                            + "\n TAG: " + RestaurantMessage.getTAG());
                });
    }

    public void addRestaurant(String name, String city, String address, JSONArray tag){

        final AddRestaurantRequest addRestaurantRequest = AddRestaurantRequest.newBuilder().setName(name)
                .setCity(city)
                .setAddress(address)
                .setTAG(TAG.newBuilder().setTag1(String.valueOf(tag.get(0)))
                        .setTag2(String.valueOf(tag.get(1))).setTag3(String.valueOf(tag.get(2))).build())
                .build();

        CommitOK response;

        try{
            response = blockingStub.addRestaurant(addRestaurantRequest);
        } catch (StatusRuntimeException e){
            e.printStackTrace();
            return;
        }

        if(response.getOk()){
            System.out.println("Restaurant correctly inserted");
        }else {
            System.out.println("Restaurant uncorrectly inserted");
        }
    }

    public void deleteRestaurant(String id){

        final DeleteRestaurantRequest deleteRestaurantRequest = DeleteRestaurantRequest.newBuilder().setId(id).build();
        CommitOK response;

        try{
            response = blockingStub.deleteRestaurant(deleteRestaurantRequest);
        } catch (StatusRuntimeException e){
            e.printStackTrace();
            return;
        }
        if(response.getOk()){
            System.out.println("Restaurant correctly deleted");
        }else {
            System.out.println("Restaurant uncorrectly deleted");
        }
    }

    public void modifyRestaurant(String id, String name, String city, String address, JSONArray prova){

        final ModifyRestaurantRequest modifyRestaurantRequest = ModifyRestaurantRequest.newBuilder().setId(id)
                .setName(name)
                .setCity(city)
                .setAddress(address)
                .setTAG(TAG.newBuilder().setTag1(String.valueOf(prova.get(0)))
                        .setTag2(String.valueOf(prova.get(1))).setTag3(String.valueOf(prova.get(2))).build())
                .build();
        CommitOK response;

        try{
            response = blockingStub.modifyRestaurant(modifyRestaurantRequest);
        } catch (StatusRuntimeException e){
            e.printStackTrace();
            return;
        }
        if(response.getOk()){
            System.out.println("Restaurant correctly modified");
        }else {
            System.out.println("Restaurant uncorrectly modified");
        }
    }

}
