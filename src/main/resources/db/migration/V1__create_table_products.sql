CREATE TABLE products (
    product_id UUID NOT NULL PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    brand VARCHAR(255),
    price NUMERIC(19, 2),
    category VARCHAR(255),
    sub_category VARCHAR(255),
    image_url VARCHAR(255),
    condition VARCHAR(255)
);