-- Ensure the database exists
SELECT 'CREATE DATABASE wallet_dev'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'wallet_dev')\gexec

-- Ensure user has proper password encryption
ALTER USER wallet_user WITH PASSWORD 'wallet_pass';

-- Grant all privileges
GRANT ALL PRIVILEGES ON DATABASE wallet_dev TO wallet_user;

-- Connect to the database
\c wallet_dev

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO wallet_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO wallet_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO wallet_user;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO wallet_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO wallet_user;