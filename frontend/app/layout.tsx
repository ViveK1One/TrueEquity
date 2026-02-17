import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "TrueEquity - Honest Stock Insights. No Hype. No Predictions.",
  description: "Data-driven stock analysis platform. Get transparent, comprehensive stock insights with BUY/HOLD/SELL/AVOID recommendations based on real financial data.",
  keywords: "stock analysis, investment, financial data, stock recommendations, valuation analysis",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
