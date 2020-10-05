package main

import (
	"context"
	"fmt"
	"github.com/sirupsen/logrus"
	"io"

	pb "github.com/alessioVnt/frontend/pb"
)

//User service RPCs

func (fe *frontendServer) getUserByID(ctx context.Context, id int32) (*pb.UserMessage, error) {
	user, err := pb.NewSdccUserServiceClient(fe.userSvcConn).
		FindByID(ctx, &pb.IDMessage{
			Id: id,
		})
	if err != nil {
		print("Error in getting user")
		logrus.Infof("" + fmt.Sprint(err) + "\n")
		return nil, err
	}
	print("User found!" + "\n")
	userName := user.Username
	userAddress := user.Address
	userMail := user.Mail
	print(userName + "\n")
	print(userAddress + "\n")
	print(userMail + "\n")
	return user, err
}

func (fe *frontendServer) createUser(ctx context.Context, userName string, password string, mail string, address string, cardNumber string, cvc string, expiration string) (bool, error){
	response, err := pb.NewSdccUserServiceClient(fe.userSvcConn).
		Save(ctx, &pb.UserInfoMessage{
		Username:         userName,
		Password:         password,
		Address:          address,
		Mail:             mail,
		CreditCardNumber: cardNumber,
		Deadline:         expiration,
		ThreeDigitCode:   cvc,
	})
	if err != nil {
		return false, err
	}
	return response.Ok, err
}

func (fe *frontendServer) logIn(ctx context.Context, mail string, password string)(*pb.LogInResponse){
	login, err := pb.NewSdccUserServiceClient(fe.userSvcConn).
		LogIn(ctx, &pb.LogInMessage{
		Mail:     mail,
		Password: password,
	})
	if err != nil {
		print("Error in logging in\n")
		return nil
	}
	return login
}

func (fe *frontendServer) updatePreferences(ctx context.Context, id int32, newPreference string) error {
	isSuccessfull, err := pb.NewSdccUserServiceClient(fe.userSvcConn).
		UpdatePreferiti(ctx, &pb.UpdatePreferitiMessage{
			Id:           id,
			NewPreferito: newPreference,
		})
	if err != nil {
		print("Error in updating user preferences\n")
		return err
	}
	if isSuccessfull.Ok {
		print("User's preferences modified with success!")
	} else {
		print("User's preferences where not modified!")
	}
	return err
}

func (fe *frontendServer) updateUsername(ctx context.Context, id int32, newUsername string) (bool, error){
	isSuccessfull, err := pb.NewSdccUserServiceClient(fe.userSvcConn).
		UpdateUsername(ctx, &pb.UpdateUsernameMessage{
			Id:           id,
			NewUsername: newUsername,
		})
	if err != nil {
		print("Error in updating username\n")
		return false, err
	}
	if isSuccessfull.Ok {
		print("User's username modified with success!\n")
	} else {
		print("User's username where not modified!\n")
	}
	return isSuccessfull.Ok, err
}

func (fe *frontendServer) updatePassword(ctx context.Context, id int32, newPassword string) (bool, error)  {
	isSuccessfull, err := pb.NewSdccUserServiceClient(fe.userSvcConn).
		UpdatePassword(ctx, &pb.UpdatePasswordMessage{
			Id:           id,
			NewPassword: newPassword,
		})
	if err != nil {
		print("Error in updating password\n")
		return false, err
	}
	if isSuccessfull.Ok {
		print("User's password modified with success!\n")
	} else {
		print("User's password where not modified!\n")
	}
	return isSuccessfull.Ok, err
}

func (fe *frontendServer) updateMail(ctx context.Context, id int32, newMail string) (bool, error){
	isSuccessfull, err := pb.NewSdccUserServiceClient(fe.userSvcConn).
		UpdateMail(ctx, &pb.UpdateMailMessage{
			Id:           id,
			NewMail: newMail,
		})
	if err != nil {
		print("Error in updating mail\n")
		return false, err
	}
	if isSuccessfull.Ok {
		print("User's mail modified with success!\n")
	} else {
		print("User's mail where not modified!\n")
	}
	return isSuccessfull.Ok, err
}

func (fe *frontendServer) updateAddress(ctx context.Context, id int32, newAddress string) (bool, error){
	isSuccessfull, err := pb.NewSdccUserServiceClient(fe.userSvcConn).
		UpdateAddress(ctx, &pb.UpdateAddressMessage{
			Id:           id,
			NewAddress: newAddress,
		})
	if err != nil {
		print("Error in updating address\n")
		return false, err
	}
	if isSuccessfull.Ok {
		print("User's address modified with success!\n")
	} else {
		print("User's address where not modified!\n")
	}
	return isSuccessfull.Ok, err
}

func (fe *frontendServer) updateCreditCard(ctx context.Context, id int32, cardNumber string, cvc string, expiration string) (bool, error){
	isSuccessfull, err := pb.NewSdccUserServiceClient(fe.userSvcConn).
		UpdatePaymentMethod(ctx, &pb.UpdatePaymentMessage{
			Id:           id,
			CreditCard: &pb.CreditCard{
				CreditCardNumber: cardNumber,
				DeadLine:         expiration,
				ThreeDigitCode:   cvc,
			},
		})
	if err != nil {
		print("Error in updating payment method\n")
		return false, err
	}
	if isSuccessfull.Ok {
		print("User's payment method modified with success!\n")
	} else {
		print("User's payment method where not modified!\n")
	}
	return isSuccessfull.Ok, err
}

//Restaurant service RPCs

