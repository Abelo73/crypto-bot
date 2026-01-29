import { Sidebar } from "./Sidebar";
import { Navbar } from "./Navbar";
import { PriceTicker } from "../dashboard/PriceTicker";
import { motion } from "framer-motion";

interface LayoutProps {
    children: React.ReactNode;
}

export function Layout({ children }: LayoutProps) {
    return (
        <div className="min-h-screen bg-[#050505] text-zinc-200 selection:bg-cyan-500/30">
            {/* Background gradients for extra wow factor */}
            <div className="fixed inset-0 overflow-hidden pointer-events-none">
                <div className="absolute -left-[10%] -top-[10%] h-[50%] w-[50%] rounded-full bg-cyan-500/10 blur-[120px] animate-pulse" />
                <div className="absolute -right-[10%] -bottom-[10%] h-[50%] w-[50%] rounded-full bg-blue-600/10 blur-[120px] animate-pulse" style={{ animationDelay: '2s' }} />
                <div className="absolute left-[20%] top-[30%] h-[30%] w-[30%] rounded-full bg-purple-600/5 blur-[120px]" />
            </div>

            <Sidebar />
            <Navbar />

            <main className="pl-64 pt-16 relative z-10">
                <PriceTicker />
                <motion.div
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.6, ease: "easeOut" }}
                    className="p-8"
                >
                    {children}
                </motion.div>
            </main>
        </div>
    );
}
