import { StockFinancial, StockScore } from '@/lib/types';

interface FinancialSnapshotProps {
  financial: StockFinancial | null;
  score: StockScore | null;
}

export default function FinancialSnapshot({ financial, score }: FinancialSnapshotProps) {
  if (!financial && !score) {
    return (
      <div className="glass-strong rounded-2xl p-8">
        <h2 className="text-2xl font-semibold mb-6">Financial Snapshot</h2>
        <p className="text-white/60">Financial data not available</p>
      </div>
    );
  }

  const formatCurrency = (value: number | string | null) => {
    if (value === null || value === undefined) return 'N/A';
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(numValue)) return 'N/A';
    if (numValue >= 1e12) return `$${(numValue / 1e12).toFixed(2)}T`;
    if (numValue >= 1e9) return `$${(numValue / 1e9).toFixed(2)}B`;
    if (numValue >= 1e6) return `$${(numValue / 1e6).toFixed(2)}M`;
    return `$${numValue.toLocaleString()}`;
  };

  const formatPercent = (value: number | string | null) => {
    if (value === null || value === undefined) return 'N/A';
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(numValue)) return 'N/A';
    return `${numValue.toFixed(2)}%`;
  };

  const formatNumber = (value: number | string | null | undefined) => {
    if (value === null || value === undefined) return 'N/A';
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(numValue)) return 'N/A';
    return numValue.toFixed(2);
  };

  return (
    <div className="glass-strong rounded-2xl p-8">
      <h2 className="text-2xl font-semibold mb-6">Financial Snapshot</h2>
      
      <div className="grid md:grid-cols-2 gap-6">
        {/* Valuation Metrics */}
        <div className="space-y-4">
          <h3 className="text-lg font-medium text-white/80 mb-4">Valuation Metrics</h3>
          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="text-white/60">P/E Ratio</span>
              <span className="font-semibold">{formatNumber(financial?.peRatio)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-white/60">PEG Ratio</span>
              <span className="font-semibold">{formatNumber(financial?.pegRatio)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-white/60">Price-to-Book</span>
              <span className="font-semibold">{formatNumber(financial?.priceToBook)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-white/60">Price-to-Sales</span>
              <span className="font-semibold">{formatNumber(financial?.priceToSales)}</span>
            </div>
            {score && (
              <div className="flex justify-between pt-3 border-t border-white/10">
                <span className="text-white/60">Valuation Score</span>
                <span className="font-semibold">{formatNumber(score.valuationScore)} / 100</span>
              </div>
            )}
          </div>
        </div>

        {/* Profitability */}
        <div className="space-y-4">
          <h3 className="text-lg font-medium text-white/80 mb-4">Profitability</h3>
          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="text-white/60">EPS (TTM)</span>
              <span className="font-semibold">${formatNumber(financial?.epsTtm)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-white/60">ROE</span>
              <span className="font-semibold">{formatPercent(financial?.roe)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-white/60">ROIC</span>
              <span className="font-semibold">{formatPercent(financial?.roic)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-white/60">Profit Margin</span>
              <span className="font-semibold">{formatPercent(financial?.profitMargin)}</span>
            </div>
            {score && (
              <div className="flex justify-between pt-3 border-t border-white/10">
                <span className="text-white/60">Profitability Score</span>
                <span className="font-semibold">
                  {formatNumber(score.profitabilityScore)} / 100
                </span>
              </div>
            )}
          </div>
        </div>

        {/* Financial Health */}
        <div className="space-y-4">
          <h3 className="text-lg font-medium text-white/80 mb-4">Financial Health</h3>
          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="text-white/60">Total Cash</span>
              <span className="font-semibold">{formatCurrency(financial?.totalCash)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-white/60">Total Debt</span>
              <span className="font-semibold">{formatCurrency(financial?.totalDebt)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-white/60">Debt-to-Equity</span>
              <span className="font-semibold">{formatNumber(financial?.debtToEquity)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-white/60">Current Ratio</span>
              <span className="font-semibold">{formatNumber(financial?.currentRatio)}</span>
            </div>
            {score && (
              <div className="flex justify-between pt-3 border-t border-white/10">
                <span className="text-white/60">Health Score</span>
                <span className="font-semibold">{formatNumber(score.healthScore)} / 100</span>
              </div>
            )}
          </div>
        </div>

        {/* Revenue & Income */}
        <div className="space-y-4">
          <h3 className="text-lg font-medium text-white/80 mb-4">Revenue & Income</h3>
          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="text-white/60">Revenue</span>
              <span className="font-semibold">{formatCurrency(financial?.revenue)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-white/60">Net Income</span>
              <span className="font-semibold">{formatCurrency(financial?.netIncome)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-white/60">Revenue Growth (YoY)</span>
              <span className="font-semibold">{formatPercent(financial?.revenueGrowthYoy)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-white/60">EPS Growth (YoY)</span>
              <span className="font-semibold">{formatPercent(financial?.epsGrowthYoy)}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

