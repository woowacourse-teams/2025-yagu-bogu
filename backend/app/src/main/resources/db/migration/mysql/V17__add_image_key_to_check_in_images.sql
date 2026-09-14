ALTER TABLE check_in_images
    ADD COLUMN image_key VARCHAR(500) NULL AFTER image_url;

ALTER TABLE check_in_images
    MODIFY COLUMN image_url VARCHAR(500) NULL;
