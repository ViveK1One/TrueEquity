import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';

export async function GET(request: NextRequest) {
  try {
    const searchParams = request.nextUrl.searchParams;
    const query = searchParams.get('q')?.toUpperCase() || '';

    if (!query || query.length < 1) {
      return NextResponse.json({ stocks: [] });
    }

    // Search stocks by symbol or name
    const result = await pool.query(
      `SELECT symbol, name, exchange, sector, industry, market_cap 
       FROM stocks 
       WHERE (symbol LIKE $1 OR name ILIKE $1) 
       AND is_active = true 
       ORDER BY 
         CASE WHEN symbol = $2 THEN 1 ELSE 2 END,
         symbol
       LIMIT 10`,
      [`%${query}%`, query]
    );

    return NextResponse.json({ stocks: result.rows });
  } catch (error) {
    console.error('Error searching stocks:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}

