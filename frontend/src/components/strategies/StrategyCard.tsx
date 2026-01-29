import { motion } from "framer-motion";
import { Play, Pause, TrendingUp, Settings2, Trash2 } from "lucide-react";
import { cn } from "@/lib/utils";

interface StrategyCardProps {
    strategy: {
        id: number;
        name: string;
        type: "DCA" | "GRID" | "ARBITRAGE";
        status: "ACTIVE" | "PAUSED" | "STOPPED";
        symbol: string;
        pnlValue?: number;
        pnlPercent?: number;
    };
}

export function StrategyCard({ strategy }: StrategyCardProps) {
    const isActive = strategy.status === "ACTIVE";

    return (
        <motion.div
            layout
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            whileHover={{ y: -5 }}
            className="group relative rounded-3xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm transition-all hover:border-white/20 hover:bg-white/[0.07]"
        >
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                    <div className={cn(
                        "h-12 w-12 rounded-2xl flex items-center justify-center text-white shadow-lg",
                        strategy.type === "DCA" ? "bg-gradient-to-tr from-cyan-500 to-blue-600 shadow-cyan-500/20" : "bg-gradient-to-tr from-purple-500 to-pink-600 shadow-purple-500/20"
                    )}>
                        <Rocket className="h-6 w-6" />
                    </div>
                    <div>
                        <h3 className="font-bold text-white tracking-tight">{strategy.name}</h3>
                        <p className="text-xs text-zinc-500 font-medium uppercase tracking-wider">{strategy.symbol} • {strategy.type}</p>
                    </div>
                </div>

                <div className={cn(
                    "flex items-center gap-1.5 px-3 py-1 rounded-full border text-[10px] font-bold uppercase tracking-widest",
                    isActive
                        ? "bg-emerald-500/10 border-emerald-500/20 text-emerald-400"
                        : "bg-zinc-500/10 border-zinc-500/20 text-zinc-400"
                )}>
                    <div className={cn("h-1.5 w-1.5 rounded-full", isActive ? "bg-emerald-500 animate-pulse" : "bg-zinc-500")} />
                    {strategy.status}
                </div>
            </div>

            <div className="grid grid-cols-2 gap-4 mb-6">
                <div className="p-4 rounded-2xl bg-white/5 ring-1 ring-white/5">
                    <p className="text-[10px] text-zinc-500 uppercase font-bold tracking-widest mb-1">Unrealized PnL</p>
                    <div className={cn(
                        "text-lg font-bold flex items-center gap-1",
                        (strategy.pnlValue || 0) >= 0 ? "text-emerald-400" : "text-rose-400"
                    )}>
                        ${strategy.pnlValue?.toLocaleString() || "0.00"}
                        <span className="text-xs">({strategy.pnlPercent || 0}%)</span>
                    </div>
                </div>
                <div className="p-4 rounded-2xl bg-white/5 ring-1 ring-white/5">
                    <p className="text-[10px] text-zinc-500 uppercase font-bold tracking-widest mb-1">24h Trades</p>
                    <div className="text-lg font-bold text-white">
                        12 <span className="text-xs text-zinc-500 font-medium">Executed</span>
                    </div>
                </div>
            </div>

            <div className="flex items-center gap-2">
                <button className={cn(
                    "flex-1 flex items-center justify-center gap-2 py-3 rounded-xl font-bold transition-all",
                    isActive
                        ? "bg-white/5 text-white hover:bg-white/10 ring-1 ring-white/10"
                        : "bg-gradient-to-r from-cyan-500 to-blue-600 text-white shadow-lg shadow-cyan-500/20"
                )}>
                    {isActive ? <Pause className="h-4 w-4" /> : <Play className="h-4 w-4" />}
                    {isActive ? "Pause Bot" : "Start Bot"}
                </button>
                <button className="p-3 rounded-xl bg-white/5 text-zinc-400 hover:text-white hover:bg-white/10 ring-1 ring-white/10 transition-all">
                    <Settings2 className="h-5 w-5" />
                </button>
                <button className="p-3 rounded-xl bg-white/5 text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 ring-1 ring-rose-500/10 transition-all">
                    <Trash2 className="h-5 w-5" />
                </button>
            </div>
        </motion.div>
    );
}

import { Rocket } from "lucide-react";
