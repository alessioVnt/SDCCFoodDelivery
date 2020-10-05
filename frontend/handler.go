package main

import (
	"encoding/json"
	"fmt"
	pb "github.com/alessioVnt/frontend/pb"
	"github.com/sirupsen/logrus"
	"html/template"
	"io/ioutil"
	"net/http"
	"strconv"
	"strings"
)

type platformDetails struct {
	css      string
	provider string
}

var (
	templates = template.Must(template.New("").ParseGlob("templates/*.html"))
	plat      platformDetails
)

//User service handlers

func (fe *frontendServer) getUserByIDHandler(w http.ResponseWriter, r *http.Request) {
	logrus.Infof("getUserByID request received\n")
	id, _ := strconv.ParseInt(r.FormValue("id"), 10, 64)
	id32 := int32(id)

	fe.getUserByID(r.Context(), id32)
	logrus.Infof("Request completed\n")
}

func (fe *frontendServer) updateUserPreferencesHandler(w http.ResponseWriter, r *http.Request) {
	logrus.Infof("updateUserPreferences request received\n")
	id, _ := strconv.ParseInt(r.FormValue("id"), 10, 32)
	id32 := int32(id)

	newPref := r.FormValue("newPreference")
	logrus.Infof("newPref: %s", newPref)

	err := fe.updatePreferences(r.Context(), id32, newPref)
	if err != nil {
		w.WriteHeader(http.StatusNotModified)
	} else {
		w.WriteHeader(http.StatusAccepted)
	}
	logrus.Infof("Request completed\n")
}

//Restaurant service handlers

func (fe *frontendServer) restaurantListHandler(w http.ResponseWriter, r *http.Request) {
	logrus.Infof("getAllRestaurants request received\n")
	_, err := fe.getAllRestaurants(r.Context())
	if err != nil {
		logrus.Infof("Error in getting restaurants")
	}
	logrus.Infof("Request completed\n")
}

func (fe *frontendServer) getRestaurantByNameHandler(w http.ResponseWriter, r *http.Request) {
	logrus.Infof("getRestaurantByName request received\n")
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		print("error")
	}

	var result map[string]interface{}
	json.Unmarshal([]byte(body), &result)

	name := result["name"]

	fe.getRestaurantByName(r.Context(), name.(string))
	logrus.Infof("Request completed\n")
}

func (fe *frontendServer) addRestaurantHandler(w http.ResponseWriter, r *http.Request) {
	logrus.Infof("addRestaurant request received\n")
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		print("error")
	}

	var result map[string]interface{}
	json.Unmarshal([]byte(body), &result)

	name := result["name"]
	city := result["city"]
	address := result["address"]
	redTags := result["TAG"].(map[string]interface{})
	tags := &pb.TAG{Tag1: redTags["tag1"].(string), Tag2: redTags["tag2"].(string), Tag3: redTags["tag3"].(string)}

	fe.addRestaurant(r.Context(), name.(string), city.(string), address.(string), tags)
	logrus.Infof("Request completed\n")

	//for key, value := range redTags {
	// Each value is an interface{} type, that is type asserted as a string
	//	fmt.Println(key, value.(string))
	//}

}

func (fe *frontendServer) modifyRestaurantMenuHandler(w http.ResponseWriter, r *http.Request) {
	logrus.Infof("modifyRestaurantMenu request received\n")
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		print("error")
	}

	var result map[string]interface{}
	json.Unmarshal([]byte(body), &result)

	id := result["id"].(string)

	newMenu := result["newMenu"].([]interface{})
	var menuItems []*pb.RestaurantMenuItem

	for _, item := range newMenu {

		itemMapped := item.(map[string]interface{})

		name := itemMapped["name"].(string)
		description := itemMapped["description"].(string)

		price64, _ := strconv.ParseFloat(itemMapped["price"].(string), 64)

		menuItem := &pb.RestaurantMenuItem{
			Name:        name,
			Description: description,
			Price:       price64,
		}
		menuItems = append(menuItems, menuItem)
	}

	fe.modifyRestaurantMenu(r.Context(), id, menuItems)
	logrus.Infof("Request completed\n")
}

