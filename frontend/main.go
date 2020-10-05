package main

import (
	"context"
	"fmt"
	"github.com/gorilla/mux"
	"github.com/gorilla/sessions"
	"github.com/pkg/errors"
	"github.com/sirupsen/logrus"
	"google.golang.org/grpc"
	"net/http"
	"os"
	"time"
)

const (
	port = "8080"
)

type frontendServer struct {
	restaurantSvcAddr string
	restaurantSvcConn *grpc.ClientConn

	userSvcAddr string
	userSvcConn *grpc.ClientConn

	mailSvcAddr string
	mailSvcConn *grpc.ClientConn

	recommendationSvcAddr string
	recommendationSvcConn *grpc.ClientConn

	orderSvcAddr string
	orderSvcConn *grpc.ClientConn

	checkoutSvcAddr string
	checkoutSvcConn *grpc.ClientConn

	store sessions.Store
}

func main() {

	ctx := context.Background()
	log := logrus.New()
	log.Level = logrus.DebugLevel
	log.Formatter = &logrus.JSONFormatter{
		FieldMap: logrus.FieldMap{
			logrus.FieldKeyTime:  "timestamp",
			logrus.FieldKeyLevel: "severity",
			logrus.FieldKeyMsg:   "message",
		},
		TimestampFormat: time.RFC3339Nano,
	}
	log.Out = os.Stdout

	srvPort := port
	if os.Getenv("PORT") != "" {
		srvPort = os.Getenv("PORT")
	}
	addr := os.Getenv("LISTEN_ADDR")
	svc := new(frontendServer)

	svc.store = sessions.NewCookieStore([]byte(os.Getenv("SESSION_KEY")))

	mustMapEnv(&svc.restaurantSvcAddr, "RESTAURANT_SERVICE_ADDR")
	mustMapEnv(&svc.userSvcAddr, "USER_SERVICE_ADDR")
	mustMapEnv(&svc.mailSvcAddr, "MAIL_SERVICE_ADDR")
	mustMapEnv(&svc.recommendationSvcAddr, "RECOMMENDATION_SERVICE_ADDR")
	mustMapEnv(&svc.orderSvcAddr, "CART_SERVICE_ADDR")
	mustMapEnv(&svc.checkoutSvcAddr, "CHECKOUT_SERVICE_ADDR")

	mustConnGRPC(ctx, &svc.restaurantSvcConn, svc.restaurantSvcAddr)
	mustConnGRPC(ctx, &svc.userSvcConn, svc.userSvcAddr)
	mustConnGRPC(ctx, &svc.mailSvcConn, svc.mailSvcAddr)
	mustConnGRPC(ctx, &svc.recommendationSvcConn, svc.recommendationSvcAddr)
	mustConnGRPC(ctx, &svc.orderSvcConn, svc.orderSvcAddr)
	mustConnGRPC(ctx, &svc.checkoutSvcConn, svc.checkoutSvcAddr)

	r := mux.NewRouter()
	r.HandleFunc("/restaurants", svc.restaurantListHandler).Methods(http.MethodGet, http.MethodHead)
	r.HandleFunc("/getRestaurantByName", svc.getRestaurantByNameHandler).Methods(http.MethodPost)
	r.HandleFunc("/addRestaurant", svc.addRestaurantHandler).Methods(http.MethodPost)
	r.HandleFunc("/modifyRestaurantMenu", svc.modifyRestaurantMenuHandler).Methods(http.MethodPost)
	r.HandleFunc("/getUser", svc.getUserByIDHandler).Methods(http.MethodGet)
	r.HandleFunc("/updatePreferences", svc.updateUserPreferencesHandler).Methods(http.MethodGet)
	r.HandleFunc("/getRecommendations", svc.getRecommendationsHandler).Methods(http.MethodGet)
	r.HandleFunc("/getOrder", svc.getOrderHandler).Methods(http.MethodGet)
	r.HandleFunc("/emptyCart", svc.emptyCartHandler).Methods(http.MethodPost)
	r.HandleFunc("/addItemToOrder", svc.addToCartHandler).Methods(http.MethodPost)
	r.HandleFunc("/checkout", svc.checkoutRequestHandler).Methods(http.MethodPost)
	r.HandleFunc("/sendMail", svc.sendMailHandler).Methods(http.MethodPost)
	r.HandleFunc("/", svc.homePageHandler).Methods(http.MethodGet, http.MethodHead)
	r.HandleFunc("/restaurantsPage", svc.restaurantsPageHandler).Methods(http.MethodGet)
	r.HandleFunc("/restaurantDetails", svc.restaurantDetailsHandler).Methods(http.MethodGet)
	r.HandleFunc("/cartPage", svc.cartPageHandler).Methods(http.MethodGet)
	r.HandleFunc("/logIn", svc.loginHandler).Methods(http.MethodPost)
	r.HandleFunc("/checkoutPage", svc.checkoutPageHandler).Methods(http.MethodGet)
	r.HandleFunc("/userProfile", svc.userProfilePageHandler).Methods(http.MethodGet)
	r.HandleFunc("/updateUserName", svc.updateUsernameHandler).Methods(http.MethodPost)
	r.HandleFunc("/updatePassword", svc.updatePasswordHandler).Methods(http.MethodPost)
	r.HandleFunc("/updateMail", svc.updateMailHandler).Methods(http.MethodPost)
	r.HandleFunc("/updateAddress", svc.updateAddressHandler).Methods(http.MethodPost)
	r.HandleFunc("/updateCreditCard", svc.updateCreditCardHandler).Methods(http.MethodPost)
	r.HandleFunc("/logOut", svc.logOutHandler).Methods(http.MethodGet)
	r.HandleFunc("/registrationPage", svc.registrationPageHandler).Methods(http.MethodGet)
	r.HandleFunc("/register", svc.registrationHandler).Methods(http.MethodPost)
	r.PathPrefix("/static/").Handler(http.StripPrefix("/static/", http.FileServer(http.Dir("./static/"))))
	var handler http.Handler = r

	log.Infof("starting server on " + addr + ":" + srvPort)
	log.Fatal(http.ListenAndServe(addr+":"+srvPort, handler))
}

func mustMapEnv(target *string, envKey string) {
	v := os.Getenv(envKey)
	if v == "" {
		panic(fmt.Sprintf("environment variable %q not set", envKey))
	}
	*target = v
}

func mustConnGRPC(ctx context.Context, conn **grpc.ClientConn, addr string) {
	var err error
	*conn, err = grpc.DialContext(ctx, addr,
		grpc.WithInsecure(),
		grpc.WithTimeout(time.Second*3))
	if err != nil {
		panic(errors.Wrapf(err, "grpc: failed to connect %s", addr))
	}
}
