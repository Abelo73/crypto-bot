# 🎨 Frontend Roadmap: Premium Dashboard UI

This document tracks the granular development tasks for the `crypto-bot` React dashboard, mapped to backend APIs.

## Phase A: Project Foundations [IN PROGRESS]
- [x] Bootstrap Vite + React + TypeScript
- [x] Configure Tailwind CSS v4 + Shadcn UI
- [ ] **Global Glassmorphism Layout Shell**
    - Implementation: Sidebar for navigation, Top Navbar for user/balance overview.
    - Technical: Use `framer-motion` for smooth transitions.

## Phase B: Real-time Data Layer [IN PROGRESS]
- [x] **API Layer Setup (Axios + TanStack Query)**
    - Config: Base URL `http://localhost:8080/api`.
    - Note: Implement interceptors for default `userId: 1` (dev mode).
- [x] **WebSocket/Polling Hook (`usePriceStream`)**
    - Backend: `GET /api/market/price/{symbol}`.
    - Technical: Poll the price endpoint or set up a dedicated WS connection if supported.

## Phase C: Unified Dashboard (Command Center) [IN PROGRESS]
- [x] **Multi-exchange Portfolio Card**
    - Backend: `GET /api/users/{userId}/balances`.
    - Implementation: Pie chart for asset distribution (Recharts).
- [x] **Global Price Ticker Scroll**
    - Backend: `GET /api/market/price/{symbol}`.
    - UI: Sliding neon ticker for BTC/USDT, ETH/USDT, etc.

## Phase D: Strategy Hub (Automated Bots)
- [ ] **Strategy Status Cards**
    - Backend: `GET /api/users/{userId}/strategies`.
    - Logic: Display PnL, Status (ACTIVE/PAUSED), and Type.
- [ ] **DCA Configuration Wizard**
    - Backend: `POST /api/users/{userId}/strategies`.
    - UI: Multi-step form using Shadcn components.
- [ ] **Bot Control Center**
    - Backend: `PATCH /api/users/{userId}/strategies/{strategyId}/status`.
    - Logic: Toggle switch to start/stop bots.

## Phase E: Social Hub (Copy Trading)
- [ ] **Follow Leaderboard**
    - Backend: `POST /api/users/{userId}/copy-trading/follow`.
    - Implementation: Search for traders and set scaling factor (1.0x, 2.0x, etc.).

## Phase F: Pro Trading Terminal
- [ ] **Lightning Order Widgets**
    - Backend: `POST /api/users/{userId}/orders`.
    - Support: MARKET and LIMIT orders.
- [ ] **Interactive Trade History**
    - Backend: `GET /api/users/{userId}/trades` (Executed) & `GET /api/users/{userId}/orders` (Open/Cancelled).
    - UI: Filterable data table with status badges.
- [ ] **API Key Manager**
    - Backend: `POST /api/users/{userId}/api-keys` & `GET /api/users/{userId}/api-keys`.
    - UI: Secure input modal and list of connected exchanges.
