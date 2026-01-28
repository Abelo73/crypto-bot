# 🎨 CryptoBot Premium Dashboard

A high-performance, real-time trading interface for the `crypto-bot` ecosystem. Built with a focus on stunning aesthetics, lightning-fast interactions, and social trading transparency.

## 🚀 Technology Stack

-   **Framework**: [React 18+](https://reactjs.org/) with **Vite** for ultra-fast builds.
-   **Language**: **TypeScript** for rock-solid type safety.
-   **Styling**: **Tailwind CSS** + **Shadcn UI** for a premium, accessible component library.
-   **Icons**: [Lucide React](https://lucide.dev/) for clean, minimalist iconography.
-   **Data Visualization**: [Recharts](https://recharts.org/) for real-time asset & performance charts.
-   **State Management**: **Zustand** or **React Context** for lightweight, global state.

## 💎 Design Language: "Glassmorphism & Neon"

The UI embraces the "Modern Crypto" aesthetic:
-   **Translucency**: Glass-like cards with varying blur effects (`backdrop-blur`).
-   **Depth**: Subtle border gradients and elevated shadows to create a multi-layered experience.
-   **Color Palette**:
    -   `Deep Space`: High-contrast dark background.
    -   `Crypto Neon`: Vibrant Cyan (#00f2ff) and Electric Purple (#bb00ff) accents.
    -   `Market Sentiment`: Emerald Green for gains, Crimson Red for losses.

## 🏛️ Core Views & Features

### 1. Unified Dashboard (The Command Center)
-   **Portfolio Overview**: Real-time aggregate balance across all linked exchanges (Bybit, etc.).
-   **Live Pulse**: A mini price ticker feed directly from the WebSocket service.
-   **Activity Feed**: Instant notifications for trade execution and strategy triggers.

### 2. Strategy Hub (Automation)
-   **Active Bots Grid**: Interactive cards for each strategy (e.g., DCA, Grid Trading).
-   **Visual Config**: Easy-to-use forms for setting intervals, amounts, and thresholds.
-   **Performance Tracking**: Profit/Loss metrics specifically for each bot.

### 3. Social Trading (Copy Trading)
-   **Follower Management**: View who you are following with real-time mirroring status.
-   **Scale Management**: Drag-and-drop or slider controls to adjust your scale factor (e.g., 0.5x vs 2.0x).

### 4. Direct Terminal
-   **Quick Trade**: A streamlined "One-Click" trading interface for manual MARKET/LIMIT orders.

---

## 🛠️ Architectural Deep Dive: "How it Works"

### 1. The Real-time Pulse (WebSocket Hook)
-   **Mechanism**: A custom `usePriceStream` React hook will initialize a single `WebSocket` instance connecting to `ws://localhost:8080/api/market/stream`.
-   **State Sync**: Incoming ticker packets (BTC, ETH, etc.) will be decoded and stored in a **Zustand store** with an `updatesPerSecond` throttle to prevent UI stuttering.
-   **Visual Feedback**: Components across the app (like the Ticker Carousel and Portfolio Balance) will subscribe to this store for sub-second updates.

### 2. The Glassmorphism Chassis
-   **Design System**: Using **Tailwind CSS**, we'll implement a custom "Glass Chassis" utility class.
-   **Aesthetics**: 
    -   `bg-opacity-10`: Translucent background.
    -   `backdrop-blur-md`: Silky blur effect.
    -   `border-white/20`: Subtle separator lines.
-   **Animation**: `framer-motion` will handle layout shifts, making card interactions feel fluid and premium.

### 3. Strategy Hub Logic
-   **Config Wizard**: A multi-step form built with `Shadcn Form` (using `Zod` for validation).
-   **Live Preview**: As you adjust DCA parameters (e.g., $10 every 4 hours), the UI will calculate and display a "Projected 30-Day Accumulation" graph in real-time.
-   **Control Logic**: "Start/Stop" buttons will trigger `PATCH` requests to the backend, which instantly tells the Java Strategy Engine to begin or cease order execution.

### 4. Social Mirroring Dashboard
-   **Scaling Slider**: A custom `Shadcn Slider` that allows you to adjust your mirroring weight (e.g., set to 1.5x to trade 50% more than the master).
-   **Risk Gauge**: A visual feedback component that turns from Green to Red as your scale factor or leverage increases, warning the user of high risk.

---

*Status: Technical blueprint complete. Awaiting bootstrap signal.*
