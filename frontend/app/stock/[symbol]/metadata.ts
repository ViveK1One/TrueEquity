import { Metadata } from 'next';

export async function generateMetadata({
  params,
}: {
  params: Promise<{ symbol: string }>;
}): Promise<Metadata> {
  const { symbol: symbolParam } = await params;
  const symbol = symbolParam.toUpperCase();

  return {
    title: `${symbol} Stock Analysis | TrueEquity`,
    description: `Comprehensive stock analysis for ${symbol} including valuation, financial health, growth outlook, and risk assessment. Get data-driven investment insights.`,
    keywords: `${symbol}, stock analysis, investment, financial data, valuation, ${symbol} stock`,
  };
}

