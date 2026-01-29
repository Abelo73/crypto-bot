import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import client, { DEFAULT_USER_ID } from "../client";
import type { Order, OrderSide, OrderType, ExchangeType } from "../types";

interface PlaceOrderParams {
    exchangeType: ExchangeType;
    symbol: string;
    side: OrderSide;
    orderType: OrderType;
    quantity: number;
    price?: number;
}

export const useOrders = (userId: number = DEFAULT_USER_ID) => {
    const queryClient = useQueryClient();

    const fetchOrders = useQuery({
        queryKey: ["orders", userId],
        queryFn: async () => {
            const response = await client.get<Order[]>(`/users/${userId}/orders`);
            return response.data;
        },
        refetchInterval: 5000, // Poll every 5s for updates
    });

    const placeOrder = useMutation({
        mutationFn: async (order: PlaceOrderParams) => {
            const response = await client.post<Order>(`/users/${userId}/orders`, order);
            return response.data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["orders", userId] });
            queryClient.invalidateQueries({ queryKey: ["balances", userId] }); // Balances change after trade
        },
    });

    const cancelOrder = useMutation({
        mutationFn: async (orderId: number) => {
            const response = await client.delete<Order>(`/users/${userId}/orders/${orderId}`);
            return response.data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["orders", userId] });
        },
    });

    return {
        orders: fetchOrders.data,
        isLoading: fetchOrders.isLoading,
        placeOrder,
        cancelOrder,
    };
};
