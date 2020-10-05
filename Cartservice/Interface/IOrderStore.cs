using System.Threading.Tasks;

namespace CartServer.Interface
{
    internal interface IOrderStore
    {
        Task InitializeAsync();
        
        Task AddItemAsync(string userId, string restaurantID, string productId, int quantity, float price);
        Task EmptyCartAsync(string userId);

        Task<SdccFoodDelivery.Cart> GetCartAsync(string userId);

        bool Ping();
    }
}
