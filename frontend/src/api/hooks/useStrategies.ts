import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import client, { DEFAULT_USER_ID } from "../client";
import type { Strategy, StrategyStatus } from "../types";

export const useStrategies = (userId: number = DEFAULT_USER_ID) => {
    const queryClient = useQueryClient();

    const fetchStrategies = useQuery({
        queryKey: ["strategies", userId],
        queryFn: async () => {
            const response = await client.get<Strategy[]>(`/users/${userId}/strategies`);
            return response.data;
        },
    });

    const createStrategy = useMutation({
        mutationFn: async (strategy: Partial<Strategy>) => {
            const response = await client.post<Strategy>(`/users/${userId}/strategies`, strategy);
            return response.data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["strategies", userId] });
        },
    });

    const toggleStatus = useMutation({
        mutationFn: async ({ id, status }: { id: number; status: StrategyStatus }) => {
            const response = await client.patch<Strategy>(`/users/${userId}/strategies/${id}/status`, null, {
                params: { status },
            });
            return response.data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["strategies", userId] });
        },
    });

    return {
        strategies: fetchStrategies.data,
        isLoading: fetchStrategies.isLoading,
        error: fetchStrategies.error,
        createStrategy,
        toggleStatus,
    };
};
