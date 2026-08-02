-- Seed users (passwords are BCrypt hashes of admin123 / customer123)
INSERT IGNORE INTO users (id, email, password, role, created_at) VALUES
  (1, 'admin@shopmart.com', '$2b$12$7oXjfxHA4wz94gQ6EIKUxumP2X.lDmqPwfv2BvpZ089YhtaL16y62', 'ADMIN', NOW()),
  (2, 'customer@shopmart.com', '$2b$12$zssifYBg6VnexwTowhTpcu19dRptXXXHfufc8EIItXSASKYN5rtkO', 'CUSTOMER', NOW());

-- Seed products: Electronics, Clothing, Books
INSERT IGNORE INTO products (id, name, description, price, stock_quantity, category, active) VALUES
  (1, 'Wireless Noise-Cancelling Headphones', 'Over-ear Bluetooth headphones with active noise cancellation and 30-hour battery life.', 149.99, 42, 'Electronics', true),
  (2, '4K Ultra HD Smart TV 55-inch', 'Smart TV with HDR support, built-in streaming apps, and voice remote.', 499.00, 15, 'Electronics', true),
  (3, 'Mechanical Gaming Keyboard', 'RGB backlit mechanical keyboard with hot-swappable switches.', 89.99, 60, 'Electronics', true),
  (4, 'Portable Bluetooth Speaker', 'Waterproof speaker with 12-hour playtime and deep bass.', 39.99, 100, 'Electronics', true),
  (5, 'Men''s Slim Fit Denim Jacket', 'Classic denim jacket with a modern slim fit cut.', 59.99, 35, 'Clothing', true),
  (6, 'Women''s Running Shoes', 'Lightweight breathable running shoes with cushioned soles.', 74.99, 50, 'Clothing', true),
  (7, 'Unisex Cotton Hoodie', 'Soft fleece-lined hoodie, available in multiple colors.', 34.99, 80, 'Clothing', true),
  (8, 'Merino Wool Socks (3-Pack)', 'Moisture-wicking merino wool socks for everyday wear.', 19.99, 120, 'Clothing', true),
  (9, 'The Pragmatic Programmer', '20th Anniversary Edition of the classic software craftsmanship book.', 44.99, 25, 'Books', true),
  (10, 'Clean Code', 'A handbook of agile software craftsmanship by Robert C. Martin.', 39.99, 30, 'Books', true),
  (11, 'Atomic Habits', 'An easy and proven way to build good habits and break bad ones.', 17.99, 90, 'Books', true),
  (12, 'Sapiens: A Brief History of Humankind', 'A thought-provoking look at the history of the human species.', 21.99, 45, 'Books', true);
