import { PortfolioCard } from "../components/dashboard/PortfolioCard";
import { TrendingUp, ArrowUpRight, ArrowDownRight, Activity } from "lucide-react";
import { motion } from "framer-motion";

export function DashboardPage() {
    return (
        <div className="space-y-8">
            <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
            >
                <h1 className="text-3xl font-bold text-white tracking-tight">System Overview</h1>
                <p className="text-zinc-500 mt-1">Real-time performance and portfolio metrics.</p>
            </motion.div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <StatCard
                    title="Total Balance"
                    value="$12,450.00"
                    change="+12.5%"
                    trend="up"
                    icon={TrendingUp}
                />
                <StatCard
                    title="Active Bots"
                    value="4"
                    change="+1 today"
                    trend="up"
                    icon={Activity}
                />
                <StatCard
                    title="24h Volume"
                    value="$842.12"
                    change="-2.1%"
                    trend="down"
                    icon={ArrowUpRight}
                />
                <StatCard
                    title="Success Rate"
                    value="94.2%"
                    change="+0.5%"
                    trend="up"
                    icon={TrendingUp}
                />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-2 rounded-3xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm">
                    <h3 className="text-lg font-semibold text-white mb-4">Portfolio Allocation</h3>
                    <PortfolioCard />
                </div>
                <div className="rounded-3xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm">
                    <h3 className="text-lg font-semibold text-white mb-4">Recent Notifications</h3>
                    <div className="space-y-4">
                        {[1, 2, 3].map((i) => (
                            <div key={i} className="flex gap-4 p-3 rounded-xl hover:bg-white/5 transition-colors cursor-pointer group">
                                <div className="h-10 w-10 shrink-0 rounded-lg bg-cyan-500/10 flex items-center justify-center text-cyan-500 ring-1 ring-cyan-500/20">
                                    <TrendingUp className="h-5 w-5" />
                                </div>
                                <div>
                                    <p className="text-sm font-medium text-white group-hover:text-cyan-400 transition-colors">BTC/USDT Strategy Buy</p>
                                    <p className="text-xs text-zinc-500">2 minutes ago • Completed</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}

function StatCard({ title, value, change, trend, icon: Icon }: any) {
    return (
        <motion.div
            whileHover={{ y: -4 }}
            className="group relative overflow-hidden rounded-3xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm transition-all hover:border-white/20 hover:bg-white/[0.07]"
        >
            <div className="absolute -right-4 -top-4 h-24 w-24 rounded-full bg-cyan-500/5 blur-3xl transition-all group-hover:bg-cyan-500/10" />

            <div className="flex items-center justify-between mb-4">
                <div className="h-10 w-10 rounded-xl bg-white/5 flex items-center justify-center ring-1 ring-white/10 text-cyan-400">
                    <Icon className="h-5 w-5" />
                </div>
                <div className={`flex items-center gap-1 text-xs font-semibold px-2 py-1 rounded-full ${trend === 'up' ? 'text-emerald-400 bg-emerald-400/10' : 'text-rose-400 bg-rose-400/10'
                    }`}>
                    {trend === 'up' ? <ArrowUpRight className="h-3 w-3" /> : <ArrowDownRight className="h-3 w-3" />}
                    {change}
                </div>
            </div>

            <div>
                <h4 className="text-sm font-medium text-zinc-400 mb-1">{title}</h4>
                <p className="text-2xl font-bold text-white">{value}</p>
            </div>
        </motion.div>
    );
}
