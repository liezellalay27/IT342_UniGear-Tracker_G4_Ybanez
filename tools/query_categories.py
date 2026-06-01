import os
import re
from pathlib import Path

# Load .env
env_path = Path(__file__).resolve().parents[1] / 'backend' / '.env'
props = {}
with open(env_path, 'r', encoding='utf-8') as f:
    for line in f:
        line = line.strip()
        if not line or line.startswith('#'):
            continue
        if '=' in line:
            k, v = line.split('=', 1)
            props[k.strip()] = v.strip()

jdbc = props.get('DB_URL')
user = props.get('DB_USERNAME')
password = props.get('DB_PASSWORD')

if not jdbc or not user:
    print('Missing DB_URL or DB_USERNAME in .env')
    raise SystemExit(1)

# Parse jdbc:postgresql://host:port/dbname?...
m = re.match(r'jdbc:postgresql://([^:/?#]+)(?::(\d+))?/([^?]+)', jdbc)
if not m:
    print('Could not parse DB_URL:', jdbc)
    raise SystemExit(1)

host = m.group(1)
port = int(m.group(2)) if m.group(2) else 5432
dbname = m.group(3)

try:
    import psycopg2
except Exception:
    print('psycopg2 not installed')
    raise

conn = psycopg2.connect(host=host, port=port, dbname=dbname, user=user, password=password)
cur = conn.cursor()
cur.execute("SELECT category FROM equipment WHERE category IS NOT NULL AND trim(category)<>'' GROUP BY category ORDER BY lower(category) LIMIT 100;")
rows = cur.fetchall()
for r in rows:
    print(r[0])
cur.close()
conn.close()