//Mail service handlers

func (fe *frontendServer) sendMailHandler(w http.ResponseWriter, r *http.Request) {
	logrus.Infof("sendMail request received\n")
	id := r.FormValue("id")
	tag := r.FormValue("tag")

	fe.sendMail(r.Context(), tag, id)
	logrus.Infof("Request completed\n")
}

//Recommendation service handlers

func (fe *frontendServer) getRecommendationsHandler(w http.ResponseWriter, r *http.Request) {
	logrus.Infof("getRecommendations request received\n")
	id := r.FormValue("id")
	recommendations := fe.getRecommendations(r.Context(), id)

	for _, recommended := range recommendations {
		print(recommended + "\n")
	}
	logrus.Infof("Request completed\n")
}

//Cart service handlers

func (fe *frontendServer) getOrderHandler(w http.ResponseWriter, r *http.Request) {
	logrus.Infof("getOrder request received\n")
	id := r.FormValue("id")
	order := fe.getOrder(r.Context(), id)

	//Print the result
	if order.UserId != "" {
		userID := order.UserId
		print("Order user ID: " + userID + " \n")
		restaurantID := order.RestaurantId
		print("From restaurant with ID: " + restaurantID + "\n")
		itemsInOrder := order.Items
		print("Items in the order: \n")
		for i, item := range itemsInOrder {
			print("ITEM NUMBER " + strconv.Itoa(i) + " IN THE ORDER\n")
			print("ProductID: " + item.ProductId + " x" + fmt.Sprint(item.Quantity) + "\n")
			print("With price: " + fmt.Sprint(item.Price) + "\n")
			print("\n")
		}
	}
	logrus.Infof("Request completed\n")

}

func (fe *frontendServer) emptyCartHandler(w http.ResponseWriter, r *http.Request) {
	logrus.Infof("emptyCart request received\n")
	id := r.FormValue("id")
	fe.emptyCart(r.Context(), id)
	logrus.Infof("Request completed\n")
}

func (fe *frontendServer) addToCartHandler(w http.ResponseWriter, r *http.Request) {
	logrus.Infof("addToCart request received\n")
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		print("error in reading body of http message")
	}

	var result map[string]interface{}
	json.Unmarshal([]byte(body), &result)

	userID := result["userid"].(string)
	restaurantID := result["restaurantid"].(string)

	itemToAdd := result["item"].(map[string]interface{})

	//Converting quantity to in32 from string
	quantity64, _ := strconv.ParseInt(itemToAdd["quantity"].(string), 10, 32)
	quantity := int32(quantity64)

	//Converting price to float32 from string
	price64, _ := strconv.ParseFloat(itemToAdd["price"].(string), 32)
	price := float32(price64)

	cItem := &pb.CartItem{
		ProductId: itemToAdd["productid"].(string),
		Quantity:  quantity,
		Price:     price,
	}

	fe.addToCart(r.Context(), userID, restaurantID, cItem)
	w.WriteHeader(http.StatusFound)
	logrus.Infof("Request completed\n")
}

//Checkout service handlers

