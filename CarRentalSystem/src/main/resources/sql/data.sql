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

-- Initialize sample rooms for landlord1
INSERT INTO rooms (title, description, price_per_night, capacity, room_type, address, city, country, available, landlord_id)
VALUES ('Luxury Apartment', 'Beautiful apartment with ocean view', 150.00, 4, 'APARTMENT', '123 Ocean Drive', 'Miami', 'USA', true, 
        (SELECT id FROM users WHERE username = 'landlord1'));

INSERT INTO rooms (title, description, price_per_night, capacity, room_type, address, city, country, available, landlord_id)
VALUES ('Cozy Studio', 'Perfect for solo travelers or couples', 80.00, 2, 'STUDIO', '456 Main Street', 'New York', 'USA', true,
        (SELECT id FROM users WHERE username = 'landlord1'));

INSERT INTO rooms (title, description, price_per_night, capacity, room_type, address, city, country, available, landlord_id)
VALUES ('Mountain Cabin', 'Rustic cabin with amazing mountain views', 120.00, 6, 'HOUSE', '789 Mountain Road', 'Denver', 'USA', true,
        (SELECT id FROM users WHERE username = 'landlord1'));

-- Initialize sample rooms for landlord2
INSERT INTO rooms (title, description, price_per_night, capacity, room_type, address, city, country, available, landlord_id)
VALUES ('Modern Loft', 'Contemporary loft in downtown area', 140.00, 3, 'APARTMENT', '101 Downtown Ave', 'Chicago', 'USA', true,
        (SELECT id FROM users WHERE username = 'landlord2'));

INSERT INTO rooms (title, description, price_per_night, capacity, room_type, address, city, country, available, landlord_id)
VALUES ('Beach House', 'Spacious house steps from the beach', 200.00, 8, 'HOUSE', '202 Beachfront Dr', 'Los Angeles', 'USA', true,
        (SELECT id FROM users WHERE username = 'landlord2'));

-- Add some room images
INSERT INTO room_images (url, room_id)
VALUES ('https://images.unsplash.com/photo-1522708323590-d24dbb6b0267', 
        (SELECT id FROM rooms WHERE title = 'Luxury Apartment'));

INSERT INTO room_images (url, room_id)
VALUES ('https://images.unsplash.com/photo-1502672260266-1c1ef2d93688', 
        (SELECT id FROM rooms WHERE title = 'Cozy Studio'));

INSERT INTO room_images (url, room_id)
VALUES ('https://images.unsplash.com/photo-1542718610-a1d656d1884c', 
        (SELECT id FROM rooms WHERE title = 'Mountain Cabin'));

INSERT INTO room_images (url, room_id)
VALUES ('https://images.unsplash.com/photo-1536376072261-38c75010e6c9',
        (SELECT id FROM rooms WHERE title = 'Modern Loft'));

INSERT INTO room_images (url, room_id)
VALUES ('https://images.unsplash.com/photo-1560448204-603b3fc33ddc',
        (SELECT id FROM rooms WHERE title = 'Beach House'));
        
-- Add some sample bookings
INSERT INTO bookings (check_in_date, check_out_date, total_price, status, customer_id, room_id)
VALUES ('2025-04-20', '2025-04-25', 750.00, 'CONFIRMED', 
        (SELECT id FROM users WHERE username = 'customer1'),
        (SELECT id FROM rooms WHERE title = 'Luxury Apartment'));

INSERT INTO bookings (check_in_date, check_out_date, total_price, status, customer_id, room_id)
VALUES ('2025-05-10', '2025-05-15', 400.00, 'CONFIRMED', 
        (SELECT id FROM users WHERE username = 'customer1'),
        (SELECT id FROM rooms WHERE title = 'Cozy Studio'));

INSERT INTO bookings (check_in_date, check_out_date, total_price, status, customer_id, room_id)
VALUES ('2025-05-01', '2025-05-07', 840.00, 'PENDING', 
        (SELECT id FROM users WHERE username = 'customer2'),
        (SELECT id FROM rooms WHERE title = 'Modern Loft'));

-- Add sample payments for the confirmed bookings
INSERT INTO payments (amount, payment_date, status, transaction_id, booking_id)
VALUES (750.00, '2025-04-01', 'COMPLETED', 'TRX12345', 
        (SELECT id FROM bookings WHERE check_in_date = '2025-04-20' AND customer_id = (SELECT id FROM users WHERE username = 'customer1')));

INSERT INTO payments (amount, payment_date, status, transaction_id, booking_id)
VALUES (400.00, '2025-04-05', 'COMPLETED', 'TRX67890', 
        (SELECT id FROM bookings WHERE check_in_date = '2025-05-10' AND customer_id = (SELECT id FROM users WHERE username = 'customer1')));
