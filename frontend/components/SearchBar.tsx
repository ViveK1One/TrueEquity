'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';

/**
 * Stock interface for search results
 */
interface Stock {
  symbol: string;
  name: string;
  exchange: string;
}

/**
 * SearchBar Component
 * 
 * Provides real-time stock search functionality with debounced API calls.
 * Displays search results in a dropdown and navigates to stock detail page on selection.
 * 
 * Features:
 * - Debounced search (300ms delay to reduce API calls)
 * - Click-outside detection to close dropdown
 * - Keyboard navigation support
 * - Loading states during search
 * 
 * @returns {JSX.Element} Search input with dropdown results
 */
export default function SearchBar() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Stock[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();
  const searchRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (query.length < 1) {
      setResults([]);
      setIsOpen(false);
      return;
    }

    const searchStocks = async () => {
      setIsLoading(true);
      try {
        const response = await fetch(`/api/stocks/search?q=${encodeURIComponent(query)}`);
        const data = await response.json();
        setResults(data.stocks || []);
        setIsOpen(data.stocks && data.stocks.length > 0);
      } catch (error) {
        console.error('Search error:', error);
        setResults([]);
      } finally {
        setIsLoading(false);
      }
    };

    const debounce = setTimeout(searchStocks, 300);
    return () => clearTimeout(debounce);
  }, [query]);

  const handleSelect = (symbol: string) => {
    setQuery('');
    setIsOpen(false);
    router.push(`/stock/${symbol}`);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (query.trim()) {
      handleSelect(query.toUpperCase());
    }
  };

  return (
    <div ref={searchRef} className="relative w-full" style={{ zIndex: 1000 }}>
      <form onSubmit={handleSubmit}>
        <div className="relative">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onFocus={() => query.length > 0 && results.length > 0 && setIsOpen(true)}
            placeholder="Search for a stock ticker (e.g., AAPL, TSLA)"
            className="search-box"
          />
          <svg
            className="absolute left-5 top-1/2 -translate-y-1/2 w-5 h-5 text-[#666] pointer-events-none"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
        </div>
      </form>

      {isOpen && (
        <div className="search-dropdown" style={{ zIndex: 1001 }}>
          {isLoading ? (
            <div className="search-dropdown-item">
              <span className="text-[#a3a3a3]">Searching...</span>
            </div>
          ) : results.length > 0 ? (
            <>
              {results.map((stock) => (
                <button
                  key={stock.symbol}
                  onClick={() => handleSelect(stock.symbol)}
                  className="search-dropdown-item"
                >
                  <div className="search-item-content">
                    <div className="search-symbol">{stock.symbol}</div>
                    <div className="search-name">{stock.name}</div>
                  </div>
                </button>
              ))}
            </>
          ) : (
            <div className="search-dropdown-item">
              <span className="text-[#a3a3a3]">No stocks found</span>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