func (fe *frontendServer) checkoutRequestHandler(w http.ResponseWriter, r *http.Request) {
	logrus.Infof("checkout request received\n")
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		print("error in reading body of http message")
	}

	var result map[string]interface{}
	json.Unmarshal([]byte(body), &result)

	userID := result["userid"].(string)
	restaurantID := result["restaurantid"].(string)

	menuItemsJSN := result["menuitems"].([]interface{})
	var menuItems []*pb.MenuItem

	for _, value := range menuItemsJSN {

		item := value.(map[string]interface{})

		productid := item["productid"].(string)

		//Converting quantity to in32 from string
		quantity64, _ := strconv.ParseInt(item["quantity"].(string), 10, 32)
		quantity := int32(quantity64)

		//Converting price to float32 from string
		price64, _ := strconv.ParseFloat(item["price"].(string), 32)
		price := float32(price64)

		toAdd := &pb.MenuItem{
			ProductId: productid,
			Quantity:  quantity,
			Price:     price,
		}
		menuItems = append(menuItems, toAdd)
	}

	cardNumber := result["cardnumber"].(string)
	cvc := result["cvc"].(string)
	expiration := result["expiration"].(string)

	transactionResult := fe.executeCheckout(r.Context(), userID, restaurantID, menuItems, cardNumber, cvc, expiration)

	if transactionResult {
		print("Transaction successfull!")
	} else {
		print("Transaction failed!")
	}

	logrus.Infof("Request completed\n")
}

//HTML handler

func (fe *frontendServer) homePageHandler(w http.ResponseWriter, r *http.Request) {
	logrus.Infof("Home page request received\n")

	session, _ := fe.store.Get(r, "trial-session")
	if session.Values["logged"] == nil {
		session.Values["logged"] = false
		err := session.Save(r, w)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}
	logged := session.Values["logged"]
	userName := session.Values["userName"]

	//Retrieve recommendations if user is logged
	if logged == true {
		//call rec service and get list of recs
		userId, _ := session.Values["userID"]
		recommendations := fe.getRecommendations(r.Context(), fmt.Sprintf("%d", userId))
		type recommendation struct {
			Name        string
		}
		recs := make([]recommendation, len(recommendations))
		for i, name := range recommendations {
			recs[i] = recommendation{
				Name:        strings.Replace(name, "\"", "", -1),
			}
		}
		if err := templates.ExecuteTemplate(w, "home", map[string]interface{}{
			"logged": logged,
			"userName": userName,
			"recommendations": recs,
		}); err != nil {
			logrus.Infof("Error executing template! %s \n", err)
		}
	} else {
		if err := templates.ExecuteTemplate(w, "home", map[string]interface{}{
			"logged": logged,
		}); err != nil {
			logrus.Infof("Error executing template! %s \n", err)
		}
	}
}

func (fe *frontendServer) restaurantsPageHandler(w http.ResponseWriter, r *http.Request) {
	session, _ := fe.store.Get(r, "trial-session")
	userId := session.Values["userID"]

	name := r.FormValue("name")
	var rst []*pb.RestaurantMessage
	var err error
	if name != "" {
		rst, err = fe.getRestaurantByName(r.Context(), name)
	} else {
		rst, err = fe.getAllRestaurants(r.Context())
	}
	var rstSlice []*pb.RestaurantMessage
	if len(rst) > 100 {
		rstSlice = rst[0:100]
	} else {
		rstSlice = rst
	}
	if err != nil {
		logrus.Infof("Error in getting restaurants")
	}
	if err := templates.ExecuteTemplate(w, "restaurants", map[string]interface{}{
		"restaurants": rstSlice,
		"userId": userId,
	}); err != nil {
		logrus.Infof("Error executing template! %s \n", err)
	}
}

func (fe *frontendServer) restaurantDetailsHandler(w http.ResponseWriter, r *http.Request) {
	id := r.FormValue("id")
	session, _ := fe.store.Get(r, "trial-session")
	userID, _ := session.Values["userID"]
	restaurant, errGrpc := fe.getRestaurantById(r.Context(), id)
	if errGrpc != nil {
		print("Error in getting restaurant info by ID")
		return
	}
	type menuItem struct {
		Name        string
		Description string
		Price       string
	}
	menuItems := make([]menuItem, len(restaurant.MenuItems))
	for i, item := range restaurant.MenuItems {
		menuItems[i] = menuItem{
			Name:        item.Name,
			Description: item.Description,
			Price:       fmt.Sprintf("%f", item.Price),
		}
	}
	if err := templates.ExecuteTemplate(w, "restaurantDetails", map[string]interface{}{
		"restaurant": restaurant.Name,
		"restaurantId": restaurant.Id,
		"items":      menuItems,
		"userID": userID,
	}); err != nil {
		logrus.Infof("Error executing template! %s \n", err)
	}
	logrus.Infof("Name of found restaurant %s \n", restaurant.Name)
	logrus.Infof("menu item in grpc resp: %s \n", restaurant.MenuItems[0].Name)
	logrus.Infof("menu item: %s \n", menuItems[0].Name)
}

