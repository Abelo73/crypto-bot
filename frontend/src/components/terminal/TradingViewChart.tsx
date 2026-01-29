import { useEffect, useRef, useState } from 'react';
import { createChart, ColorType } from 'lightweight-charts';
import type { IChartApi, ISeriesApi, CandlestickData, Time } from 'lightweight-charts';
import axios from 'axios';

interface TradingViewChartProps {
    symbol: string;
    interval?: string;
}

interface CandleData {
    symbol: string;
    interval: string;
    openTime: number;
    open: number;
    high: number;
    low: number;
    close: number;
    volume: number;
}

export function TradingViewChart({ symbol, interval = '5' }: TradingViewChartProps) {
    const chartContainerRef = useRef<HTMLDivElement>(null);
    const chartRef = useRef<IChartApi | null>(null);
    const candlestickSeriesRef = useRef<ISeriesApi<'Candlestick'> | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!chartContainerRef.current) return;

        // Create chart
        const chart = createChart(chartContainerRef.current, {
            layout: {
                background: { type: ColorType.Solid, color: 'transparent' },
                textColor: '#9CA3AF',
            },
            grid: {
                vertLines: { color: 'rgba(255, 255, 255, 0.05)' },
                horzLines: { color: 'rgba(255, 255, 255, 0.05)' },
            },
            crosshair: {
                mode: 1,
            },
            rightPriceScale: {
                borderColor: 'rgba(255, 255, 255, 0.1)',
            },
            timeScale: {
                borderColor: 'rgba(255, 255, 255, 0.1)',
                timeVisible: true,
                secondsVisible: false,
            },
            width: chartContainerRef.current.clientWidth,
            height: chartContainerRef.current.clientHeight,
        });

        const candlestickSeries = chart.addCandlestickSeries({
            upColor: '#10b981',
            downColor: '#ef4444',
            borderVisible: false,
            wickUpColor: '#10b981',
            wickDownColor: '#ef4444',
        });

        chartRef.current = chart;
        candlestickSeriesRef.current = candlestickSeries;

        // Handle resize
        const handleResize = () => {
            if (chartContainerRef.current && chartRef.current) {
                chart.applyOptions({
                    width: chartContainerRef.current.clientWidth,
                    height: chartContainerRef.current.clientHeight,
                });
            }
        };

        window.addEventListener('resize', handleResize);

        // Fetch initial data
        fetchCandleData();

        return () => {
            window.removeEventListener('resize', handleResize);
            chart.remove();
        };
    }, [symbol, interval]);

    const fetchCandleData = async () => {
        try {
            setLoading(true);
            setError(null);

            const response = await axios.get<CandleData[]>(`http://localhost:8080/api/market/candles`, {
                params: {
                    symbol,
                    interval,
                    limit: 200,
                },
            });

            const candles: CandlestickData<Time>[] = response.data.map((candle) => ({
                time: (candle.openTime / 1000) as Time,
                open: candle.open,
                high: candle.high,
                low: candle.low,
                close: candle.close,
            }));

            if (candlestickSeriesRef.current) {
                candlestickSeriesRef.current.setData(candles);
            }

            console.log('TradingView chart initialized with', candles.length, 'candles');
            setLoading(false);
        } catch (err) {
            console.error('Failed to fetch candle data:', err);
            setError('Failed to load chart data');
            setLoading(false);
        }
    };

    return (
        <div className="relative w-full h-full rounded-3xl border border-white/10 bg-white/5 p-1 backdrop-blur-sm overflow-hidden">
            {loading && (
                <div className="absolute inset-0 flex items-center justify-center bg-black/50 z-10">
                    <div className="flex flex-col items-center gap-2">
                        <div className="w-8 h-8 border-2 border-orange-500 border-t-transparent rounded-full animate-spin" />
                        <p className="text-sm text-zinc-400">Loading chart...</p>
                    </div>
                </div>
            )}
            {error && (
                <div className="absolute inset-0 flex items-center justify-center z-10">
                    <div className="bg-red-500/10 border border-red-500/20 rounded-lg px-4 py-2">
                        <p className="text-sm text-red-400">{error}</p>
                    </div>
                </div>
            )}
            <div ref={chartContainerRef} className="w-full h-full" />
        </div>
    );
}
