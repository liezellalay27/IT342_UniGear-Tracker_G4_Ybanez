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

m = re.match(r'jdbc:postgresql://([^:/?#]+)(?::(\d+))?/([^?]+)', jdbc)
if not m:
    print('Could not parse DB_URL:', jdbc)
    raise SystemExit(1)

host = m.group(1)
port = int(m.group(2)) if m.group(2) else 5432
dbname = m.group(3)

import psycopg2
conn = psycopg2.connect(host=host, port=port, dbname=dbname, user=user, password=password)
cur = conn.cursor()
cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name='equipment';")
cols = [r[0] for r in cur.fetchall()]

if 'serial_number' in cols:
    cur.execute("""
    SELECT lower(trim(serial_number)) AS serial_norm,
           COUNT(*) AS cnt,
           array_agg(id ORDER BY id) AS ids,
           array_agg(coalesce(name, '') ORDER BY id) AS names
    FROM equipment
    WHERE serial_number IS NOT NULL AND trim(serial_number) <> ''
    GROUP BY lower(trim(serial_number))
    HAVING COUNT(*) > 1
    ORDER BY cnt DESC
    LIMIT 200;
    """)
    mode = 'serial'
else:
    # Fall back to possible duplicates by name + category
    cur.execute("""
    SELECT lower(trim(name)) || '||' || coalesce(lower(trim(category)), '') AS key_norm,
           COUNT(*) AS cnt,
           array_agg(id ORDER BY id) AS ids,
           array_agg(coalesce(name, '') ORDER BY id) AS names,
           array_agg(coalesce(category, '') ORDER BY id) AS categories
    FROM equipment
    WHERE name IS NOT NULL AND trim(name)<>''
    GROUP BY lower(trim(name)), coalesce(lower(trim(category)), '')
    HAVING COUNT(*) > 1
    ORDER BY cnt DESC
    LIMIT 200;
    """)
    mode = 'name_category'
rows = cur.fetchall()
if not rows:
    print('No duplicate serial groups found.')
else:
    if mode == 'serial':
        print(f'Found {len(rows)} duplicate serial groups (showing up to 200):')
        for serial, cnt, ids, names in rows:
            print('-' * 60)
            print('Serial (normalized):', serial)
            print('Count:', cnt)
            print('IDs:', ids)
            print('Names:', names[:len(ids)])
    else:
        print(f'Found {len(rows)} duplicate name+category groups (showing up to 200):')
        for key, cnt, ids, names, categories in rows:
            print('-' * 60)
            print('Name+Category key:', key)
            print('Count:', cnt)
            print('IDs:', ids)
            print('Names:', names[:len(ids)])
            print('Categories:', categories[:len(ids)])

cur.close()
conn.close()
