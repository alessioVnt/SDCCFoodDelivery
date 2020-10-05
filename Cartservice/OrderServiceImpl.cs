using System;
using System.Threading.Tasks;
using CartServer.Interface;
using Grpc.Core;
using SdccFoodDelivery;
using static SdccFoodDelivery.OrderService;

namespace cartservice
{
    // Cart wrapper to deal with grpc communication
    internal class OrderServiceImpl : OrderServiceBase
    {
        private IOrderStore orderStore;
        private readonly static Empty Empty = new Empty();

        public OrderServiceImpl(IOrderStore orderStore){
            this.orderStore = orderStore;
        }

       public async override Task<Empty> AddItem(AddItemRequest request, Grpc.Core.ServerCallContext context)
        {
            await orderStore.AddItemAsync(request.UserId, request.RestaurantId, request.Item.ProductId, request.Item.Quantity, request.Item.Price);
            return Empty;
        }

        public async override Task<Empty> EmptyCart(EmptyCartRequest request, ServerCallContext context)
        {
            await orderStore.EmptyCartAsync(request.UserId);
            return Empty;
        }

        public override Task<SdccFoodDelivery.Cart> GetCart(GetCartRequest request, ServerCallContext context)
        {
            return orderStore.GetCartAsync(request.UserId);
        }
    }
}
