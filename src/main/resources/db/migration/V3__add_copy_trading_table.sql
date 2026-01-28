-- Support for copy trading relationships
CREATE TABLE copy_relations (
    id BIGSERIAL PRIMARY KEY,
    lead_user_id BIGINT NOT NULL REFERENCES users(id),
    follower_user_id BIGINT NOT NULL REFERENCES users(id),
    scale_factor DECIMAL(10, 4) NOT NULL DEFAULT 1.0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_relation UNIQUE (lead_user_id, follower_user_id)
);

-- Index for finding followers of a lead trader
CREATE INDEX idx_copy_relations_lead ON copy_relations(lead_user_id);
CREATE INDEX idx_copy_relations_follower ON copy_relations(follower_user_id);
