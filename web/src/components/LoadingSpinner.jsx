import React, { useEffect, useState } from 'react';
import styles from './LoadingSpinner.module.css';

export default function LoadingSpinner({ message = 'Loading...', showTimer = true }) {
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    if (!showTimer) return undefined;
    setSeconds(0);
    const t = setInterval(() => setSeconds((s) => s + 1), 1000);
    return () => clearInterval(t);
  }, [showTimer]);

  return (
    <div className={styles.loadingContainer} role="status" aria-live="polite">
      <div className={styles.spinner} aria-hidden="true">
        <div className={styles.dot} />
        <div className={styles.dot} />
        <div className={styles.dot} />
      </div>
      <div className={styles.loadingText}>
        <div>{message}</div>
        {showTimer && <div className={styles.waitTime}>Waited: {seconds}s</div>}
      </div>
    </div>
  );
}
