import { usePriceStream } from "@/api/hooks/usePriceStream";
import { motion } from "framer-motion";
import { TrendingUp, TrendingDown } from "lucide-react";

const SYMBOLS = ["BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT", "XRPUSDT"];

export function PriceTicker() {
    const { data: prices } = usePriceStream(SYMBOLS);

    return (
        <div className="w-full bg-black/40 border-y border-white/5 py-2 overflow-hidden flex items-center">
            <motion.div
                animate={{ x: [0, -1000] }}
                transition={{
                    duration: 30,
                    repeat: Infinity,
                    ease: "linear",
                }}
                className="flex gap-12 whitespace-nowrap px-8"
            >
                {/* Duplicate items for a seamless loop */}
                {[...(prices || []), ...(prices || [])].map((ticker, i) => (
                    <div key={`${ticker.symbol}-${i}`} className="flex items-center gap-2 group">
                        <span className="text-xs font-bold text-zinc-500 uppercase">{ticker.symbol.replace("USDT", "")}</span>
                        <span className="text-sm font-mono font-semibold text-white">
                            {ticker.price ? `$${ticker.price.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : "$0.00"}
                        </span>
                        <span className={`flex items-center text-[10px] font-bold ${(ticker.priceChange24h || 0) >= 0 ? 'text-emerald-400' : 'text-rose-400'
                            }`}>
                            {(ticker.priceChange24h || 0) >= 0 ? <TrendingUp className="h-3 w-3 mr-0.5" /> : <TrendingDown className="h-3 w-3 mr-0.5" />}
                            {Math.abs(ticker.priceChange24h || 0).toFixed(2)}%
                        </span>
                        <div className={`h-1.5 w-1.5 rounded-full ${(ticker.priceChange24h || 0) >= 0 ? 'bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.5)]' : 'bg-rose-500 shadow-[0_0_8px_rgba(244,63,94,0.5)]'
                            }`} />
                    </div>
                ))}
            </motion.div>
        </div>
    );
}