func (fe *frontendServer) getAllRestaurants(ctx context.Context) ([]*pb.RestaurantMessage, error) {
	restaurants, err := pb.NewRestaurantServiceClient(fe.restaurantSvcConn).
		GetAllRestaurants(ctx, &pb.RestaurantsRequest{
			Message: "prova",
		})
	if err != nil {
		print("Can't get restaurants list")
		return nil, err
	}
	var rst []*pb.RestaurantMessage
	for {
		restaurant, err := restaurants.Recv()
		if err == io.EOF {
			return rst, err
		}
		rst = append(rst, restaurant)
		print(restaurant.String() + "\n")
	}
}

func (fe *frontendServer) getRestaurantByName(ctx context.Context, name string) ([]*pb.RestaurantMessage, error) {
	restaurants, err := pb.NewRestaurantServiceClient(fe.restaurantSvcConn).
		GetRestaurantInfoByName(ctx, &pb.RestaurantRequestName{
			Name: name,
		})
	if err != nil {
		print("Can't get restaurant by name")
		return nil, err
	}
	var rst []*pb.RestaurantMessage
	for {
		restaurant, err := restaurants.Recv()
		if err == io.EOF {
			return rst, err
		}
		rst = append(rst, restaurant)
		print(restaurant.String() + "\n")
	}
}

func (fe *frontendServer) getRestaurantById(ctx context.Context, id string) (*pb.RestaurantMessage, error) {
	logrus.Infof("ID of searche restaurant %s \n", id)
	restaurant, err := pb.NewRestaurantServiceClient(fe.restaurantSvcConn).
		GetRestaurantInfoById(ctx, &pb.RestaurantRequestID{
			Id: id,
		})
	return restaurant, err
}

func (fe *frontendServer) addRestaurant(ctx context.Context, name string, city string, address string, tags *pb.TAG) {
	response, err := pb.NewRestaurantServiceClient(fe.restaurantSvcConn).
		AddRestaurant(ctx, &pb.AddRestaurantRequest{
			Name:    name,
			City:    city,
			Address: address,
			TAG:     tags,
		})
	if err != nil {
		print("Error in adding restaurant")
		return
	}
	print(response)
}

func (fe *frontendServer) modifyRestaurantMenu(ctx context.Context, id string, newMenu []*pb.RestaurantMenuItem) {
	isSuccessful, err := pb.NewRestaurantServiceClient(fe.restaurantSvcConn).
		ModifyRestaurantMenu(ctx, &pb.ModifyRestaurantMenuRequest{
			Id:         id,
			ItemsToAdd: newMenu,
		})

	if err != nil {
		print("Error in updating menu")
		logrus.Infof("" + fmt.Sprint(err) + "\n")
		return
	}
	if isSuccessful.Ok {
		print("Menu updated successfully")
	} else {
		print("Menu could not be updated, grpc returned failure")
	}
}

//Mail service handlers

func (fe *frontendServer) sendMail(ctx context.Context, tag string, id string) {
	isSuccessful, err := pb.NewSdccMailServiceClient(fe.mailSvcConn).
		SendMail(ctx, &pb.SendMailRequest{
			UserID: id,
			Tag:    tag,
		})

	if err != nil {
		print("Error in sending mail")
		logrus.Infof("" + fmt.Sprint(err) + "\n")
		return
	}
	if isSuccessful.Ok {
		print("Mail sent successfully")
	} else {
		print("Failed to send mail")
	}
}

//Recommendation service handlers

func (fe *frontendServer) getRecommendations(ctx context.Context, id string) []string {
	response, err := pb.NewRecommendationServiceClient(fe.recommendationSvcConn).
		GetRecommendations(ctx, &pb.GetRecommendationsRequest{
			UserID: id,
		})

	if err != nil {
		print("Error getting recommendations for user")
		logrus.Infof("" + fmt.Sprint(err) + "\n")
		return nil
	}

	for rec := range response.RecommendationList {
		print(rec)
	}

	return response.RecommendationList
}

//Order service handlers

func (fe *frontendServer) getOrder(ctx context.Context, id string) pb.Cart {
	order, err := pb.NewOrderServiceClient(fe.orderSvcConn).
		GetCart(ctx, &pb.GetCartRequest{
			UserId: id,
		})

	if err != nil {
		print("error in getting user order \n")
		logrus.Infof("" + fmt.Sprint(err) + "\n")
		return pb.Cart{}
	}

	return *order
}

func (fe *frontendServer) emptyCart(ctx context.Context, id string) {
	_, err := pb.NewOrderServiceClient(fe.orderSvcConn).
		EmptyCart(ctx, &pb.EmptyCartRequest{
			UserId: id,
		})

	if err != nil {
		print("error in emptying user order")
		return
	}
	print("order succesfully cleared \n")
	return

}

func (fe *frontendServer) addToCart(ctx context.Context, userID string, restaurantID string, itemToAdd *pb.CartItem) {
	_, err := pb.NewOrderServiceClient(fe.orderSvcConn).
		AddItem(ctx, &pb.AddItemRequest{
			UserId:       userID,
			RestaurantId: restaurantID,
			Item:         itemToAdd,
		})

	if err != nil {
		print("Error in adding item to order \n")
		return
	}
}

//Checkout service handlers

func (fe *frontendServer) executeCheckout(ctx context.Context, userID string, restaurantID string, menuItems []*pb.MenuItem, cardNumber string, cvc string, expiration string) bool {
	response, err := pb.NewCheckoutServiceClient(fe.checkoutSvcConn).
		ExecuteTransaction(ctx, &pb.TransactionInfo{
			UserID:         userID,
			RestaurantID:   restaurantID,
			Items:          menuItems,
			CardNumber:     cardNumber,
			Cvc:            cvc,
			CardExpiration: expiration,
		})

	if err != nil {
		print("Error in executing transaction \n")
		logrus.Infof("" + fmt.Sprint(err) + "\n")
		return false
	}
	return response.IsSuccessful
}
