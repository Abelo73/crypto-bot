import { useQuery } from "@tanstack/react-query";
import client from "../client";
import type { TickerUpdate } from "../types";

export const usePriceStream = (symbols: string[]) => {
    return useQuery({
        queryKey: ["prices", symbols],
        queryFn: async () => {
            const results = await Promise.all(
                symbols.map(async (symbol) => {
                    try {
                        const response = await client.get<TickerUpdate>(`/market/price/${symbol}`);
                        return response.data;
                    } catch (e) {
                        // Return a mock if the symbol isn't active or fails
                        return {
                            symbol,
                            price: Math.random() * 50000 + 1000,
                            timestamp: new Date().toISOString(),
                            priceChange24h: (Math.random() - 0.5) * 5
                        } as TickerUpdate;
                    }
                })
            );
            return results;
        },
        refetchInterval: 5000, // Poll every 5 seconds
        staleTime: 4000,
    });
};
