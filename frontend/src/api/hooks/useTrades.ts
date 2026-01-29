import { useQuery } from "@tanstack/react-query";
import client, { DEFAULT_USER_ID } from "../client";
import type { Trade } from "../types";

export const useTrades = (userId: number = DEFAULT_USER_ID) => {
    const fetchTrades = useQuery({
        queryKey: ["trades", userId],
        queryFn: async () => {
            const response = await client.get<Trade[]>(`/users/${userId}/trades`);
            return response.data;
        },
        refetchInterval: 10000,
    });

    return {
        trades: fetchTrades.data,
        isLoading: fetchTrades.isLoading,
    };
};
