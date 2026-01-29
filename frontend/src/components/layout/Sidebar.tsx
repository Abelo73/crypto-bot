import { LayoutDashboard, Rocket, Users, Terminal, Settings, Activity } from "lucide-react";
import { cn } from "@/lib/utils";
import { motion } from "framer-motion";
import { Link, useLocation } from "react-router-dom";

const navItems = [
    { icon: LayoutDashboard, label: "Dashboard", href: "/" },
    { icon: Rocket, label: "Strategies", href: "/strategies" },
    { icon: Users, label: "Social", href: "/social" },
    { icon: Terminal, label: "Terminal", href: "/terminal" },
    { icon: Settings, label: "Settings", href: "/settings" },
];

export function Sidebar() {
    const location = useLocation();

    return (
        <aside className="fixed left-0 top-0 z-40 h-screen w-64 border-r border-white/5 bg-black/40 backdrop-blur-2xl">
            <div className="flex h-full flex-col p-6">
                <div className="mb-10 flex items-center gap-3">
                    <motion.div
                        whileHover={{ rotate: 180 }}
                        transition={{ duration: 0.5 }}
                        className="h-9 w-9 rounded-xl bg-gradient-to-tr from-cyan-500 to-blue-600 shadow-lg shadow-cyan-500/20 flex items-center justify-center"
                    >
                        <Activity className="h-5 w-5 text-white" />
                    </motion.div>
                    <span className="text-xl font-bold tracking-tight text-white bg-clip-text">CryptoBot</span>
                </div>

                <nav className="space-y-1">
                    {navItems.map((item) => {
                        const isActive = location.pathname === item.href;
                        return (
                            <Link
                                key={item.label}
                                to={item.href}
                            >
                                <motion.div
                                    whileHover={{ x: 4 }}
                                    whileTap={{ scale: 0.98 }}
                                    className={cn(
                                        "group relative flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition-all duration-200",
                                        isActive
                                            ? "bg-white/10 text-white shadow-sm ring-1 ring-white/20"
                                            : "text-zinc-400 hover:bg-white/5 hover:text-white"
                                    )}
                                >
                                    {isActive && (
                                        <motion.div
                                            layoutId="active-nav"
                                            className="absolute inset-0 rounded-xl bg-gradient-to-r from-cyan-500/10 to-transparent"
                                        />
                                    )}
                                    <item.icon className={cn(
                                        "h-5 w-5 transition-colors z-10",
                                        isActive ? "text-cyan-400" : "text-zinc-400 group-hover:text-cyan-400"
                                    )} />
                                    <span className="z-10">{item.label}</span>
                                </motion.div>
                            </Link>
                        );
                    })}
                </nav>

                <div className="mt-auto overflow-hidden rounded-2xl bg-white/5 p-4 ring-1 ring-white/10 backdrop-blur-sm relative group">
                    <div className="absolute inset-0 bg-gradient-to-br from-cyan-500/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
                    <div className="relative z-10">
                        <div className="mb-2 text-[10px] font-bold uppercase tracking-widest text-zinc-500">
                            System Status
                        </div>
                        <div className="flex items-center gap-2 text-sm text-zinc-300">
                            <span className="relative flex h-2 w-2">
                                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                                <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
                            </span>
                            Live Sync: Running
                        </div>
                    </div>
                </div>
            </div>
        </aside>
    );
}
