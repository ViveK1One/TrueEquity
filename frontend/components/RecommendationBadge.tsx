import { RecommendationData } from '@/lib/types';

interface RecommendationBadgeProps {
  recommendation: RecommendationData;
}

export default function RecommendationBadge({ recommendation }: RecommendationBadgeProps) {
  const { recommendation: rec, confidence, overallScore } = recommendation;

  const badgeClasses = {
    BUY: 'badge-buy',
    HOLD: 'badge-hold',
    SELL: 'badge-sell',
    AVOID: 'badge-avoid',
  };

  const confidenceColors = {
    high: 'text-green-400',
    medium: 'text-yellow-400',
    low: 'text-orange-400',
  };

  return (
    <div className="glass-strong rounded-2xl p-6 border border-white/10">
      <div className="flex items-center gap-6">
        <div className={`${badgeClasses[rec]} px-6 py-3 rounded-xl font-bold text-lg text-white`}>
          {rec}
        </div>
        <div className="flex-1">
          <div className="flex items-center gap-4 mb-2">
            <span className="text-white/60">Overall Score:</span>
            <span className="text-2xl font-bold">{overallScore.toFixed(1)}</span>
            <span className="text-sm text-white/40">/ 100</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-white/60">Confidence:</span>
            <span className={`font-semibold ${confidenceColors[confidence]}`}>
              {confidence.toUpperCase()}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}

