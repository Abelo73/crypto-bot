import { useOrders } from "@/api/hooks/useOrders";
import { cn } from "@/lib/utils";
import { History, XCircle } from "lucide-react";
import { format } from "date-fns";
import { motion, AnimatePresence } from "framer-motion";

export function TradeHistory() {
    const { orders, cancelOrder, isLoading } = useOrders();

    return (
        <div className="rounded-3xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm h-full flex flex-col">
            <h3 className="text-lg font-bold text-white mb-4 flex items-center gap-2">
                <History className="h-5 w-5 text-cyan-400" />
                Order History
            </h3>

            <div className="flex-1 overflow-auto -mx-2 px-2">
                <table className="w-full text-left text-sm">
                    <thead className="text-xs text-zinc-500 font-bold uppercase tracking-wider sticky top-0 bg-[#0c0c0c] z-10">
                        <tr>
                            <th className="pb-4 pt-2">Time</th>
                            <th className="pb-4 pt-2">Symbol</th>
                            <th className="pb-4 pt-2">Side</th>
                            <th className="pb-4 pt-2">Type</th>
                            <th className="pb-4 pt-2 text-right">Price</th>
                            <th className="pb-4 pt-2 text-right">Qty</th>
                            <th className="pb-4 pt-2 text-center">Status</th>
                            <th className="pb-4 pt-2 text-right">Action</th>
                        </tr>
                    </thead>
                    <tbody className="space-y-2">
                        <AnimatePresence initial={false}>
                            {orders?.map((order) => (
                                <motion.tr
                                    key={order.id}
                                    layout
                                    initial={{ opacity: 0, x: -10 }}
                                    animate={{ opacity: 1, x: 0 }}
                                    exit={{ opacity: 0, x: -10 }}
                                    className="group border-b border-white/5 hover:bg-white/5 transition-colors"
                                >
                                    <td className="py-3 text-zinc-400 font-mono text-xs">
                                        {format(new Date(order.createdAt), "HH:mm:ss")}
                                    </td>
                                    <td className="py-3 font-bold text-white">{order.symbol}</td>
                                    <td className="py-3">
                                        <span className={cn(
                                            "px-2 py-0.5 rounded text-[10px] font-bold uppercase",
                                            order.side === "BUY" ? "bg-emerald-500/10 text-emerald-400" : "bg-rose-500/10 text-rose-400"
                                        )}>
                                            {order.side}
                                        </span>
                                    </td>
                                    <td className="py-3 text-zinc-400 text-xs">{order.orderType}</td>
                                    <td className="py-3 text-right font-mono text-white">
                                        {order.averagePrice ? `$${order.averagePrice.toFixed(2)}` : "-"}
                                    </td>
                                    <td className="py-3 text-right font-mono text-zinc-300">
                                        {order.filledQuantity}/{order.quantity}
                                    </td>
                                    <td className="py-3 text-center">
                                        <StatusBadge status={order.status} />
                                    </td>
                                    <td className="py-3 text-right">
                                        {order.status === "NEW" || order.status === "PARTIALLY_FILLED" ? (
                                            <button
                                                onClick={() => cancelOrder.mutate(order.id)}
                                                disabled={cancelOrder.isPending}
                                                className="p-1 rounded hover:bg-rose-500/20 text-zinc-500 hover:text-rose-400 transition-colors"
                                            >
                                                <XCircle className="h-4 w-4" />
                                            </button>
                                        ) : null}
                                    </td>
                                </motion.tr>
                            ))}
                        </AnimatePresence>
                    </tbody>
                </table>

                {!isLoading && (!orders || orders.length === 0) && (
                    <div className="flex flex-col items-center justify-center h-40 text-zinc-500">
                        <History className="h-8 w-8 mb-2 opacity-20" />
                        <p className="text-sm">No orders found</p>
                    </div>
                )}
            </div>
        </div>
    );
}

function StatusBadge({ status }: { status: string }) {
    const styles = {
        NEW: "bg-blue-500/10 text-blue-400",
        PARTIALLY_FILLED: "bg-yellow-500/10 text-yellow-400",
        FILLED: "bg-emerald-500/10 text-emerald-400",
        CANCELLED: "bg-zinc-500/10 text-zinc-400",
        REJECTED: "bg-rose-500/10 text-rose-400",
    }[status] || "bg-zinc-500/10 text-zinc-400";

    return (
        <span className={cn("px-2 py-0.5 rounded-full text-[10px] font-bold uppercase", styles)}>
            {status.replace("_", " ")}
        </span>
    );
}