func (fe *frontendServer) cartPageHandler(w http.ResponseWriter, r *http.Request){
	session, _ := fe.store.Get(r, "trial-session")
	id, _ := session.Values["userID"]
	cart := fe.getOrder(r.Context(), fmt.Sprint(id))

	var restaurantName string

	if cart.RestaurantId != "" {
		restaurant, resError := fe.getRestaurantById(r.Context(), cart.RestaurantId)
		if resError != nil {
			restaurantName = ""
		} else {
			restaurantName = restaurant.Name
		}
	}

	type itemInOrder struct {
		Name        string
		Quantity 	string
		Price       string
	}

	totalPrice := float32(0.0)

	items := make([]itemInOrder, len(cart.Items))
	for i, item := range cart.Items{
		totalPrice += item.Price * float32(item.Quantity)
		items[i] = itemInOrder{
			Name: item.ProductId,
			Quantity: fmt.Sprintf("%d", item.Quantity),
			Price: fmt.Sprintf("%.2f", item.Price),
		}
	}
	if err := templates.ExecuteTemplate(w, "cart", map[string]interface{}{
		"items": items,
		"userId": cart.UserId,
		"restaurantName": restaurantName,
		"totalPrice": totalPrice,
	}); err != nil {
		logrus.Infof("Error executing template! %s \n", err)
	}
}

func (fe *frontendServer) loginHandler(w http.ResponseWriter, r *http.Request){
	//Read body of http request
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		print("error in reading body of http message")
	}
	var result map[string]interface{}
	json.Unmarshal([]byte(body), &result)
	//Extract mail and password
	mail := result["mail"].(string)
	password := result["password"].(string)

	resp := fe.logIn(r.Context(), mail, password)

	if resp.Logged == true {
		user, userErr := fe.getUserByID(r.Context(), resp.UserId)
		if userErr != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		session, _ := fe.store.Get(r, "trial-session")
		session.Values["logged"] = resp.Logged
		session.Values["userID"] = resp.UserId
		session.Values["userName"] = resp.Username
		session.Values["userMail"] = user.Mail
		session.Values["userAddress"] = user.Address
		err := session.Save(r, w)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		w.WriteHeader(http.StatusAccepted)
	} else {
		w.WriteHeader(http.StatusUnauthorized)
	}



}

