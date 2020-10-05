import random
from locust import HttpLocust, TaskSet, between

#Restaurant service
def list_all_restaurants(l):
    l.client.get("/restaurants")

#def get_restaurant_by_id(l):

def get_restaurants_by_name(l):
	l.client.post("/getRestaurantByName", json={
		"name" : "Starbucks"
		})

#User service
def get_user(l):
    id = 1
    l.client.get("/getUser?id=" + str(id))

#Mail service
def send_mail(l):
    id = 1
    tag = "PUT_TAG_HERE"
    l.client.post("/sendMail?id=" + str(id) + "&tag=" + tag)

#Cart service
def add_to_cart(l):
    l.client.post("/addItemToOrder", json={
        "userid" : "1",
        "restaurantid" : "idprova",
        "item": {
            "productid" : "prova",
            "quantity" : "2",
            "price" : "4"
        }
    })

def get_cart(l):
    id = 1
    l.client.get("/getOrder?id=" + str(id))

def empty_cart(l):
    id = 1
    l.client.post("/emptyCart?id=" + str(id))

#Recommendation service
def get_recommendations(l):
    id = 1
    l.client.get("/getRecommendations?id=" + str(id))

#Checkout service
def execute_checkout(l):
    l.client.post("/checkout", json={
        "userid" : "1",
        "restaurantid" : "idprova",
        "menuitems" : [
            {"productid" : "idprova",
            "quantity" : "2",
            "price" : "10"
             }
            ],
        "cardnumber" : "4000901291203",
        "cvc" : "000",
        "expiration" : "08/22"
    })


class UserBehavior(TaskSet):

    def on_start(self):
        get_user(self)

    tasks = {list_all_restaurants: 1,
    		 get_restaurants_by_name: 2,
             send_mail: 1,
             add_to_cart: 4,
             get_cart: 2,
             get_recommendations: 1,
             execute_checkout: 1,
             empty_cart: 1
             }

class WebsiteUser(HttpLocust):
    task_set = UserBehavior
    wait_time = between(1, 10)
