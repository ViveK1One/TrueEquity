import { Pool } from 'pg';

// Database connection pool
// Get password from environment variable, ensure it's a string
const dbPassword = process.env.DATABASE_PASSWORD;
if (!dbPassword) {
  console.warn('DATABASE_PASSWORD not set in environment variables. Using default.');
}

const pool = new Pool({
  host: process.env.DATABASE_HOST || 'localhost',
  port: parseInt(process.env.DATABASE_PORT || '5432'),
  database: process.env.DATABASE_NAME || 'trueequity_market_data',
  user: process.env.DATABASE_USER || 'postgres',
  password: dbPassword ? String(dbPassword) : 'Vivek@025!', // Default password from Java config
  max: 20, // Maximum number of clients in the pool
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000,
});

// Test connection
pool.on('connect', () => {
  console.log('Database connected');
});

pool.on('error', (err) => {
  console.error('Unexpected database error', err);
});

export default pool;

