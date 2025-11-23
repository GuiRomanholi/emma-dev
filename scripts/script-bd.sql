CREATE TABLE person (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE reading (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    date DATETIME2,
    description NVARCHAR(MAX),
    humor NVARCHAR(255),

    person_id BIGINT,
    CONSTRAINT fk_reading_person FOREIGN KEY (person_id) REFERENCES person(id)
);

CREATE TABLE review (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    description NVARCHAR(MAX),

    reading_id BIGINT,
    CONSTRAINT fk_review_reading FOREIGN KEY (reading_id) REFERENCES reading(id)
);
