ALTER TABLE members
    MODIFY COLUMN provider ENUM ('GOOGLE','APPLE') NOT NULL;

ALTER TABLE members
    ADD UNIQUE KEY uq_members_provider_oauth (provider, oauth_id);
