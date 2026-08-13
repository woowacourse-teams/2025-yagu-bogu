CREATE TABLE restaurants
(
    restaurant_id BIGINT        NOT NULL AUTO_INCREMENT,
    content_id    VARCHAR(50)   NOT NULL,
    stadium_id    BIGINT        NOT NULL,
    title         VARCHAR(255)  NOT NULL,
    address       VARCHAR(512)  NULL,
    map_x         DOUBLE        NOT NULL,
    map_y         DOUBLE        NOT NULL,
    distance      INT           NULL,
    tel           VARCHAR(100)  NULL,
    image_url     VARCHAR(1024) NULL,
    created_at    DATETIME(6)   NOT NULL,
    updated_at    DATETIME(6)   NOT NULL,
    deleted_at    DATETIME(6)   NULL,
    PRIMARY KEY (restaurant_id),
    UNIQUE KEY uq_restaurants_content_stadium (content_id, stadium_id),
    CONSTRAINT fk_restaurants_stadium FOREIGN KEY (stadium_id) REFERENCES stadiums (stadium_id)
) ENGINE = InnoDB;
