import Link from 'next/link';

export default function NotFound() {
  return (
    <main className="min-h-screen grid-background flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-6xl font-bold mb-4">404</h1>
        <p className="text-xl text-white/60 mb-8">Stock not found</p>
        <Link
          href="/"
          className="px-6 py-3 glass-strong rounded-xl hover:bg-white/10 transition-colors inline-block"
        >
          Back to Home
        </Link>
      </div>
    </main>
  );
}

