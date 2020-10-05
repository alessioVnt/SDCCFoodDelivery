using System;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Google.Protobuf;
using Grpc.Core;
using StackExchange.Redis;
using CartServer.Interface;
using Newtonsoft.Json;

namespace CartServer.OrderStore
{
    class RedisOrderStore : IOrderStore
    {

        private const string CART_FIELD_NAME = "cart";
        private const int REDIS_RETRY_NUM = 5;
        private ConnectionMultiplexer redis;
        private volatile bool isRedisConnectionOpened = false;
        private readonly object locker = new object();
        private readonly string connectionString;

        private readonly ConfigurationOptions redisConnectionOptions;

        //Constructor
        public RedisOrderStore(string redisAddress)
        {

            connectionString = $"{redisAddress},ssl=false,allowAdmin=true,connectRetry=5";
            redisConnectionOptions = ConfigurationOptions.Parse(connectionString);

            // Try to reconnect if first retry failed (up to 5 times with exponential backoff)
            redisConnectionOptions.ConnectRetry = REDIS_RETRY_NUM;
            redisConnectionOptions.ReconnectRetryPolicy = new ExponentialRetry(100);

            redisConnectionOptions.KeepAlive = 180;

        }

        //Adds an item to the cart
        public async Task AddItemAsync(string userId, string restaurantID, string productId, int quantity, float price)
        {
            Console.WriteLine($"AddItemAsync called with userId={userId}, productId={productId}, quantity={quantity}");

            try
            {
                EnsureRedisIsConnected();

                var db = redis.GetDatabase();

                // Access the cart from the cache
                var value = await db.HashGetAsync(userId, CART_FIELD_NAME);

                Cart cart;
                if (value.IsNull)
                {
                    cart = new Cart();
                    cart.SetUserID(userId);
                    cart.SetRestaurantID(restaurantID);
                    cart.AddMenuItem(new MenuItem(productId, quantity, price));
                    //cart.Items.Add(new Hipstershop.CartItem { ProductId = productId, Quantity = quantity });
                }
                else
                {
                    cart = JsonConvert.DeserializeObject<Cart>(value);
                    //if the cart already existed but contained the order from another restaurant reset it and add the item
                    if (cart.restaurantID == null || !cart.restaurantID.Equals(restaurantID))
                    {
                        //Empty the cart
                        cart.ResetCart(restaurantID);
                    }
                    cart.AddMenuItem(new MenuItem(productId, quantity, price));
                }
                await db.HashSetAsync(userId, new[] { new HashEntry(CART_FIELD_NAME, cart.toJSON()) });
            }
            catch (Exception ex)
            {
                throw new RpcException(new Status(StatusCode.FailedPrecondition, $"Can't access cart storage. {ex}"));
            }
        }

        //Empties the cart
        public async Task EmptyCartAsync(string userId)
        {
            Console.WriteLine($"EmptyCartAsync called with userId={userId}");

            try
            {
                EnsureRedisIsConnected();
                var db = redis.GetDatabase();

                Cart emptyCart = new Cart();
                emptyCart.SetUserID(userId);

                // Update the cache with empty cart for given user
                await db.HashSetAsync(userId, new[] { new HashEntry(CART_FIELD_NAME, emptyCart.toJSON()) });
            }
            catch (Exception ex)
            {
                throw new RpcException(new Status(StatusCode.FailedPrecondition, $"Can't access cart storage. {ex}"));
            }
        }

        //Returns the user's cart
        public async Task<SdccFoodDelivery.Cart> GetCartAsync(string userId)
        {
            Console.WriteLine($"GetCartAsync called with userId={userId}");

            try
            {
                EnsureRedisIsConnected();

                var db = redis.GetDatabase();

                // Access the cart from the cache
                var value = await db.HashGetAsync(userId, CART_FIELD_NAME);

                if (!value.IsNull)
                {
                    Cart cart = new Cart();
                    cart = JsonConvert.DeserializeObject<Cart>(value);
                    SdccFoodDelivery.Cart hWCart = new SdccFoodDelivery.Cart();
                    hWCart.UserId = cart.userID;
                    if (cart.restaurantID != null){
                        hWCart.RestaurantId = cart.restaurantID;    
                    }
                    foreach (MenuItem item in cart.menuItems)
                    {
                        hWCart.Items.Add(new SdccFoodDelivery.CartItem {ProductId = item.itemID, Quantity = item.quantity, Price = item.price});
                    } 
                    return hWCart;
                }

                // We decided to return empty cart in cases when user wasn't in the cache before
                SdccFoodDelivery.Cart hNonExistingWCart = new SdccFoodDelivery.Cart(); 
                hNonExistingWCart.UserId = userId;              
                return hNonExistingWCart;
            }
            catch (Exception ex)
            {
                throw new RpcException(new Status(StatusCode.FailedPrecondition, $"Can't access cart storage. {ex}"));
            }
        }

        //Initialize Redis connection
        public Task InitializeAsync()
        {
            EnsureRedisIsConnected();
            return Task.CompletedTask;
        }

        //Checks if a Redis connection exists already
        private void EnsureRedisIsConnected()
        {

            if (isRedisConnectionOpened)
            {
                return;
            }
            //If there is no connection to Redis get the lock and initialize a new one
            lock (locker)
            {
                if (isRedisConnectionOpened)
                {
                    return;
                }

                Console.WriteLine("Connecting to Redis: " + connectionString);
                redis = ConnectionMultiplexer.Connect(redisConnectionOptions);

                //Check for connection failure
                if (redis == null || !redis.IsConnected)
                {
                    Console.WriteLine("Wasn't able to connect to redis");

                    // We weren't able to connect to redis despite 5 retries with exponential backoff
                    throw new ApplicationException("Wasn't able to connect to redis");
                }

                Console.WriteLine("Successfully connected to Redis");
                var cache = redis.GetDatabase();

                Console.WriteLine("Performing small test");
                cache.StringSet("cart", "OK");
                object res = cache.StringGet("cart");
                Console.WriteLine($"Small test result: {res}");

                isRedisConnectionOpened = true;

            }
        }

        public bool Ping()
        {
            throw new NotImplementedException();
        }
    }
}
