import { ResponsiveContainer, PieChart, Pie, Cell, Tooltip } from 'recharts';
import { useBalances } from '@/api/hooks/useBalances';

const COLORS = ['#06b6d4', '#3b82f6', '#8b5cf6', '#ec4899', '#f43f5e', '#f59e0b'];

export function PortfolioCard() {
    const { data: balances, isLoading } = useBalances();

    const data = balances?.filter(b => b.totalBalance > 0).map(b => ({
        name: b.asset,
        value: b.totalBalance
    })) || [
            { name: 'BTC', value: 4500 },
            { name: 'ETH', value: 3000 },
            { name: 'SOL', value: 1500 },
            { name: 'USDT', value: 2450 },
        ];

    if (isLoading) {
        return (
            <div className="h-64 flex items-center justify-center border border-dashed border-white/20 rounded-2xl text-zinc-500 animate-pulse">
                Loading Assets...
            </div>
        );
    }

    return (
        <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                    <Pie
                        data={data}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={80}
                        paddingAngle={8}
                        dataKey="value"
                    >
                        {data.map((_, index) => (
                            <Cell
                                key={`cell-${index}`}
                                fill={COLORS[index % COLORS.length]}
                                stroke="transparent"
                                style={{ filter: `drop-shadow(0 0 8px ${COLORS[index % COLORS.length]}40)` }}
                            />
                        ))}
                    </Pie>
                    <Tooltip
                        contentStyle={{
                            backgroundColor: 'rgba(0, 0, 0, 0.8)',
                            border: '1px solid rgba(255, 255, 255, 0.1)',
                            borderRadius: '12px',
                            backdropFilter: 'blur(8px)',
                            color: '#fff'
                        }}
                        itemStyle={{ color: '#fff' }}
                    />
                </PieChart>
            </ResponsiveContainer>

            <div className="mt-4 grid grid-cols-2 gap-2">
                {data.map((_, index) => (
                    <div key={data[index].name} className="flex items-center gap-2">
                        <div
                            className="h-2 w-2 rounded-full"
                            style={{ backgroundColor: COLORS[index % COLORS.length] }}
                        />
                        <span className="text-xs text-zinc-400 font-medium">{data[index].name}</span>
                        <span className="text-xs text-white ml-auto">${data[index].value.toLocaleString()}</span>
                    </div>
                ))}
            </div>
        </div>
    );
}
