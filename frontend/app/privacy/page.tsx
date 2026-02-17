import Link from 'next/link';

export default function PrivacyPage() {
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
        <div style={{ maxWidth: '900px', margin: '0 auto', padding: '0 2rem' }}>
          <h1 style={{ fontSize: '3rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa', textAlign: 'center' }}>
            Privacy Policy
          </h1>
          <p style={{ fontSize: '1rem', color: '#a3a3a3', textAlign: 'center', marginBottom: '3rem' }}>
            Last Updated: January 2025
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                1. Introduction
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                TrueEquity ("we," "our," or "us") is committed to protecting your privacy. This Privacy Policy explains how we collect, 
                use, disclose, and safeguard your information when you use our website and services.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                2. Information We Collect
              </h2>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 600, marginTop: '1rem', marginBottom: '0.5rem', color: '#fafafa' }}>
                2.1 Information You Provide
              </h3>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginBottom: '1rem' }}>
                We may collect information that you voluntarily provide when using our services, including but not limited to:
              </p>
              <ul style={{ color: '#a3a3a3', lineHeight: '1.7', marginLeft: '2rem', marginBottom: '1rem' }}>
                <li>Search queries and stock symbols you search for</li>
                <li>Contact information if you reach out to us</li>
                <li>Any feedback or communications you send to us</li>
              </ul>

              <h3 style={{ fontSize: '1.25rem', fontWeight: 600, marginTop: '1rem', marginBottom: '0.5rem', color: '#fafafa' }}>
                2.2 Automatically Collected Information
              </h3>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                We may automatically collect certain information about your device and usage patterns, including IP address, browser type, 
                operating system, pages visited, and time spent on pages. This information is used to improve our services and user experience.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                3. How We Use Your Information
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginBottom: '1rem' }}>
                We use the information we collect to:
              </p>
              <ul style={{ color: '#a3a3a3', lineHeight: '1.7', marginLeft: '2rem' }}>
                <li>Provide, maintain, and improve our services</li>
                <li>Respond to your inquiries and provide customer support</li>
                <li>Analyze usage patterns to enhance user experience</li>
                <li>Ensure the security and integrity of our platform</li>
                <li>Comply with legal obligations</li>
              </ul>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                4. Information Sharing and Disclosure
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginBottom: '1rem' }}>
                We do not sell, trade, or rent your personal information to third parties. We may share your information only in the following circumstances:
              </p>
              <ul style={{ color: '#a3a3a3', lineHeight: '1.7', marginLeft: '2rem' }}>
                <li><strong>Service Providers:</strong> We may share information with trusted third-party service providers who assist us in operating our platform</li>
                <li><strong>Legal Requirements:</strong> We may disclose information if required by law or in response to valid legal requests</li>
                <li><strong>Business Transfers:</strong> In the event of a merger, acquisition, or sale of assets, your information may be transferred</li>
              </ul>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                5. Data Security
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                We implement appropriate technical and organizational measures to protect your information against unauthorized access, 
                alteration, disclosure, or destruction. However, no method of transmission over the Internet or electronic storage is 100% secure, 
                and we cannot guarantee absolute security.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                6. Your Rights
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginBottom: '1rem' }}>
                Depending on your location, you may have certain rights regarding your personal information, including:
              </p>
              <ul style={{ color: '#a3a3a3', lineHeight: '1.7', marginLeft: '2rem' }}>
                <li>The right to access your personal information</li>
                <li>The right to rectify inaccurate information</li>
                <li>The right to request deletion of your information</li>
                <li>The right to object to processing of your information</li>
                <li>The right to data portability</li>
              </ul>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                7. Cookies and Tracking Technologies
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                We may use cookies and similar tracking technologies to enhance your experience, analyze usage, and assist with marketing efforts. 
                You can control cookies through your browser settings, though this may affect the functionality of our services.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                8. Third-Party Links
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                Our website may contain links to third-party websites. We are not responsible for the privacy practices of these external sites. 
                We encourage you to review the privacy policies of any third-party sites you visit.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                9. Children's Privacy
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                Our services are not intended for individuals under the age of 18. We do not knowingly collect personal information from children. 
                If you believe we have collected information from a child, please contact us immediately.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                10. Changes to This Privacy Policy
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                We may update this Privacy Policy from time to time. We will notify you of any material changes by posting the new Privacy Policy 
                on this page and updating the "Last Updated" date. Your continued use of our services after such changes constitutes acceptance of the updated policy.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                11. Contact Us
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                If you have questions or concerns about this Privacy Policy or our data practices, please contact us through our 
                <Link href="/contact" style={{ color: '#60a5fa', textDecoration: 'underline', marginLeft: '0.25rem' }}>Contact</Link> page.
              </p>
            </div>
          </div>
        </div>
      </main>
    </>
  );
}
