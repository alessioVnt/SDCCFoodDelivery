using System.Collections.Generic;
using Json.Net;
using Newtonsoft.Json;

namespace CartServer
{
    class MenuItem
    {

        public string itemID;
        public int quantity;
        public float price;

        public MenuItem(string item, int qty, float prc){
            itemID = item;
            quantity = qty;
            price = prc;
        }

        public void IncreaseQuantity(int toIncrease) {
            this.quantity += toIncrease;
        }

    }
}