-- Test Data Seed Script for Crypto Trading Platform
-- Run this against your PostgreSQL database to populate it with test data

-- 1. Create a test user
INSERT INTO users (id, email, username) 
VALUES (1, 'trader@example.com', 'test_trader')
ON CONFLICT (id) DO NOTHING;

-- 2. Create a placeholder Bybit API Key
-- Note: These are dummy encrypted values. To test real exchange connectivity, 
-- use the Postman 'Add API Key' request with your actual Bybit Testnet keys.
INSERT INTO api_keys (id, user_id, exchange_type, api_key_encrypted, api_secret_encrypted, label, is_active)
VALUES (1, 1, 'BYBIT', 'dummy_encrypted_key', 'dummy_encrypted_secret', 'Bybit Testnet', true)
ON CONFLICT (id) DO NOTHING;

-- 3. Add dummy balances
INSERT INTO balances (user_id, api_key_id, exchange_type, asset, free_balance, locked_balance, total_balance)
VALUES 
(1, 1, 'BYBIT', 'BTC', 0.50000000, 0.10000000, 0.60000000),
(1, 1, 'BYBIT', 'USDT', 10000.00000000, 2000.00000000, 12000.00000000),
(1, 1, 'BYBIT', 'ETH', 5.00000000, 0.00000000, 5.00000000)
ON CONFLICT (user_id, api_key_id, asset) DO UPDATE SET
    free_balance = EXCLUDED.free_balance,
    locked_balance = EXCLUDED.locked_balance,
    total_balance = EXCLUDED.total_balance;

-- 4. Add some dummy orders
INSERT INTO orders (id, user_id, api_key_id, exchange_type, exchange_order_id, symbol, order_type, side, quantity, price, status, filled_quantity, average_price)
VALUES 
(1, 1, 1, 'BYBIT', 'order-12345', 'BTCUSDT', 'LIMIT', 'BUY', 0.01, 40000.00, 'FILLED', 0.01, 39950.00),
(2, 1, 1, 'BYBIT', 'order-67890', 'BTCUSDT', 'LIMIT', 'SELL', 0.005, 45000.00, 'NEW', 0.00, NULL),
(3, 1, 1, 'BYBIT', 'order-abcde', 'ETHUSDT', 'MARKET', 'BUY', 1.0, NULL, 'FILLED', 1.0, 2200.00)
ON CONFLICT (id) DO NOTHING;

-- 5. Add some dummy trades (executions)
INSERT INTO trades (id, order_id, user_id, exchange_type, exchange_trade_id, symbol, side, quantity, price, executed_at)
VALUES 
(1, 1, 1, 'BYBIT', 'trade-111', 'BTCUSDT', 'BUY', 0.01, 39950.00, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(2, 3, 1, 'BYBIT', 'trade-222', 'ETHUSDT', 'BUY', 1.0, 2200.00, CURRENT_TIMESTAMP - INTERVAL '2 hours')
ON CONFLICT (id) DO NOTHING;

-- Reset sequences for future inserts
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('api_keys_id_seq', (SELECT MAX(id) FROM api_keys));
SELECT setval('orders_id_seq', (SELECT MAX(id) FROM orders));
SELECT setval('balances_id_seq', (SELECT MAX(id) FROM balances));
SELECT setval('trades_id_seq', (SELECT MAX(id) FROM trades));
