import { NextRequest, NextResponse } from 'next/server';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ symbol: string }> }
) {
  try {
    const { symbol: symbolParam } = await params;
    const symbol = symbolParam.toUpperCase();
    
    // Get timeframe from query parameter (default to '1d' for daily)
    const { searchParams } = new URL(request.url);
    const timeframe = searchParams.get('timeframe') || '1d';
    
    // Valid timeframes: 1h, 30m, 2h, 1d
    const validTimeframes = ['1h', '30m', '2h', '1d'];
    if (!validTimeframes.includes(timeframe)) {
      return NextResponse.json(
        { error: 'Invalid timeframe. Use: 1h, 30m, 2h, or 1d' },
        { status: 400 }
      );
    }
    
    // Call Java backend to calculate RSI for the timeframe
    // Note: This requires the Java backend to be running and accessible
    const javaBackendUrl = process.env.JAVA_BACKEND_URL || 'http://localhost:8080';
    
    try {
      const response = await fetch(
        `${javaBackendUrl}/api/rsi/${symbol}?timeframe=${timeframe}`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
          // Add timeout
          signal: AbortSignal.timeout(10000), // 10 second timeout
        }
      );
      
      if (!response.ok) {
        // Even if not OK, try to get error message
        try {
          const errorData = await response.json();
          return NextResponse.json({ rsi: null, timeframe, error: errorData.error || 'Failed to calculate RSI' });
        } catch {
          return NextResponse.json({ rsi: null, timeframe, error: `Backend returned ${response.status}` });
        }
      }
      
      const data = await response.json();
      // Backend now returns 200 even if RSI is null, so check the rsi field
      return NextResponse.json({ rsi: data.rsi, timeframe, error: data.error || null });
      
    } catch (error) {
      console.error('Error calling Java backend for RSI:', error);
      // Fallback: return null if Java backend is not available
      return NextResponse.json({ rsi: null, timeframe, error: 'Backend service unavailable' });
    }
    
  } catch (error) {
    console.error('Error fetching RSI:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
