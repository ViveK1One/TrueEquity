import { StockScore, TechnicalIndicator } from '@/lib/types';

interface RiskAnalysisProps {
  score: StockScore | null;
  technicalIndicator: TechnicalIndicator | null;
}

export default function RiskAnalysis({ score, technicalIndicator }: RiskAnalysisProps) {
  return (
    <div className="glass-strong rounded-2xl p-6">
      <h3 className="text-lg font-semibold mb-4">Risk Analysis</h3>
      <div className="space-y-4">
        {score && score.riskScore != null && (
          <>
            <div>
              <div className="flex justify-between mb-2">
                <span className="text-white/60 text-sm">Risk Score</span>
                <span className="font-semibold">
                  {typeof score.riskScore === 'string' ? parseFloat(score.riskScore).toFixed(1) : score.riskScore.toFixed(1)} / 100
                </span>
              </div>
              <div className="h-2 bg-white/5 rounded-full overflow-hidden">
                <div
                  className={`h-full ${
                    Number(score.riskScore) >= 70
                      ? 'bg-green-500'
                      : Number(score.riskScore) >= 50
                      ? 'bg-yellow-500'
                      : 'bg-red-500'
                  }`}
                  style={{ width: `${Number(score.riskScore)}%` }}
                ></div>
              </div>
              <div className="text-xs text-white/40 mt-1">Grade: {score.riskGrade || 'N/A'}</div>
            </div>

            <div className="pt-3 border-t border-white/10 space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-white/60">Debt Score</span>
                <span>{score.debtScore?.toFixed(1) || 'N/A'}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-white/60">Volatility Score</span>
                <span>{score.volatilityScore?.toFixed(1) || 'N/A'}</span>
              </div>
            </div>
          </>
        )}

        {technicalIndicator?.rsi && (
          <div className="pt-3 border-t border-white/10">
            <div className="flex justify-between mb-2">
              <span className="text-white/60 text-sm">RSI Signal</span>
              <span
                className={`font-semibold ${
                  technicalIndicator.rsi < 30
                    ? 'text-green-400'
                    : technicalIndicator.rsi > 70
                    ? 'text-red-400'
                    : 'text-yellow-400'
                }`}
              >
                {technicalIndicator.rsi < 30
                  ? 'Oversold'
                  : technicalIndicator.rsi > 70
                  ? 'Overbought'
                  : 'Neutral'}
              </span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

