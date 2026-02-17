import { StockScore, StockFinancial, RecommendationData } from '@/lib/types';

interface AnalysisSummaryProps {
  score: StockScore | null;
  financial: StockFinancial | null;
  recommendation: RecommendationData;
}

export default function AnalysisSummary({ score, financial, recommendation }: AnalysisSummaryProps) {
  const getScoreColor = (scoreValue: number) => {
    if (scoreValue >= 70) return 'text-green-400';
    if (scoreValue >= 50) return 'text-yellow-400';
    if (scoreValue >= 30) return 'text-orange-400';
    return 'text-red-400';
  };

  const getScoreBgColor = (scoreValue: number) => {
    if (scoreValue >= 70) return 'bg-green-500';
    if (scoreValue >= 50) return 'bg-yellow-500';
    if (scoreValue >= 30) return 'bg-orange-500';
    return 'bg-red-500';
  };

  const getGradeColor = (grade: string) => {
    if (!grade) return 'text-white/40 border-white/10 bg-white/5';
    const g = grade.toUpperCase();
    if (g === 'A' || g === 'A+') return 'text-green-400 border-green-400/50 bg-green-400/20';
    if (g === 'B' || g === 'B+') return 'text-yellow-400 border-yellow-400/50 bg-yellow-400/20';
    if (g === 'C' || g === 'C+') return 'text-orange-400 border-orange-400/50 bg-orange-400/20';
    if (g === 'D' || g === 'F') return 'text-red-400 border-red-400/50 bg-red-400/20';
    if (g === 'CHEAP' || g.includes('CHEAP')) return 'text-green-400 border-green-400/50 bg-green-400/20';
    if (g === 'FAIR' || g.includes('FAIR')) return 'text-yellow-400 border-yellow-400/50 bg-yellow-400/20';
    if (g === 'EXPENSIVE' || g.includes('EXPENSIVE')) return 'text-red-400 border-red-400/50 bg-red-400/20';
    return 'text-white/40 border-white/10 bg-white/5';
  };

  const formatNumber = (value: number | string | null | undefined) => {
    if (value === null || value === undefined) return 'N/A';
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(numValue)) return 'N/A';
    return numValue.toFixed(2);
  };

  const formatPercent = (value: number | string | null | undefined) => {
    if (value === null || value === undefined) return 'N/A';
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(numValue)) return 'N/A';
    const sign = numValue > 0 ? '+' : '';
    return `${sign}${numValue.toFixed(2)}%`;
  };

  const hasData = (category: 'valuation' | 'health' | 'growth' | 'risk') => {
    if (!score) return false;
    switch (category) {
      case 'valuation':
        return score.valuationScore != null && score.valuationScore > 0;
      case 'health':
        return score.healthScore != null && score.healthScore > 0;
      case 'growth':
        return score.growthScore != null && score.growthScore > 0;
      case 'risk':
        return score.riskScore != null && score.riskScore > 0;
      default:
        return false;
    }
  };

  return (
    <div className="glass-strong rounded-2xl p-8 border border-white/10">
      <div className="mb-8">
        <h2 className="text-3xl font-bold text-white mb-2">Analysis Summary</h2>
        <p className="text-white/60">Key metrics and evaluation across valuation, financial health, growth, and risk</p>
      </div>
      
      <div className="grid md:grid-cols-2 gap-6">
        {/* Valuation Card */}
        <div className="bg-white/5 rounded-xl p-6 border border-white/10 hover:border-white/20 transition-all">
          <div className="flex items-start justify-between mb-4">
            <div className="flex-1">
              <h3 className="text-xl font-bold text-white mb-1">Valuation</h3>
              {hasData('valuation') ? (
                <p className="text-white/70 text-sm leading-relaxed">{recommendation.reasons.valuation}</p>
              ) : (
                <p className="text-white/50 text-sm italic">Score calculation pending</p>
              )}
            </div>
            {hasData('valuation') && score && (
              <div className="flex flex-col items-end gap-2 ml-4">
                <div className="flex items-baseline gap-2">
                  <span className={`text-3xl font-bold ${getScoreColor(Number(score.valuationScore))}`}>
                    {formatNumber(score.valuationScore)}
                  </span>
                  <span className="text-sm text-white/40">/100</span>
                </div>
                {score.valuationCategory && (
                  <span className={`px-3 py-1 rounded-lg text-xs font-bold border ${getGradeColor(score.valuationCategory)}`}>
                    {score.valuationCategory}
                  </span>
                )}
              </div>
            )}
          </div>
          {hasData('valuation') && score && (
            <div className="h-3 bg-white/10 rounded-full overflow-hidden mb-5">
              <div
                className={`h-full transition-all ${getScoreBgColor(Number(score.valuationScore))}`}
                style={{ width: `${Number(score.valuationScore)}%` }}
              ></div>
            </div>
          )}
          {financial && (
            <div className="space-y-3 pt-4 border-t border-white/10">
              <div className="flex justify-between items-center">
                <span className="text-white/60 font-medium">P/E Ratio</span>
                <span className="text-white font-bold text-lg">{formatNumber(financial.peRatio)}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-white/60 font-medium">PEG Ratio</span>
                <span className="text-white font-bold text-lg">{formatNumber(financial.pegRatio)}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-white/60 font-medium">Price-to-Book</span>
                <span className="text-white font-bold text-lg">{formatNumber(financial.priceToBook)}</span>
              </div>
            </div>
          )}
        </div>

        {/* Financial Strength Card */}
        <div className="bg-white/5 rounded-xl p-6 border border-white/10 hover:border-white/20 transition-all">
          <div className="flex items-start justify-between mb-4">
            <div className="flex-1">
              <h3 className="text-xl font-bold text-white mb-1">Financial Strength</h3>
              {hasData('health') ? (
                <p className="text-white/70 text-sm leading-relaxed">{recommendation.reasons.financialStrength}</p>
              ) : (
                <p className="text-white/50 text-sm italic">Score calculation pending</p>
              )}
            </div>
            {hasData('health') && score && (
              <div className="flex flex-col items-end gap-2 ml-4">
                <div className="flex items-baseline gap-2">
                  <span className={`text-3xl font-bold ${getScoreColor(Number(score.healthScore))}`}>
                    {formatNumber(score.healthScore)}
                  </span>
                  <span className="text-sm text-white/40">/100</span>
                </div>
                {score.healthGrade && (
                  <span className={`px-3 py-1 rounded-lg text-xs font-bold border ${getGradeColor(score.healthGrade)}`}>
                    {score.healthGrade}
                  </span>
                )}
              </div>
            )}
          </div>
          {hasData('health') && score && (
            <div className="h-3 bg-white/10 rounded-full overflow-hidden mb-5">
              <div
                className={`h-full transition-all ${getScoreBgColor(Number(score.healthScore))}`}
                style={{ width: `${Number(score.healthScore)}%` }}
              ></div>
            </div>
          )}
          {financial && (
            <div className="space-y-3 pt-4 border-t border-white/10">
              <div className="flex justify-between items-center">
                <span className="text-white/60 font-medium">Debt-to-Equity</span>
                <span className="text-white font-bold text-lg">{formatNumber(financial.debtToEquity)}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-white/60 font-medium">Current Ratio</span>
                <span className="text-white font-bold text-lg">{formatNumber(financial.currentRatio)}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-white/60 font-medium">ROE</span>
                <span className="text-white font-bold text-lg">{formatPercent(financial.roe)}</span>
              </div>
            </div>
          )}
        </div>

        {/* Growth Outlook Card */}
        <div className="bg-white/5 rounded-xl p-6 border border-white/10 hover:border-white/20 transition-all">
          <div className="flex items-start justify-between mb-4">
            <div className="flex-1">
              <h3 className="text-xl font-bold text-white mb-1">Growth Outlook</h3>
              {hasData('growth') ? (
                <p className="text-white/70 text-sm leading-relaxed">{recommendation.reasons.growthOutlook}</p>
              ) : (
                <p className="text-white/50 text-sm italic">Score calculation pending</p>
              )}
            </div>
            {hasData('growth') && score && (
              <div className="flex flex-col items-end gap-2 ml-4">
                <div className="flex items-baseline gap-2">
                  <span className={`text-3xl font-bold ${getScoreColor(Number(score.growthScore))}`}>
                    {formatNumber(score.growthScore)}
                  </span>
                  <span className="text-sm text-white/40">/100</span>
                </div>
                {score.growthGrade && (
                  <span className={`px-3 py-1 rounded-lg text-xs font-bold border ${getGradeColor(score.growthGrade)}`}>
                    {score.growthGrade}
                  </span>
                )}
              </div>
            )}
          </div>
          {hasData('growth') && score && (
            <div className="h-3 bg-white/10 rounded-full overflow-hidden mb-5">
              <div
                className={`h-full transition-all ${getScoreBgColor(Number(score.growthScore))}`}
                style={{ width: `${Number(score.growthScore)}%` }}
              ></div>
            </div>
          )}
          {financial && (
            <div className="space-y-3 pt-4 border-t border-white/10">
              <div className="flex justify-between items-center">
                <span className="text-white/60 font-medium">Revenue Growth</span>
                <span className="text-white font-bold text-lg">{formatPercent(financial.revenueGrowthYoy)}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-white/60 font-medium">EPS Growth</span>
                <span className="text-white font-bold text-lg">{formatPercent(financial.epsGrowthYoy)}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-white/60 font-medium">Profit Margin</span>
                <span className="text-white font-bold text-lg">{formatPercent(financial.profitMargin)}</span>
              </div>
            </div>
          )}
        </div>

        {/* Risk Level Card */}
        <div className="bg-white/5 rounded-xl p-6 border border-white/10 hover:border-white/20 transition-all">
          <div className="flex items-start justify-between mb-4">
            <div className="flex-1">
              <h3 className="text-xl font-bold text-white mb-1">Risk Level</h3>
              {hasData('risk') ? (
                <p className="text-white/70 text-sm leading-relaxed">{recommendation.reasons.riskLevel}</p>
              ) : (
                <p className="text-white/50 text-sm italic">Score calculation pending</p>
              )}
            </div>
            {hasData('risk') && score && (
              <div className="flex flex-col items-end gap-2 ml-4">
                <div className="flex items-baseline gap-2">
                  <span className={`text-3xl font-bold ${getScoreColor(Number(score.riskScore))}`}>
                    {formatNumber(score.riskScore)}
                  </span>
                  <span className="text-sm text-white/40">/100</span>
                </div>
                {score.riskGrade && (
                  <span className={`px-3 py-1 rounded-lg text-xs font-bold border ${getGradeColor(score.riskGrade)}`}>
                    {score.riskGrade}
                  </span>
                )}
              </div>
            )}
          </div>
          {hasData('risk') && score && (
            <div className="h-3 bg-white/10 rounded-full overflow-hidden mb-5">
              <div
                className={`h-full transition-all ${getScoreBgColor(Number(score.riskScore))}`}
                style={{ width: `${Number(score.riskScore)}%` }}
              ></div>
            </div>
          )}
          {score && (
            <div className="space-y-3 pt-4 border-t border-white/10">
              <div className="flex justify-between items-center">
                <span className="text-white/60 font-medium">Debt Score</span>
                <span className="text-white font-bold text-lg">
                  {score.debtScore != null ? formatNumber(score.debtScore) : 'N/A'}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-white/60 font-medium">Volatility Score</span>
                <span className="text-white font-bold text-lg">
                  {score.volatilityScore != null ? formatNumber(score.volatilityScore) : 'N/A'}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-white/60 font-medium">Profitability Score</span>
                <span className="text-white font-bold text-lg">
                  {score.profitabilityScore != null ? formatNumber(score.profitabilityScore) : 'N/A'}
                </span>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
