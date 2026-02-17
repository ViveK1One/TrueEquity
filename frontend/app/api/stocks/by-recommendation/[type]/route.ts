import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { calculateRecommendation } from '@/lib/recommendation';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ type: string }> }
) {
  try {
    const { type: recommendationType } = await params;
    const upperType = recommendationType.toUpperCase();
    
    // Validate recommendation type
    const validTypes = ['BUY', 'HOLD', 'SELL', 'AVOID'];
    if (!validTypes.includes(upperType)) {
      return NextResponse.json(
        { error: 'Invalid recommendation type. Use: BUY, HOLD, SELL, or AVOID' },
        { status: 400 }
      );
    }

    // Get all active stocks with their latest scores
    const stocksResult = await pool.query(
      `SELECT DISTINCT ON (s.symbol)
         s.symbol,
         s.name,
         s.sector,
         s.exchange,
         sp.close as current_price,
         sp.date as price_date,
         ss.overall_score,
         ss.valuation_score,
         ss.health_score,
         ss.growth_score,
         ss.risk_score,
         ss.overall_grade,
         ss.health_grade,
         ss.growth_grade,
         ss.risk_grade,
         ss.valuation_category,
         sf.pe_ratio,
         sf.peg_ratio,
         sf.revenue_growth_yoy,
         sf.eps_growth_yoy
       FROM stocks s
       LEFT JOIN stock_prices sp ON s.symbol = sp.symbol 
         AND sp.date = (SELECT MAX(date) FROM stock_prices WHERE symbol = s.symbol)
       LEFT JOIN stock_scores ss ON s.symbol = ss.symbol 
         AND ss.calculated_at = (SELECT MAX(calculated_at) FROM stock_scores WHERE symbol = s.symbol)
       LEFT JOIN stock_financials sf ON s.symbol = sf.symbol 
         AND sf.period_end_date = (SELECT MAX(period_end_date) FROM stock_financials WHERE symbol = s.symbol)
       WHERE s.is_active = true
       ORDER BY s.symbol, ss.calculated_at DESC NULLS LAST
       LIMIT 500`
    );

    // Calculate recommendation for each stock and filter by type
    const stocksWithRecommendations = [];
    
    for (const row of stocksResult.rows) {
      // Build score object
      const score = row.overall_score != null ? {
        overallScore: parseFloat(row.overall_score) || 0,
        valuationScore: parseFloat(row.valuation_score) || null,
        healthScore: parseFloat(row.health_score) || null,
        growthScore: parseFloat(row.growth_score) || null,
        riskScore: parseFloat(row.risk_score) || null,
        overallGrade: row.overall_grade,
        healthGrade: row.health_grade,
        growthGrade: row.growth_grade,
        riskGrade: row.risk_grade,
        valuationCategory: row.valuation_category,
      } : null;

      // Build financial object
      const financial = row.pe_ratio != null ? {
        peRatio: parseFloat(row.pe_ratio) || null,
        pegRatio: parseFloat(row.peg_ratio) || null,
        revenueGrowthYoy: parseFloat(row.revenue_growth_yoy) || null,
        epsGrowthYoy: parseFloat(row.eps_growth_yoy) || null,
      } : null;

      // Calculate recommendation
      const recommendation = calculateRecommendation(score, financial);
      
      // Filter by recommendation type
      if (recommendation.recommendation === upperType) {
        stocksWithRecommendations.push({
          symbol: row.symbol,
          name: row.name,
          sector: row.sector,
          exchange: row.exchange,
          currentPrice: row.current_price ? parseFloat(row.current_price) : null,
          priceDate: row.price_date,
          overallScore: score?.overallScore || 0,
          overallGrade: score?.overallGrade || null,
          recommendation: recommendation.recommendation,
          confidence: recommendation.confidence,
          peRatio: financial?.peRatio || null,
          revenueGrowth: financial?.revenueGrowthYoy || null,
        });
      }
    }

    // Sort by overall score (descending) for BUY, ascending for SELL/AVOID
    stocksWithRecommendations.sort((a, b) => {
      if (upperType === 'BUY' || upperType === 'HOLD') {
        return b.overallScore - a.overallScore;
      } else {
        return a.overallScore - b.overallScore;
      }
    });

    return NextResponse.json({
      stocks: stocksWithRecommendations,
      count: stocksWithRecommendations.length,
      type: upperType,
    });
  } catch (error) {
    console.error('Error fetching stocks by recommendation:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