func (fe *frontendServer) checkoutPageHandler(w http.ResponseWriter, r *http.Request){
	userID := r.FormValue("id");

	//Retrieve cart items
	cart := fe.getOrder(r.Context(), fmt.Sprint(userID))

	type itemInOrder struct {
		Name        string
		Quantity string
		Price       string
	}

	totalPrice := float32(0.0)

	cartItems := make([]itemInOrder, len(cart.Items))
	var menuItems []*pb.MenuItem
	for i, item := range cart.Items{
		totalPrice += item.Price * float32(item.Quantity)
		cartItems[i] = itemInOrder{
			Name: item.ProductId,
			Quantity: fmt.Sprintf("%d", item.Quantity),
			Price: fmt.Sprintf("%.2f", item.Price),
		}
		toAdd := &pb.MenuItem{
			ProductId: item.ProductId,
			Quantity:  item.Quantity,
			Price:     item.Price,
		}
		menuItems = append(menuItems, toAdd)
	}

	intID, _ := strconv.ParseInt(userID, 10, 32)
	userInfo, userErr := fe.getUserByID(r.Context(), int32(intID))
	if userErr != nil{
		http.Error(w, userErr.Error(), http.StatusInternalServerError)
		return
	}

	transactionResult := fe.executeCheckout(r.Context(), userID, cart.RestaurantId, menuItems, userInfo.CreditCard.CreditCardNumber, userInfo.CreditCard.ThreeDigitCode, userInfo.CreditCard.DeadLine)

	var restaurantName = ""
	if transactionResult == true {
		fe.emptyCart(r.Context(), userID)
		restaurant, restaurantErr := fe.getRestaurantById(r.Context(), cart.RestaurantId)
		if restaurantErr == nil {
			restaurantName = restaurant.Name
		}
		fe.sendMail(r.Context(), "ORDER_SUCCESS", userID)
	}

	session, _ := fe.store.Get(r, "trial-session")
	userAddress := session.Values["userAddress"]

	if err := templates.ExecuteTemplate(w, "checkoutDetails", map[string]interface{}{
		"userID": userID,
		"transactionResult": transactionResult,
		"items": cartItems,
		"totalPrice": totalPrice,
		"restaurantName": restaurantName,
		"userAddress": userAddress,
	}); err != nil {
		logrus.Infof("Error executing template! %s \n", err)
	}
}

func (fe *frontendServer) userProfilePageHandler(w http.ResponseWriter, r *http.Request){
	session, _ := fe.store.Get(r, "trial-session")
	userID := session.Values["userID"]
	userName := session.Values["userName"]
	userMail := session.Values["userMail"]
	userAddress := session.Values["userAddress"]

	if err := templates.ExecuteTemplate(w, "userProfile", map[string]interface{}{
		"userId": userID,
		"userName": userName,
		"userMail": userMail,
		"userAddress": userAddress,
	}); err != nil {
		logrus.Infof("Error executing template! %s \n", err)
	}
}

func (fe *frontendServer) logOutHandler(w http.ResponseWriter, r *http.Request){
	session, _ := fe.store.Get(r, "trial-session")
	session.Values["logged"] = false
	delete(session.Values, "userID")
	delete(session.Values, "userName")
	delete(session.Values, "userMail")
	delete(session.Values, "userAddress")
	err := session.Save(r, w)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
}

func (fe *frontendServer) registrationPageHandler(w http.ResponseWriter, r *http.Request){
	if err := templates.ExecuteTemplate(w, "registration", map[string]interface{}{}); err != nil {
		logrus.Infof("Error executing template! %s \n", err)
	}
}

func (fe *frontendServer) registrationHandler(w http.ResponseWriter, r *http.Request){
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		print("error in reading body of http message")
	}

	var result map[string]interface{}
	json.Unmarshal([]byte(body), &result)

	userName := result["username"].(string)
	mail := result["mail"].(string)
	address := result["address"].(string)
	password := result["password"].(string)
	cardNumber := result["cardNumber"].(string)
	cvc := result["cvc"].(string)
	expire := result["expire"].(string)

	registrationOutcome, registrationErr := fe.createUser(r.Context(), userName, password, mail, address, cardNumber, cvc, expire)
	if registrationErr != nil {
		http.Error(w, registrationErr.Error(), http.StatusInternalServerError)
		return
	}
	if registrationOutcome == true {
		w.WriteHeader(http.StatusAccepted)
	} else {
		w.WriteHeader(http.StatusNotModified)
	}
}

// User updates handlers

