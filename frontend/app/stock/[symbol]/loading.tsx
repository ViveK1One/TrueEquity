export default function Loading() {
  return (
    <main className="min-h-screen grid-background">
      <nav className="container mx-auto px-6 py-6 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 bg-gradient-to-br from-white to-white/60 rounded-lg"></div>
          <span className="text-xl font-semibold">TrueEquity</span>
        </div>
      </nav>

      <div className="container mx-auto px-6 py-12">
        <div className="space-y-6">
          {/* Header Skeleton */}
          <div className="skeleton h-32 rounded-2xl"></div>

          {/* Recommendation Badge Skeleton */}
          <div className="skeleton h-24 rounded-2xl"></div>

          {/* Chart Skeleton */}
          <div className="skeleton h-64 rounded-2xl"></div>

          {/* Content Grid Skeleton */}
          <div className="grid lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 space-y-6">
              <div className="skeleton h-48 rounded-2xl"></div>
              <div className="skeleton h-64 rounded-2xl"></div>
            </div>
            <div className="space-y-6">
              <div className="skeleton h-48 rounded-2xl"></div>
              <div className="skeleton h-48 rounded-2xl"></div>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

