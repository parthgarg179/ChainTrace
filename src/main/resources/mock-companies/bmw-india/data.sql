-- Demo data for BMW India (invented for a college project). Every account's password is: demo123

INSERT INTO account_holders (acc_no, first_name, last_name, login_email, password_sha256, state_code) VALUES
 ('BI1001', 'Karan',  'Singh', 'karan@bmw.demo',  'd3ad9315b7be5dd53b31a273b3b3aba5defe700808305aa16a3062b76658a791', 'PB'),
 ('BI1002', 'Meera',  'Joshi', 'meera@bmw.demo',  'd3ad9315b7be5dd53b31a273b3b3aba5defe700808305aa16a3062b76658a791', 'MH'),
 ('BI1003', 'Vikram', 'Rao',   'vikram@bmw.demo', 'd3ad9315b7be5dd53b31a273b3b3aba5defe700808305aa16a3062b76658a791', 'KA'),
 ('BI1004', 'Ananya', 'Das',   'ananya@bmw.demo', 'd3ad9315b7be5dd53b31a273b3b3aba5defe700808305aa16a3062b76658a791', 'WB');

INSERT INTO vehicles (vin, make_model, variant, fuel_type, manufactured_on, warranty_valid_till, first_registered_state, accident_reported, open_recall) VALUES
 -- three owners, warranty expired
 ('WBA5R1C0XK8B45210', 'BMW 3 Series Gran Limousine', '330Li M Sport', 'Petrol',   DATE '2019-03-22', DATE '2022-04-10', 'Maharashtra', FALSE, NULL),
 -- two owners, accident reported, warranty active
 ('WBA11AA0XP5Y78830', 'BMW X1',                      'sDrive18d M Sport', 'Diesel', DATE '2023-08-02', DATE '2028-09-05', 'Karnataka', TRUE, NULL),
 -- brand-new EV, one owner, open recall campaign
 ('WBA31EF06S8P01200', 'BMW iX1 LWB',                 'eDrive20L M Sport', 'Electric', DATE '2025-11-10', DATE '2033-12-01', 'Punjab', FALSE,
  'High-voltage battery management software update');

INSERT INTO title_transfers (transfer_id, vin, to_acc_no, transfer_date, transfer_mode, odometer_km, dealer_name) VALUES
 (1, 'WBA5R1C0XK8B45210', 'BI1002', DATE '2019-04-10', 'DEALER_SALE',    12, 'BMW Dealer, Pune'),
 (2, 'WBA5R1C0XK8B45210', 'BI1003', DATE '2021-09-01', 'RESALE',      38450, NULL),
 (3, 'WBA5R1C0XK8B45210', 'BI1001', DATE '2024-02-18', 'RESALE',      71200, NULL),
 (4, 'WBA11AA0XP5Y78830', 'BI1004', DATE '2023-09-05', 'DEALER_SALE',     8, 'BMW Dealer, Bengaluru'),
 (5, 'WBA11AA0XP5Y78830', 'BI1003', DATE '2025-06-12', 'RESALE',      24300, NULL),
 (6, 'WBA31EF06S8P01200', 'BI1001', DATE '2025-12-01', 'DEALER_SALE',     5, 'BMW Dealer, Ludhiana');
