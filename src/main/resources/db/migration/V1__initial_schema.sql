-- Phase 2 connectivity baseline. Domain tables are introduced in Phase 3 migrations.
CREATE TABLE schema_metadata (
    id BIGINT NOT NULL AUTO_INCREMENT,
    application_name VARCHAR(100) NOT NULL,
    installed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_schema_metadata_application UNIQUE (application_name)
) ENGINE=InnoDB;

INSERT INTO schema_metadata (application_name) VALUES ('local-services-finder');
