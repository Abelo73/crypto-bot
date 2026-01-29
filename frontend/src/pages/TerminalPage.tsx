import { motion } from "framer-motion";
import { OrderWidget } from "@/components/terminal/OrderWidget";
import { TradeHistory } from "@/components/terminal/TradeHistory";
import { TradingViewChart } from "@/components/terminal/TradingViewChart";
import { BarChart2 } from "lucide-react";

export function TerminalPage() {
    return (
        <div className="h-[calc(100vh-6rem)] flex flex-col gap-4">
            <motion.div
                initial={{ opacity: 0, y: -10 }}
                animate={{ opacity: 1, y: 0 }}
                className="flex items-center justify-between"
            >
                <div className="flex items-center gap-4">
                    <div className="flex items-center gap-2">
                        <div className="h-8 w-8 rounded-lg bg-orange-500/10 flex items-center justify-center text-orange-500">
                            <span className="font-bold">₿</span>
                        </div>
                        <div>
                            <h1 className="text-xl font-bold text-white tracking-tight">BTC/USDT</h1>
                            <p className="text-xs text-zinc-500 font-medium">Bitcoin Spot</p>
                        </div>
                    </div>
                </div>
            </motion.div>

            <div className="flex-1 grid grid-cols-12 gap-4 min-h-0">
                {/* Main Chart Area */}
                <div className="col-span-12 lg:col-span-9 flex flex-col gap-4">
                    <div className="flex-1">
                        <TradingViewChart symbol="BTCUSDT" interval="5" />
                    </div>

                    <div className="h-64">
                        <TradeHistory />
                    </div>
                </div>

                {/* Right Panel: Order Book & Order Widget */}
                <div className="col-span-12 lg:col-span-3 flex flex-col gap-4">
                    <div className="flex-1 rounded-3xl border border-white/10 bg-white/5 p-4 backdrop-blur-sm flex flex-col">
                        <h3 className="text-xs font-bold text-zinc-500 uppercase tracking-wider mb-3 flex items-center gap-2">
                            <BarChart2 className="h-4 w-4" />
                            Order Book
                        </h3>
                        <div className="flex-1 flex flex-col gap-1 overflow-hidden relative">
                            {/* Mock Order Book */}
                            <div className="space-y-0.5 opacity-60">
                                {[...Array(8)].map((_, i) => (
                                    <div key={`ask-${i}`} className="flex justify-between text-xs text-rose-400">
                                        <span>{(45000 + i * 50).toFixed(2)}</span>
                                        <span className="text-zinc-500">{(Math.random() * 2).toFixed(4)}</span>
                                    </div>
                                )).reverse()}
                            </div>
                            <div className="py-2 text-center text-lg font-bold text-white my-auto">
                                45,124.50
                            </div>
                            <div className="space-y-0.5 opacity-60">
                                {[...Array(8)].map((_, i) => (
                                    <div key={`bid-${i}`} className="flex justify-between text-xs text-emerald-400">
                                        <span>{(45000 - i * 50).toFixed(2)}</span>
                                        <span className="text-zinc-500">{(Math.random() * 2).toFixed(4)}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>

                    <OrderWidget />
                </div>
            </div>
        </div>
    );
}
