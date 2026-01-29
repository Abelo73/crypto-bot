import { useQuery } from "@tanstack/react-query";
import client, { DEFAULT_USER_ID } from "../client";
import type { Balance } from "../types";

export const useBalances = (userId: number = DEFAULT_USER_ID) => {
    return useQuery({
        queryKey: ["balances", userId],
        queryFn: async () => {
            const response = await client.get<Balance[]>(`/users/${userId}/balances`);
            return response.data;
        },
    });
};
