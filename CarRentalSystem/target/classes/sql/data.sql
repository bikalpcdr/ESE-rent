-- Initialize admin user
INSERT INTO users (username, password, email, full_name, phone_number, role, enabled)
VALUES ('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'admin@eserent.com', 'System Administrator', '+1234567890', 'ROLE_ADMIN', true);

-- Initialize sample landlord users
INSERT INTO users (username, password, email, full_name, phone_number, role, enabled)
VALUES ('landlord1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'landlord1@example.com', 'John Landlord', '+1234567891', 'ROLE_LANDLORD', true);

INSERT INTO users (username, password, email, full_name, phone_number, role, enabled)
VALUES ('landlord2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'landlord2@example.com', 'Jane Landlord', '+1234567892', 'ROLE_LANDLORD', true);

-- Initialize sample customer users
INSERT INTO users (username, password, email, full_name, phone_number, role, enabled)
VALUES ('customer1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'customer1@example.com', 'Bob Customer', '+1234567893', 'ROLE_CUSTOMER', true);

INSERT INTO users (username, password, email, full_name, phone_number, role, enabled)
VALUES ('customer2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'customer2@example.com', 'Alice Customer', '+1234567894', 'ROLE_CUSTOMER', true);

-- Note: The password for all users is 'password'
