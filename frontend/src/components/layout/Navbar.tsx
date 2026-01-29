import { Bell, Search, User, Wallet } from "lucide-react";
import { useBalances } from "@/api/hooks/useBalances";
import { motion } from "framer-motion";

export function Navbar() {
    const { data: balances } = useBalances();

    const totalValue = balances?.reduce((acc, curr) => acc + curr.totalBalance, 0) || 0;

    return (
        <header className="fixed left-64 right-0 top-0 z-30 h-16 border-b border-white/5 bg-black/20 backdrop-blur-xl">
            <div className="flex h-full items-center justify-between px-8">
                <div className="relative w-96 group">
                    <Search className="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-zinc-500 group-focus-within:text-cyan-500 transition-colors" />
                    <input
                        type="text"
                        placeholder="Search symbols, strategies..."
                        className="w-full rounded-2xl bg-white/5 py-2.5 pl-11 pr-4 text-sm text-zinc-200 ring-1 ring-white/10 transition-all focus:bg-white/10 focus:outline-none focus:ring-2 focus:ring-cyan-500/50"
                    />
                </div>

                <div className="flex items-center gap-6">
                    <div className="flex items-center gap-3 px-4 py-2 rounded-2xl bg-white/5 ring-1 ring-white/10 backdrop-blur-sm">
                        <div className="h-8 w-8 rounded-lg bg-cyan-500/10 flex items-center justify-center text-cyan-400">
                            <Wallet className="h-4 w-4" />
                        </div>
                        <div className="flex flex-col">
                            <span className="text-[10px] text-zinc-500 uppercase font-bold tracking-widest">Available Balance</span>
                            <motion.span
                                key={totalValue}
                                initial={{ opacity: 0, y: -10 }}
                                animate={{ opacity: 1, y: 0 }}
                                className="text-sm font-bold text-white"
                            >
                                ${totalValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </motion.span>
                        </div>
                    </div>

                    <div className="flex items-center gap-2">
                        <motion.button
                            whileHover={{ scale: 1.1 }}
                            whileTap={{ scale: 0.9 }}
                            className="relative rounded-xl p-2.5 text-zinc-400 transition-colors hover:bg-white/5 hover:text-white ring-1 ring-white/5"
                        >
                            <Bell className="h-5 w-5" />
                            <span className="absolute right-2.5 top-2.5 h-2 w-2 rounded-full bg-cyan-500 shadow-[0_0_8px_rgba(6,182,212,0.8)]" />
                        </motion.button>

                        <div className="h-8 w-px bg-white/10 mx-2" />

                        <motion.div
                            whileHover={{ x: -4 }}
                            className="flex items-center gap-3 cursor-pointer group"
                        >
                            <div className="flex flex-col items-end">
                                <span className="text-sm font-bold text-white group-hover:text-cyan-400 transition-colors">Dev User</span>
                                <span className="text-[10px] text-zinc-500 uppercase tracking-tighter font-medium">Pro member</span>
                            </div>
                            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-zinc-800 to-black ring-1 ring-white/10 group-hover:ring-cyan-500/50 transition-all shadow-lg overflow-hidden">
                                <User className="h-5 w-5 text-zinc-400 group-hover:text-cyan-400 transition-colors" />
                            </div>
                        </motion.div>
                    </div>
                </div>
            </div>
        </header>
    );
}
