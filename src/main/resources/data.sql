-- create a test owner
INSERT INTO users (id, full_name, email, password, role) VALUES (1, 'Owner One', 'owner1@example.com', 'noop-password', 'GARAGE_OWNER')
ON CONFLICT DO NOTHING;

-- create a test customer
INSERT INTO users (id, full_name, email, password, role) VALUES (2, 'Customer One', 'cust1@example.com', 'noop-password', 'CUSTOMER')
ON CONFLICT DO NOTHING;

-- create a garage owned by owner 1
INSERT INTO garage (id, name, address, phone, owner_id, active) VALUES (1, 'Alpha Garage', 'Main Street', '9876543210', 1, true)
ON CONFLICT DO NOTHING;

-- create a vehicle owned by customer 2
INSERT INTO vehicle (id, make, model, registration_number, owner_id) VALUES (1, 'Honda', 'City', 'KA01AB0001', 2)
ON CONFLICT DO NOTHING;
