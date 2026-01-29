import axios from "axios";

// Default user ID for development purposes
export const DEFAULT_USER_ID = 1;

const client = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
    headers: {
        "Content-Type": "application/json",
    },
});

// Add a request interceptor if we ever need auth tokens
client.interceptors.request.use(
    (config) => {
        // For now, no auth needed
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default client;
