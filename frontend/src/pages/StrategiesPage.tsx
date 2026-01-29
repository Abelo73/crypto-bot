import { useState } from "react";
import { motion } from "framer-motion";
import { Plus, Rocket } from "lucide-react";
import { useStrategies } from "@/api/hooks/useStrategies";
import { StrategyCard } from "@/components/strategies/StrategyCard";
import { CreateStrategyModal } from "@/components/strategies/CreateStrategyModal";

export function StrategiesPage() {
    const { strategies, isLoading } = useStrategies();
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

    return (
        <div className="space-y-8">
            <div className="flex items-center justify-between">
                <motion.div
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                >
                    <h1 className="text-3xl font-bold text-white tracking-tight">Strategy Hub</h1>
                    <p className="text-zinc-500 mt-1">Deploy and manage your automated trading bots.</p>
                </motion.div>

                <motion.button
                    onClick={() => setIsCreateModalOpen(true)}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    className="flex items-center gap-2 bg-gradient-to-r from-cyan-500 to-blue-600 px-6 py-3 rounded-2xl text-white font-bold shadow-lg shadow-cyan-500/20 hover:shadow-cyan-500/40 transition-all"
                >
                    <Plus className="h-5 w-5" />
                    New Strategy
                </motion.button>
            </div>

            <CreateStrategyModal isOpen={isCreateModalOpen} onClose={() => setIsCreateModalOpen(false)} />

            {isLoading ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {[1, 2, 3].map((i) => (
                        <div key={i} className="h-64 rounded-3xl bg-white/5 animate-pulse" />
                    ))}
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {strategies && strategies.length > 0 ? (
                        strategies.map((strategy) => (
                            <StrategyCard key={strategy.id} strategy={strategy} />
                        ))
                    ) : (
                        <div className="col-span-full py-20 flex flex-col items-center justify-center bg-white/5 rounded-3xl border border-dashed border-white/10 backdrop-blur-sm">
                            <div className="h-16 w-16 rounded-2xl bg-white/5 flex items-center justify-center mb-4 text-zinc-500">
                                <Rocket className="h-8 w-8" />
                            </div>
                            <h3 className="text-lg font-semibold text-white">No active strategies</h3>
                            <p className="text-zinc-500 max-w-xs text-center mt-2">Create your first DCA or Grid strategy to start automated trading.</p>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
