import { StockScore, StockFinancial, Recommendation, RecommendationData } from './types';

/**
 * Calculate recommendation based on stock scores and financial data
 * BUY: overallScore >= 70
 * HOLD: overallScore 50-69
 * SELL: overallScore 30-49
 * AVOID: overallScore < 30
 */
export function calculateRecommendation(
  score: StockScore | null,
  financial: StockFinancial | null
): RecommendationData {
  if (!score) {
    return {
      recommendation: 'AVOID',
      confidence: 'low',
      overallScore: 0,
      reasons: {
        valuation: 'Insufficient data to evaluate valuation',
        financialStrength: 'Insufficient data to evaluate financial strength',
        growthOutlook: 'Insufficient data to evaluate growth',
        riskLevel: 'Insufficient data to evaluate risk',
      },
    };
  }

  const overallScore = score.overallScore;
  let recommendation: Recommendation;
  let confidence: 'low' | 'medium' | 'high' = 'medium';

  if (overallScore >= 70) {
    recommendation = 'BUY';
    confidence = overallScore >= 85 ? 'high' : overallScore >= 75 ? 'medium' : 'low';
  } else if (overallScore >= 50) {
    recommendation = 'HOLD';
    confidence = overallScore >= 60 ? 'medium' : 'low';
  } else if (overallScore >= 30) {
    recommendation = 'SELL';
    confidence = overallScore >= 40 ? 'medium' : 'low';
  } else {
    recommendation = 'AVOID';
    confidence = 'high';
  }

  // Generate reasons based on scores
  const reasons = {
    valuation: getValuationReason(score, financial),
    financialStrength: getFinancialStrengthReason(score),
    growthOutlook: getGrowthReason(score),
    riskLevel: getRiskReason(score),
  };

  return {
    recommendation,
    confidence,
    overallScore,
    reasons,
  };
}

function getValuationReason(score: StockScore, financial: StockFinancial | null): string {
  const category = score.valuationCategory?.toLowerCase() || 'unknown';
  const peRatio = financial?.peRatio;
  
  if (category === 'cheap') {
    return `Attractive valuation with ${category} metrics. ${peRatio ? `P/E ratio of ${peRatio.toFixed(2)} is below market average.` : ''}`;
  } else if (category === 'fair') {
    return `Fair valuation. ${peRatio ? `P/E ratio of ${peRatio.toFixed(2)} is reasonable.` : ''}`;
  } else {
    return `Valuation appears ${category}. ${peRatio ? `P/E ratio of ${peRatio.toFixed(2)} is above market average.` : ''}`;
  }
}

function getFinancialStrengthReason(score: StockScore): string {
  const healthGrade = score.healthGrade;
  const healthScore = score.healthScore;
  
  if (healthScore >= 70) {
    return `Strong financial health (${healthGrade} grade). Healthy balance sheet with good liquidity.`;
  } else if (healthScore >= 50) {
    return `Moderate financial health (${healthGrade} grade). Some areas need attention.`;
  } else {
    return `Weak financial health (${healthGrade} grade). Significant concerns about financial stability.`;
  }
}

function getGrowthReason(score: StockScore): string {
  const growthGrade = score.growthGrade;
  const growthScore = score.growthScore;
  
  if (growthScore >= 70) {
    return `Strong growth outlook (${growthGrade} grade). Positive revenue and earnings trends.`;
  } else if (growthScore >= 50) {
    return `Moderate growth outlook (${growthGrade} grade). Growth may be slowing.`;
  } else {
    return `Weak growth outlook (${growthGrade} grade). Declining or stagnant growth.`;
  }
}

function getRiskReason(score: StockScore): string {
  const riskGrade = score.riskGrade;
  const riskScore = score.riskScore;
  
  if (riskScore >= 70) {
    return `Low risk profile (${riskGrade} grade). Stable and predictable business.`;
  } else if (riskScore >= 50) {
    return `Moderate risk (${riskGrade} grade). Some volatility expected.`;
  } else {
    return `High risk profile (${riskGrade} grade). Significant volatility and uncertainty.`;
  }
}

