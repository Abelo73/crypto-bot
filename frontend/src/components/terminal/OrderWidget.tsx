import { useState } from "react";
import { useOrders } from "@/api/hooks/useOrders";
import { cn } from "@/lib/utils";
import { motion, AnimatePresence } from "framer-motion";
import { ArrowDown, ArrowUp, Loader2 } from "lucide-react";

export function OrderWidget() {
    const { placeOrder } = useOrders();
    const [side, setSide] = useState<"BUY" | "SELL">("BUY");
    const [type, setType] = useState<"LIMIT" | "MARKET">("LIMIT");
    const [symbol, setSymbol] = useState("BTCUSDT");
    const [price, setPrice] = useState("");
    const [quantity, setQuantity] = useState("");

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await placeOrder.mutateAsync({
                exchangeType: "BYBIT",
                symbol: symbol.toUpperCase(),
                side,
                orderType: type,
                quantity: parseFloat(quantity),
                price: type === "LIMIT" ? parseFloat(price) : undefined,
            });
            // Reset form on success
            setPrice("");
            setQuantity("");
        } catch (error) {
            console.error("Failed to place order", error);
        }
    };

    return (
        <div className="rounded-3xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm">
            <h3 className="text-lg font-bold text-white mb-4 flex items-center gap-2">
                Place Order
            </h3>

            <div className="flex p-1 bg-black/20 rounded-xl mb-6">
                <button
                    onClick={() => setSide("BUY")}
                    className={cn(
                        "flex-1 py-2 rounded-lg text-sm font-bold transition-all",
                        side === "BUY" ? "bg-emerald-500 text-white shadow-lg shadow-emerald-500/20" : "text-zinc-500 hover:text-white"
                    )}
                >
                    Buy
                </button>
                <button
                    onClick={() => setSide("SELL")}
                    className={cn(
                        "flex-1 py-2 rounded-lg text-sm font-bold transition-all",
                        side === "SELL" ? "bg-rose-500 text-white shadow-lg shadow-rose-500/20" : "text-zinc-500 hover:text-white"
                    )}
                >
                    Sell
                </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
                <div className="flex gap-2 mb-2">
                    <button
                        type="button"
                        onClick={() => setType("LIMIT")}
                        className={cn(
                            "px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors",
                            type === "LIMIT"
                                ? "border-cyan-500 text-cyan-500 bg-cyan-500/10"
                                : "border-white/10 text-zinc-400 hover:border-white/20"
                        )}
                    >
                        Limit
                    </button>
                    <button
                        type="button"
                        onClick={() => setType("MARKET")}
                        className={cn(
                            "px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors",
                            type === "MARKET"
                                ? "border-cyan-500 text-cyan-500 bg-cyan-500/10"
                                : "border-white/10 text-zinc-400 hover:border-white/20"
                        )}
                    >
                        Market
                    </button>
                </div>

                <div>
                    <label className="text-xs text-zinc-500 font-bold uppercase tracking-wider mb-1.5 block">Symbol</label>
                    <input
                        type="text"
                        value={symbol}
                        onChange={(e) => setSymbol(e.target.value.toUpperCase())}
                        className="w-full rounded-xl bg-black/40 border border-white/10 px-4 py-3 text-white focus:border-cyan-500/50 focus:outline-none transition-all font-mono"
                        placeholder="BTCUSDT"
                    />
                </div>

                <AnimatePresence mode="wait">
                    {type === "LIMIT" && (
                        <motion.div
                            initial={{ height: 0, opacity: 0 }}
                            animate={{ height: "auto", opacity: 1 }}
                            exit={{ height: 0, opacity: 0 }}
                            className="overflow-hidden"
                        >
                            <label className="text-xs text-zinc-500 font-bold uppercase tracking-wider mb-1.5 block">Price (USDT)</label>
                            <div className="relative">
                                <input
                                    type="number"
                                    value={price}
                                    onChange={(e) => setPrice(e.target.value)}
                                    className="w-full rounded-xl bg-black/40 border border-white/10 px-4 py-3 text-white focus:border-cyan-500/50 focus:outline-none transition-all font-mono"
                                    placeholder="0.00"
                                />
                                <span className="absolute right-4 top-1/2 -translate-y-1/2 text-zinc-600 text-xs">USDT</span>
                            </div>
                        </motion.div>
                    )}
                </AnimatePresence>

                <div>
                    <label className="text-xs text-zinc-500 font-bold uppercase tracking-wider mb-1.5 block">Quantity</label>
                    <div className="relative">
                        <input
                            type="number"
                            value={quantity}
                            onChange={(e) => setQuantity(e.target.value)}
                            className="w-full rounded-xl bg-black/40 border border-white/10 px-4 py-3 text-white focus:border-cyan-500/50 focus:outline-none transition-all font-mono"
                            placeholder="0.00"
                        />
                        <span className="absolute right-4 top-1/2 -translate-y-1/2 text-zinc-600 text-xs">{symbol.replace("USDT", "")}</span>
                    </div>
                </div>

                <button
                    type="submit"
                    disabled={placeOrder.isPending}
                    className={cn(
                        "w-full py-4 rounded-xl font-bold text-white shadow-lg transition-all flex items-center justify-center gap-2 mt-4",
                        side === "BUY"
                            ? "bg-gradient-to-r from-emerald-500 to-teal-600 shadow-emerald-500/20 hover:shadow-emerald-500/40"
                            : "bg-gradient-to-r from-rose-500 to-pink-600 shadow-rose-500/20 hover:shadow-rose-500/40",
                        placeOrder.isPending && "opacity-70 cursor-wait"
                    )}
                >
                    {placeOrder.isPending ? (
                        <Loader2 className="h-5 w-5 animate-spin" />
                    ) : side === "BUY" ? (
                        <ArrowDown className="h-5 w-5" />
                    ) : (
                        <ArrowUp className="h-5 w-5" />
                    )}
                    {side} {symbol.replace("USDT", "")}
                </button>
            </form>
        </div>
    );
}
