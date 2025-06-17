CREATE TABLE product_gallery_image_urls (
    id UUID NOT NULL PRIMARY KEY,
    product_id UUID NOT NULL,
    image_url VARCHAR(255) NOT NULL,

    CONSTRAINT fk_product_gallery_image_urls_product_id
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);