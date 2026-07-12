-- The Docker entrypoint creates recruitment_db from POSTGRES_DB before this script runs.
-- This guard keeps the bootstrap safe if the script is executed manually on the postgres database.
\connect postgres
SELECT format('CREATE DATABASE %I', 'recruitment_db')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'recruitment_db')\gexec

\connect recruitment_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Future schema changes must be delivered as versioned Flyway migrations in ../migrations.
-- Do not add application tables to this bootstrap script.
