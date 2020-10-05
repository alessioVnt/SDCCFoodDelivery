// Copyright 2015 gRPC authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using System;
using System.Threading.Tasks;
using Grpc.Core;
using cartservice;
using StackExchange.Redis;
using CartServer.OrderStore;
using CartServer.Interface;
using System.Threading;
using Newtonsoft.Json;
using static SdccFoodDelivery.Cart;
using SdccFoodDelivery;

namespace CartServer
{
    class Program
    {
        // todo make them dynamic
        const string hostName = "0.0.0.0";
        const int port = 7070;
        const string redisAddress = "localhost";

        
        static object StartServer(string host, int port, IOrderStore orderStore)
        {
            // Run the server in a separate thread and make the main thread busy waiting.
            // The busy wait is because when we run in a container, we can't use techniques such as waiting on user input (Console.Readline())
            Task serverTask = Task.Run(async () =>
            {
                try
                {
                    await orderStore.InitializeAsync();

                    Console.WriteLine($"Trying to start a grpc server at  {host}:{port}");
                    Server server = new Server
                    {
                        Services =
                        {
                            // Cart Service Endpoint
                             SdccFoodDelivery.OrderService.BindService(new OrderServiceImpl(orderStore))
                        },
                        Ports = { new ServerPort(host, port, ServerCredentials.Insecure) }
                    };

                    Console.WriteLine($"Cart server is listening at {host}:{port}");
                    server.Start();

                    Console.WriteLine("Initialization completed");

                    // Keep the server up and running
                    while(true)
                    {
                        Thread.Sleep(TimeSpan.FromMinutes(10));
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine(ex);
                }
            });

            return Task.WaitAny(new[] { serverTask });
        }
        
        static void Main(string[] args)
        {

            //Read Redis address from env, if null take the default address
            string redis = Environment.GetEnvironmentVariable("REDIS_ADDR");
            if (string.IsNullOrEmpty(redis))
            {
                redis = redisAddress;
            }
            IOrderStore orderStore = new RedisOrderStore(redis);
            string envPort = Environment.GetEnvironmentVariable("PORT");
            int intPort = port;
            if (!string.IsNullOrEmpty(envPort))
            {
            	intPort = Int32.Parse(envPort);
            } 

            StartServer(hostName, intPort, orderStore);
        }

    }
}
