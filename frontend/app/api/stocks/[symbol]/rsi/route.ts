import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ symbol: string }> }
) {
  try {
    const { symbol: symbolParam } = await params;
    const symbol = symbolParam.toUpperCase();

    const { searchParams } = new URL(request.url);
    const timeframe = searchParams.get('timeframe') || '1d';

    const validTimeframes = ['1h', '30m', '2h', '1d'];
    if (!validTimeframes.includes(timeframe)) {
      return NextResponse.json(
        { error: 'Invalid timeframe. Use: 1h, 30m, 2h, or 1d' },
        { status: 400 }
      );
    }

    // 1) Read from DB first (works when Java backend is stopped)
    const dbResult = await pool.query(
      `SELECT rsi FROM technical_indicators 
       WHERE symbol = $1 AND timeframe = $2 AND rsi IS NOT NULL 
       ORDER BY date DESC 
       LIMIT 1`,
      [symbol, timeframe]
    );
    if (dbResult.rows.length > 0 && dbResult.rows[0].rsi != null) {
      const rsi = parseFloat(dbResult.rows[0].rsi);
      return NextResponse.json({ rsi: Number.isFinite(rsi) ? rsi : null, timeframe, error: null });
    }

    // 2) Fallback: call Java backend when running (to compute and store for next time)
    const javaBackendUrl = process.env.JAVA_BACKEND_URL || 'http://localhost:8080';
    try {
      const response = await fetch(
        `${javaBackendUrl}/api/rsi/${symbol}?timeframe=${timeframe}`,
        {
          method: 'GET',
          headers: { 'Content-Type': 'application/json' },
          signal: AbortSignal.timeout(10000),
        }
      );
      if (response.ok) {
        const data = await response.json();
        if (data.rsi != null) {
          return NextResponse.json({ rsi: data.rsi, timeframe, error: data.error || null });
        }
      }
    } catch (err) {
      console.error('Java backend unavailable for RSI:', err);
    }

    return NextResponse.json({
      rsi: null,
      timeframe,
      error: 'No RSI in database. Run the Java ingestion app to populate RSI for all timeframes.',
    });
  } catch (error) {
    console.error('Error fetching RSI:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
