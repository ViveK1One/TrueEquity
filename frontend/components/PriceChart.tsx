'use client';

import { StockPrice } from '@/lib/types';
import { useEffect, useRef } from 'react';

interface PriceChartProps {
  prices: StockPrice[];
  symbol: string;
}

export default function PriceChart({ prices, symbol }: PriceChartProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    if (!canvasRef.current || prices.length === 0) return;

    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const width = canvas.width;
    const height = canvas.height;
    const padding = 40;

    // Clear canvas
    ctx.clearRect(0, 0, width, height);

    // Background
    ctx.fillStyle = 'rgba(255, 255, 255, 0.02)';
    ctx.fillRect(0, 0, width, height);

    // Find min/max prices
    const values = prices.map((p) => p.close);
    const minPrice = Math.min(...values);
    const maxPrice = Math.max(...values);
    const priceRange = maxPrice - minPrice || 1;

    // Draw grid
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.05)';
    ctx.lineWidth = 1;
    for (let i = 0; i <= 5; i++) {
      const y = padding + (height - 2 * padding) * (i / 5);
      ctx.beginPath();
      ctx.moveTo(padding, y);
      ctx.lineTo(width - padding, y);
      ctx.stroke();
    }

    // Draw price line
    ctx.strokeStyle = '#10b981';
    ctx.lineWidth = 2;
    ctx.beginPath();

    prices.forEach((price, index) => {
      const x = padding + ((width - 2 * padding) * index) / (prices.length - 1);
      const y = height - padding - ((price.close - minPrice) / priceRange) * (height - 2 * padding);

      if (index === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    });

    ctx.stroke();

    // Draw area under curve
    const gradient = ctx.createLinearGradient(0, padding, 0, height - padding);
    gradient.addColorStop(0, 'rgba(16, 185, 129, 0.3)');
    gradient.addColorStop(1, 'rgba(16, 185, 129, 0)');

    ctx.fillStyle = gradient;
    ctx.beginPath();
    ctx.moveTo(padding, height - padding);
    prices.forEach((price, index) => {
      const x = padding + ((width - 2 * padding) * index) / (prices.length - 1);
      const y = height - padding - ((price.close - minPrice) / priceRange) * (height - 2 * padding);
      ctx.lineTo(x, y);
    });
    ctx.lineTo(width - padding, height - padding);
    ctx.closePath();
    ctx.fill();

    // Draw labels
    ctx.fillStyle = 'rgba(255, 255, 255, 0.6)';
    ctx.font = '12px sans-serif';
    ctx.textAlign = 'right';

    // Y-axis labels
    for (let i = 0; i <= 5; i++) {
      const value = minPrice + (priceRange * i) / 5;
      const y = padding + (height - 2 * padding) * (1 - i / 5);
      ctx.fillText(`$${value.toFixed(2)}`, padding - 10, y + 4);
    }

    // X-axis labels (first, middle, last)
    ctx.textAlign = 'center';
    const dates = [0, Math.floor(prices.length / 2), prices.length - 1];
    dates.forEach((index) => {
      const x = padding + ((width - 2 * padding) * index) / (prices.length - 1);
      const date = new Date(prices[index].date);
      ctx.fillText(date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }), x, height - padding + 20);
    });
  }, [prices]);

  if (prices.length === 0) {
    return (
      <div>
        <h2 className="text-2xl font-semibold text-white mb-6">Price Chart</h2>
        <p className="text-gray-400">No price data available</p>
      </div>
    );
  }

  const latestPrice = prices[prices.length - 1];
  const firstPrice = prices[0];
  const change = latestPrice.close - firstPrice.close;
  const changePercent = ((change / firstPrice.close) * 100).toFixed(2);

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <div className="text-sm text-gray-400">30-Day Change</div>
        <div className={`text-lg font-bold ${change >= 0 ? 'text-green-400' : 'text-red-400'}`}>
          {change >= 0 ? '+' : ''}${change.toFixed(2)} ({changePercent}%)
        </div>
      </div>
      <div className="relative bg-gray-900 rounded-lg p-4">
        <canvas
          ref={canvasRef}
          width={800}
          height={300}
          className="w-full h-auto rounded-lg"
        />
      </div>
    </div>
  );
}

