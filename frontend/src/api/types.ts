export type ExchangeType = "BYBIT" | "BINANCE" | "KRAKEN";

export interface Balance {
    id: number;
    userId: number;
    apiKeyId: number;
    exchangeType: ExchangeType;
    asset: string;
    freeBalance: number;
    lockedBalance: number;
    totalBalance: number;
    updatedAt: string;
}

export interface User {
    id: number;
    email: string;
    username: string;
    createdAt: string;
}

export interface TickerUpdate {
    symbol: string;
    price: number;
    timestamp: string;
    priceChange24h?: number;
}

export type StrategyType = "DCA" | "GRID" | "ARBITRAGE";
export type StrategyStatus = "ACTIVE" | "PAUSED" | "STOPPED";

export interface Strategy {
    id: number;
    userId: number;
    apiKeyId: number;
    name: string;
    type: StrategyType;
    status: StrategyStatus;
    symbol: string;
    parameters: Record<string, any>;
    createdAt: string;
    updatedAt: string;
    lastRunAt?: string;
}

export type OrderSide = "BUY" | "SELL";
export type OrderType = "LIMIT" | "MARKET";
export type OrderStatus = "NEW" | "PARTIALLY_FILLED" | "FILLED" | "CANCELLED" | "REJECTED";

export interface Order {
    id: number;
    userId: number;
    apiKeyId: number;
    exchangeType: ExchangeType;
    symbol: string;
    side: OrderSide;
    orderType: OrderType;
    status: OrderStatus;
    quantity: number;
    price?: number;
    filledQuantity: number;
    averagePrice?: number;
    createdAt: string;
    updatedAt: string;
}

export interface Trade {
    id: number;
    userId: number;
    orderId: number;
    exchangeType: ExchangeType;
    symbol: string;
    side: OrderSide;
    quantity: number;
    price: number;
    fee: number;
    feeCurrency: string;
    executedAt: string;
}
