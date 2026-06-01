import React from 'react';
import { Link, Navigate } from 'react-router-dom';
import { getCurrentUser, isAuthenticated } from '../services/authService';
import styles from './LandingPage.module.css';
import logo from '../assets/UniGear Symbol.png';

function LandingPage() {
  if (isAuthenticated()) {
    const user = getCurrentUser();
    if (user?.role === 'ADMIN') {
      return <Navigate to="/admin?tab=overview" replace />;
    }
    return <Navigate to="/dashboard" replace />;
  }

  const highlights = [
    {
      title: 'Fast approvals',
      text: 'Track requests, add notes, and send decisions with a flow that stays consistent across web and mobile.'
    },
    {
      title: 'Live inventory',
      text: 'Manage equipment availability, stock levels, and borrowed items from one admin-ready dashboard.'
    },
    {
      title: 'Student-friendly',
      text: 'Students can browse equipment, submit requests, and review status updates without jumping between tools.'
    },
    {
      title: 'Real-time visibility',
      text: 'See pending approvals, borrowed items, and low-stock alerts in one crisp admin-ready interface.'
    }
  ];

  const workflow = [
    {
      step: '01',
      title: 'Browse inventory',
      text: 'Students search equipment, compare availability, and open a polished detail view before requesting.'
    },
    {
      step: '02',
      title: 'Approve with context',
      text: 'Admins review the request, add a note, and keep the approval PDF aligned with the email flow.'
    },
    {
      step: '03',
      title: 'Track everything',
      text: 'Borrowed records, return status, and user history remain visible across web and mobile.'
    }
  ];

  const audiences = [
    {
      title: 'For students',
      text: 'Browse equipment, submit requests, and track status updates without guessing where things stand.'
    },
    {
      title: 'For admins',
      text: 'Handle approvals, inventory, notes, borrowed items, and user management from one dashboard.'
    },
    {
      title: 'For the department',
      text: 'Keep the workflow consistent across web and mobile so the entire process feels connected.'
    }
  ];

  const testimonials = [
    {
      quote: 'Everything is in one place now. The approval flow is much easier to follow.',
      name: 'Admin dashboard view'
    },
    {
      quote: 'The request process feels cleaner, especially on mobile.',
      name: 'Student experience'
    }
  ];

  const stats = [
    { value: '24/7', label: 'Accessible workflow' },
    { value: '3 views', label: 'Student, admin, and records' },
    { value: '1 flow', label: 'Shared backend logic' }
  ];

  return (
    <div className={styles.page}>
      <div className={styles.backdropOrbA} />
      <div className={styles.backdropOrbB} />
      <div className={styles.backdropGrid} />

      <header className={styles.navbar}>
        <div className={styles.brand}>
          <img src={logo} alt="UniGear Tracker" className={styles.brandLogo} />
          <div>
            <div className={styles.brandKicker}>University Equipment System</div>
            <div className={styles.brandName}>UniGear Tracker</div>
          </div>
        </div>

        <div className={styles.navActions}>
          <span className={styles.navBadge}>Web + Mobile aligned</span>
          <Link to="/login" className={styles.navLink}>Login</Link>
          <Link to="/register" className={styles.navButton}>Create Account</Link>
        </div>
      </header>

      <main className={styles.heroShell}>
        <section className={styles.hero}>
          <div className={styles.heroCopy}>
            <span className={styles.pill}>Borrow. Approve. Track. Repeat.</span>
            <div className={styles.heroMetaRow}>
              <span>Connected approvals</span>
              <span>Live equipment records</span>
              <span>Branded email PDFs</span>
            </div>
            <h1>Equipment requests that feel organized from first click to final return.</h1>
            <p>
              UniGear Tracker gives students a clean borrowing flow and gives admins one place to manage equipment,
              approvals, notes, and returns across web and mobile.
            </p>

            <div className={styles.heroActions}>
              <Link to="/register" className={styles.primaryAction}>Get started</Link>
              <Link to="/login" className={styles.secondaryAction}>Sign in</Link>
            </div>

            <div className={styles.statsRow}>
              {stats.map((item) => (
                <div key={item.label} className={styles.statCard}>
                  <strong>{item.value}</strong>
                  <span>{item.label}</span>
                </div>
              ))}
            </div>
          </div>

          <div className={styles.heroPanel}>
            <div className={styles.panelGlow} />
            <div className={styles.panelCard}>
              <div className={styles.panelHeader}>
                <span className={styles.panelLabel}>Admin command view</span>
                <span className={styles.panelStatus}>Live</span>
              </div>
              <div className={styles.panelMetric}>
                <span>Pending approvals</span>
                <strong>12</strong>
              </div>
              <div className={styles.panelMetric}>
                <span>Borrowed items</span>
                <strong>28</strong>
              </div>
              <div className={styles.panelMetric}>
                <span>Low stock alerts</span>
                <strong>4</strong>
              </div>
              <div className={styles.panelFooter}>
                <span>Notes, approvals, and PDF email attachments stay aligned.</span>
              </div>
            </div>
          </div>
        </section>

        <section className={styles.marqueeBar}>
          <span>Inventory visibility</span>
          <span>Approval notes</span>
          <span>Borrow tracking</span>
          <span>Mobile parity</span>
          <span>Branded email PDFs</span>
        </section>

        <section className={styles.featureGrid}>
          {highlights.map((item) => (
            <article key={item.title} className={styles.featureCard} style={{ animationDelay: `${highlights.indexOf(item) * 90}ms` }}>
              <div className={styles.featureIndex}>{item.title.slice(0, 2).toUpperCase()}</div>
              <h2>{item.title}</h2>
              <p>{item.text}</p>
            </article>
          ))}
        </section>

        <section className={styles.sectionBlock}>
          <div className={styles.sectionHeading}>
            <span className={styles.sectionKicker}>Workflow</span>
            <h2>Designed to move from browsing to approval without friction.</h2>
          </div>

          <div className={styles.workflowGrid}>
            {workflow.map((item) => (
              <article key={item.step} className={styles.workflowCard}>
                <div className={styles.workflowStep}>{item.step}</div>
                <h3>{item.title}</h3>
                <p>{item.text}</p>
              </article>
            ))}
          </div>
        </section>

        <section className={styles.sectionBlock}>
          <div className={styles.sectionHeadingAlt}>
            <span className={styles.sectionKicker}>Who it serves</span>
            <h2>One system, three audiences, zero disconnected workflows.</h2>
          </div>

          <div className={styles.audienceGrid}>
            {audiences.map((item) => (
              <article key={item.title} className={styles.audienceCard}>
                <h3>{item.title}</h3>
                <p>{item.text}</p>
              </article>
            ))}
          </div>
        </section>

        <section className={styles.quoteStrip}>
          {testimonials.map((item) => (
            <div key={item.name} className={styles.quoteCard}>
              <p>“{item.quote}”</p>
              <span>{item.name}</span>
            </div>
          ))}
        </section>

        <section className={styles.bottomBand}>
          <div>
            <h3>Built for the full workflow</h3>
            <p>
              The web app now starts with a proper landing page, then moves into login, catalog, request handling,
              and admin decisions with a consistent UniGear look.
            </p>
          </div>
          <div className={styles.bottomActions}>
            <Link to="/dashboard" className={styles.bandLink}>Open catalog</Link>
            <Link to="/forgot-password" className={styles.bandGhost}>Reset password</Link>
          </div>
        </section>
      </main>
    </div>
  );
}

export default LandingPage;