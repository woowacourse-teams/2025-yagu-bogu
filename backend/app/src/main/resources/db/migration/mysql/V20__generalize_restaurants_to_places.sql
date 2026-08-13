ALTER TABLE restaurants RENAME TO places;

ALTER TABLE places
    CHANGE COLUMN restaurant_id place_id BIGINT NOT NULL AUTO_INCREMENT,
    ADD COLUMN content_type_id INT NOT NULL DEFAULT 39 AFTER content_id,
    ADD COLUMN category ENUM ('ATTRACTION', 'PERFORMANCE', 'LODGING', 'RESTAURANT', 'CAFE')
        NOT NULL DEFAULT 'RESTAURANT' AFTER content_type_id,
    ADD COLUMN detail_info JSON NULL AFTER image_url;

ALTER TABLE places RENAME INDEX uq_restaurants_content_stadium TO uq_places_content_stadium;

ALTER TABLE places DROP FOREIGN KEY fk_restaurants_stadium;
ALTER TABLE places ADD CONSTRAINT fk_places_stadium FOREIGN KEY (stadium_id) REFERENCES stadiums (stadium_id);
