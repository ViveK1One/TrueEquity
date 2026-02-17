import { StockFinancial, StockScore } from '@/lib/types';

interface GrowthMetricsProps {
  financial: StockFinancial | null;
  score: StockScore | null;
}

export default function GrowthMetrics({ financial, score }: GrowthMetricsProps) {
  const formatPercent = (value: number | string | null | undefined) => {
    if (value === null || value === undefined) return 'N/A';
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(numValue)) return 'N/A';
    return `${numValue > 0 ? '+' : ''}${numValue.toFixed(2)}%`;
  };

  const formatNumber = (value: number | string | null | undefined) => {
    if (value === null || value === undefined) return 'N/A';
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(numValue)) return 'N/A';
    return numValue.toFixed(1);
  };

  return (
    <div className="glass-strong rounded-2xl p-6">
      <h3 className="text-lg font-semibold mb-4">Growth Metrics</h3>
      <div className="space-y-4">
        <div>
          <div className="flex justify-between mb-1">
            <span className="text-white/60 text-sm">Revenue Growth (YoY)</span>
            <span className="font-semibold">{formatPercent(financial?.revenueGrowthYoy)}</span>
          </div>
          {financial?.revenueGrowthYoy != null && (
            <div className="h-2 bg-white/5 rounded-full overflow-hidden">
              <div
                className={`h-full ${
                  Number(financial.revenueGrowthYoy) > 20
                    ? 'bg-green-500'
                    : Number(financial.revenueGrowthYoy) > 10
                    ? 'bg-yellow-500'
                    : Number(financial.revenueGrowthYoy) > 0
                    ? 'bg-orange-500'
                    : 'bg-red-500'
                }`}
                style={{
                  width: `${Math.min(Math.abs(Number(financial.revenueGrowthYoy)), 50)}%`,
                }}
              ></div>
            </div>
          )}
        </div>

        <div>
          <div className="flex justify-between mb-1">
            <span className="text-white/60 text-sm">EPS Growth (YoY)</span>
            <span className="font-semibold">{formatPercent(financial?.epsGrowthYoy)}</span>
          </div>
          {financial?.epsGrowthYoy != null && (
            <div className="h-2 bg-white/5 rounded-full overflow-hidden">
              <div
                className={`h-full ${
                  Number(financial.epsGrowthYoy) > 20
                    ? 'bg-green-500'
                    : Number(financial.epsGrowthYoy) > 10
                    ? 'bg-yellow-500'
                    : Number(financial.epsGrowthYoy) > 0
                    ? 'bg-orange-500'
                    : 'bg-red-500'
                }`}
                style={{
                  width: `${Math.min(Math.abs(Number(financial.epsGrowthYoy)), 50)}%`,
                }}
              ></div>
            </div>
          )}
        </div>

        {score && score.growthScore != null && (
          <div className="pt-4 border-t border-white/10">
            <div className="flex justify-between mb-2">
              <span className="text-white/60">Growth Score</span>
              <span className="font-semibold">{formatNumber(score.growthScore)} / 100</span>
            </div>
            <div className="h-2 bg-white/5 rounded-full overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-blue-500 to-green-500"
                style={{ width: `${Number(score.growthScore)}%` }}
              ></div>
            </div>
            <div className="text-xs text-white/40 mt-1">Grade: {score.growthGrade || 'N/A'}</div>
          </div>
        )}
      </div>
    </div>
  );
}

