import Link from 'next/link';

export default function ContactPage() {
  return (
    <>
      <div className="gradient-bg"></div>
      <div className="grid-overlay"></div>

      <nav>
        <div className="logo">
          <div className="logo-icon">T</div>
          <Link href="/" style={{ color: '#fafafa', textDecoration: 'none' }}>TrueEquity</Link>
        </div>
        <ul className="nav-links">
          <li><Link href="/">Home</Link></li>
          <li><Link href="/stocks">Stocks</Link></li>
          <li><Link href="/pricing">Pricing</Link></li>
          <li><Link href="/#faq">FAQ</Link></li>
        </ul>
      </nav>

      <main style={{ paddingTop: '100px', paddingBottom: '4rem', minHeight: '100vh' }}>
        <div style={{ maxWidth: '800px', margin: '0 auto', padding: '0 2rem' }}>
          <h1 style={{ fontSize: '3rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa', textAlign: 'center' }}>
            Contact Us
          </h1>
          <p style={{ fontSize: '1.25rem', color: '#a3a3a3', textAlign: 'center', marginBottom: '4rem' }}>
            Have questions? We're here to help.
          </p>

          <div className="prop-card" style={{ marginBottom: '2rem' }}>
            <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
              Get in Touch
            </h2>
            <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginBottom: '1.5rem' }}>
              If you have questions about TrueEquity, our features, or need support, please reach out to us.
            </p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <h3 style={{ color: '#fafafa', fontWeight: 600, marginBottom: '0.5rem' }}>General Inquiries</h3>
                <p style={{ color: '#a3a3a3' }}>For general questions about TrueEquity and our services.</p>
              </div>
              <div>
                <h3 style={{ color: '#fafafa', fontWeight: 600, marginBottom: '0.5rem' }}>Technical Support</h3>
                <p style={{ color: '#a3a3a3' }}>If you're experiencing technical issues or need help using the platform.</p>
              </div>
              <div>
                <h3 style={{ color: '#fafafa', fontWeight: 600, marginBottom: '0.5rem' }}>Feedback</h3>
                <p style={{ color: '#a3a3a3' }}>We welcome your feedback and suggestions to improve TrueEquity.</p>
              </div>
            </div>
          </div>

          <div className="prop-card">
            <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
              Frequently Asked Questions
            </h2>
            <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginBottom: '1rem' }}>
              Before contacting us, you might find answers to common questions in our <Link href="/#faq" style={{ color: '#60a5fa', textDecoration: 'underline' }}>FAQ section</Link>.
            </p>
            <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
              For detailed documentation on how to use TrueEquity, please visit our <Link href="/docs" style={{ color: '#60a5fa', textDecoration: 'underline' }}>Documentation</Link> page.
            </p>
          </div>
        </div>
      </main>
    </>
  );
}