func (fe *frontendServer) updateUsernameHandler(w http.ResponseWriter, r *http.Request){
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		print("error in reading body of http message")
	}
	var result map[string]interface{}
	json.Unmarshal([]byte(body), &result)

	userID64, _ := strconv.ParseInt(result["userid"].(string), 10, 64)
	userID := int32(userID64)
	newUserName := result["newUserName"].(string)

	response, updateErr := fe.updateUsername(r.Context(), userID, newUserName)
	if updateErr != nil {
		http.Error(w, updateErr.Error(), http.StatusInternalServerError)
		return
	}
	//Prepare to modify session cookies
	session, _ := fe.store.Get(r, "trial-session")
	if response {
		session.Values["userName"] = newUserName
		session.Save(r, w)
		w.WriteHeader(http.StatusAccepted)
	} else {
		w.WriteHeader(http.StatusNotModified)
	}
}

func (fe *frontendServer) updateMailHandler(w http.ResponseWriter, r *http.Request){
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		print("error in reading body of http message")
	}
	var result map[string]interface{}
	json.Unmarshal([]byte(body), &result)

	userID64, _ := strconv.ParseInt(result["userid"].(string), 10, 64)
	userID := int32(userID64)
	newMail := result["newMail"].(string)

	response, updateErr := fe.updateMail(r.Context(), userID, newMail)
	if updateErr != nil {
		http.Error(w, updateErr.Error(), http.StatusInternalServerError)
		return
	}
	//Prepare to modify session cookies
	session, _ := fe.store.Get(r, "trial-session")
	if response {
		session.Values["userMail"] = newMail
		session.Save(r, w)
		w.WriteHeader(http.StatusAccepted)
	} else {
		w.WriteHeader(http.StatusNotModified)
	}
}

func (fe *frontendServer) updatePasswordHandler(w http.ResponseWriter, r *http.Request){
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		print("error in reading body of http message")
	}
	var result map[string]interface{}
	json.Unmarshal([]byte(body), &result)

	userID64, _ := strconv.ParseInt(result["userid"].(string), 10, 64)
	userID := int32(userID64)
	newPassword := result["newPassword"].(string)

	response, updateErr := fe.updatePassword(r.Context(), userID, newPassword)
	if updateErr != nil {
		http.Error(w, updateErr.Error(), http.StatusInternalServerError)
		return
	}
	if response {
		w.WriteHeader(http.StatusAccepted)
	} else {
		w.WriteHeader(http.StatusNotModified)
	}
}

func (fe *frontendServer) updateAddressHandler(w http.ResponseWriter, r *http.Request){
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		print("error in reading body of http message")
	}
	var result map[string]interface{}
	json.Unmarshal([]byte(body), &result)

	userID64, _ := strconv.ParseInt(result["userid"].(string), 10, 64)
	userID := int32(userID64)
	newAddress := result["newAddress"].(string)

	response, updateErr := fe.updateAddress(r.Context(), userID, newAddress)
	if updateErr != nil {
		http.Error(w, updateErr.Error(), http.StatusInternalServerError)
		return
	}
	//Prepare to modify session cookies
	session, _ := fe.store.Get(r, "trial-session")
	if response {
		session.Values["userAddress"] = newAddress
		session.Save(r, w)
		w.WriteHeader(http.StatusAccepted)
	} else {
		w.WriteHeader(http.StatusNotModified)
	}
}

func (fe *frontendServer) updateCreditCardHandler(w http.ResponseWriter, r *http.Request){
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		print("error in reading body of http message")
	}
	var result map[string]interface{}
	json.Unmarshal([]byte(body), &result)

	userID64, _ := strconv.ParseInt(result["userid"].(string), 10, 64)
	userID := int32(userID64)
	cardNumber := result["cardNumber"].(string)
	cvc := result["cvc"].(string)
	expire := result["expiration"].(string)

	response, updateErr := fe.updateCreditCard(r.Context(), userID, cardNumber, cvc, expire)
	if updateErr != nil {
		http.Error(w, updateErr.Error(), http.StatusInternalServerError)
		return
	}
	if response {
		w.WriteHeader(http.StatusAccepted)
	} else {
		w.WriteHeader(http.StatusNotModified)
	}
}