INSERT INTO fraud_rules (
  id,
  name,
  threshold,
  enabled,
  created_at
) VALUES (
  RANDOM_UUID(),
  'HIGH_AMOUNT',
  100000,
  TRUE,
  CURRENT_TIMESTAMP
);
VALUES
(gen_random_uuid(), 'Max Daily Transaction Limit', 'AMOUNT_THRESHOLD',
 '{"max_daily_amount": 1000000, "currency": "NGN"}',
 true, 100, 'Block transactions exceeding daily limit', 'BLOCK'),

(gen_random_uuid(), 'Velocity Check - Hourly', 'VELOCITY_CHECK',
 '{"max_transactions_per_hour": 10}',
 true, 90, 'Monitor transaction frequency per hour', 'REVIEW'),

(gen_random_uuid(), 'Large Amount Alert', 'AMOUNT_THRESHOLD',
 '{"threshold_amount": 100000, "currency": "NGN"}',
 true, 80, 'Alert on suspicious large amounts', 'ALERT'),

(gen_random_uuid(), 'Rapid Transfer Pattern', 'VELOCITY_CHECK',
 '{"max_transfers_per_5_minutes": 3}',
 true, 95, 'Detect rapid succession transfers', 'BLOCK');