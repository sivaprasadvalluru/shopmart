-- Seed categories (INSERT IGNORE avoids re-inserting on restart)
INSERT IGNORE INTO categories (id, name, description) VALUES
  (1, 'Electronics',    'Gadgets, devices, and accessories'),
  (2, 'Clothing',       'Men''s and women''s fashion'),
  (3, 'Home & Kitchen', 'Everything for your home');

-- Seed products
INSERT IGNORE INTO products (id, name, description, price, image_url, category_id) VALUES
  (1,  'Wireless Noise-Cancelling Headphones',
       'Premium over-ear headphones with 30-hour battery life and active noise cancellation.',
       149.99, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400', 1),

  (2,  'Smart Watch Series X',
       'Track fitness, receive notifications, and pay contactlessly — all from your wrist.',
       299.99, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400', 1),

  (3,  'Portable Bluetooth Speaker',
       'Waterproof 360° speaker with 20 hours of playtime, perfect for outdoors.',
       79.99,  'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400', 1),

  (4,  '4K USB-C Monitor 27"',
       'Crystal-clear 3840×2160 display with USB-C power delivery and built-in speakers.',
       449.99, 'https://images.unsplash.com/photo-1527443224154-c4a573d3b196?w=400', 1),

  (5,  'Classic Fit Cotton T-Shirt',
       'Soft, breathable 100% organic cotton tee available in a range of colours.',
       24.99,  'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400', 2),

  (6,  'Slim-Fit Chino Trousers',
       'Versatile stretch chinos that go from the office to the weekend effortlessly.',
       59.99,  'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=400', 2),

  (7,  'Lightweight Down Jacket',
       'Pack-away puffer jacket that keeps you warm without the bulk.',
       119.99, 'https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=400', 2),

  (8,  'Running Sneakers Pro',
       'Responsive cushioning and breathable mesh upper built for long-distance comfort.',
       89.99,  'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400', 2),

  (9,  'Stainless Steel Cookware Set (10 pc)',
       'Professional-grade pots and pans with tri-ply construction and ergonomic handles.',
       189.99, 'https://images.unsplash.com/photo-1584990347449-a9e56f3b5da6?w=400', 3),

  (10, 'Programmable Coffee Maker',
       '12-cup coffee maker with built-in grinder, brew-strength control, and keep-warm plate.',
       99.99,  'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400', 3),

  (11, 'Bamboo Cutting Board Set',
       'Set of three eco-friendly cutting boards in small, medium, and large sizes.',
       34.99,  'https://images.unsplash.com/photo-1614628577-3d17c8d5bfb3?w=400', 3),

  (12, 'Robot Vacuum Cleaner',
       'Intelligent mapping, auto-empty base, and 180-minute runtime for whole-home cleaning.',
       349.99, 'https://images.unsplash.com/photo-1588359348347-9bc6cbbb689e?w=400', 3);
