INSERT INTO categories (id, name, description) VALUES
  (1, 'Electronics', 'Gadgets and devices'),
  (2, 'Clothing',    'Fashion items');

INSERT INTO products (id, name, short_description, description, price, image_url, category_id) VALUES
  (1, 'Test Headphones', 'Great sound, test item.', 'Great sound', 99.99,  'https://example.com/img1.jpg', 1),
  (2, 'Test T-Shirt',    'Comfortable test shirt.', 'Comfortable', 19.99,  'https://example.com/img2.jpg', 2);
