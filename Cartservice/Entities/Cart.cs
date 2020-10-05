using System.Collections.Generic;
using Json.Net;
using Newtonsoft.Json;

namespace CartServer
{

    class Cart
    {

        public string userID;
        public string restaurantID;
        public List<MenuItem> menuItems = new List<MenuItem>();

        public Cart()
        {

        }


        public string toJSON()
        {
            string jsonString = JsonConvert.SerializeObject(this);
            return jsonString;
        }

        public void SetUserID(string userID)
        {
            this.userID = userID;
        }
        public void SetRestaurantID(string restaurantID)
        {
            this.restaurantID = restaurantID;
        }

        //Add an item to the cart if the item is not in the cart, else increase quantity 
        //todo: use getter and setters to access attributes
        public void AddMenuItem(MenuItem item)
        {
            foreach (MenuItem mItem in this.menuItems)
            {
                if (mItem.itemID.Equals(item.itemID))
                {
                    menuItems[this.menuItems.IndexOf(mItem)].quantity += item.quantity;
                    return;
                }
            }
            menuItems.Add(item);
            return;
        }

        public string OrderToString()
        {
            return this.restaurantID;
        }

        //Reset the cart menu items and assign it the new restaurant ID 
        public void ResetCart(string newRestId)
        {

            this.restaurantID = newRestId;
            menuItems = new List<MenuItem>();

        }


    }
}