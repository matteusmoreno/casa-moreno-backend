ALTER TABLE products RENAME COLUMN brand TO product_brand;
ALTER TABLE products RENAME COLUMN price TO current_price;
ALTER TABLE products RENAME COLUMN condition TO product_condition;
ALTER TABLE products DROP COLUMN image_url;
