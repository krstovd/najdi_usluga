CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(191) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'PROVIDER', 'ADMIN'))
) ENGINE=InnoDB;

CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    description VARCHAR(500) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_name UNIQUE (name),
    CONSTRAINT uk_categories_slug UNIQUE (slug)
) ENGINE=InnoDB;

CREATE TABLE service_providers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    business_name VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(191) NOT NULL,
    website VARCHAR(255) NULL,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    average_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    review_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_service_providers_owner UNIQUE (owner_id),
    CONSTRAINT fk_service_providers_owner FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT fk_service_providers_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT chk_provider_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_provider_longitude CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT chk_provider_status CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED', 'SUSPENDED')),
    CONSTRAINT chk_provider_average_rating CHECK (average_rating BETWEEN 0 AND 5),
    CONSTRAINT chk_provider_review_count CHECK (review_count >= 0)
) ENGINE=InnoDB;

CREATE TABLE availability (
    id BIGINT NOT NULL AUTO_INCREMENT,
    service_provider_id BIGINT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    start_time TIME NULL,
    end_time TIME NULL,
    closed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_availability_provider_day UNIQUE (service_provider_id, day_of_week),
    CONSTRAINT fk_availability_provider FOREIGN KEY (service_provider_id) REFERENCES service_providers (id) ON DELETE CASCADE,
    CONSTRAINT chk_availability_day CHECK (day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    CONSTRAINT chk_availability_hours CHECK (
        (closed = TRUE AND start_time IS NULL AND end_time IS NULL)
        OR (closed = FALSE AND start_time IS NOT NULL AND end_time IS NOT NULL AND start_time < end_time)
    )
) ENGINE=InnoDB;

CREATE TABLE reservations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    service_provider_id BIGINT NOT NULL,
    reservation_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(1000) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reservations_provider FOREIGN KEY (service_provider_id) REFERENCES service_providers (id),
    CONSTRAINT chk_reservation_times CHECK (start_time < end_time),
    CONSTRAINT chk_reservation_status CHECK (status IN ('PENDING','CONFIRMED','CANCELLED','REJECTED','COMPLETED'))
) ENGINE=InnoDB;

CREATE TABLE reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    service_provider_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    rating TINYINT NOT NULL,
    comment VARCHAR(2000) NULL,
    moderated BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_reviews_reservation UNIQUE (reservation_id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reviews_provider FOREIGN KEY (service_provider_id) REFERENCES service_providers (id),
    CONSTRAINT fk_reviews_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE INDEX idx_users_role_enabled ON users (role, enabled);
CREATE INDEX idx_categories_active_name ON categories (active, name);
CREATE INDEX idx_providers_category_status ON service_providers (category_id, verification_status);
CREATE INDEX idx_providers_city_status ON service_providers (city, verification_status);
CREATE INDEX idx_providers_rating ON service_providers (average_rating);
CREATE INDEX idx_providers_coordinates ON service_providers (latitude, longitude);
CREATE INDEX idx_reservations_provider_date_status ON reservations (service_provider_id, reservation_date, status);
CREATE INDEX idx_reservations_user_date ON reservations (user_id, reservation_date);
CREATE INDEX idx_reservations_slot ON reservations (service_provider_id, reservation_date, start_time, end_time);
CREATE INDEX idx_reviews_provider_created ON reviews (service_provider_id, created_at);
CREATE INDEX idx_reviews_user ON reviews (user_id);
