-- V3__rename_user_column.sql

ALTER TABLE like_windows
    CHANGE client_instance_id member_id BIGINT NOT NULL;
