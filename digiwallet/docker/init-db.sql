-- Create database if it doesn't exist
SELECT 'CREATE DATABASE wallet_dev'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'wallet_dev')\gexec

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE wallet_dev TO wallet_user;