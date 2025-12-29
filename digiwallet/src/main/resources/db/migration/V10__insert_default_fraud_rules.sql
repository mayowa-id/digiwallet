INSERT INTO fraud_rules(
  id,
  name,
  rule_type,
  config,
  enabled,
  priority,
  description,
  action,
  created_at
) VALUES
(
  RANDOM_UUID(),
  'Max Daily Transaction Limit',
  'AMOUNT_THRESHOLD',
  '{"max_daily_amount": 1000000, "currency": "NGN"}',
  TRUE,
  100,
  'Block transactions exceeding daily limit',
  'BLOCK',
  CURRENT_TIMESTAMP
),
(
  RANDOM_UUID(),
  'Velocity Check - Hourly',
  'VELOCITY_CHECK',
  '{"max_transactions_per_hour": 10}',
  TRUE,
  90,
  'Monitor transaction frequency per hour',
  'REVIEW',
  CURRENT_TIMESTAMP
),
(
  RANDOM_UUID(),
  'Large Amount Alert',
  'AMOUNT_THRESHOLD',
  '{"threshold_amount": 100000, "currency": "NGN"}',
  TRUE,
  80,
  'Alert on suspicious large amounts',
  'ALERT',
  CURRENT_TIMESTAMP
),
(
  RANDOM_UUID(),
  'Rapid Transfer Pattern',
  'VELOCITY_CHECK',
  '{"max_transfers_per_5_minutes": 3}',
  TRUE,
  95,
  'Detect rapid succession transfers',
  'BLOCK',
  CURRENT_TIMESTAMP
);
