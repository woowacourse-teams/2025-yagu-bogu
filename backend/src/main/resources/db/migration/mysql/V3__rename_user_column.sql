-- V3__rename_user_column.sql

ALTER TABLE like_windows
    RENAME COLUMN client_instance_id TO member_id;
