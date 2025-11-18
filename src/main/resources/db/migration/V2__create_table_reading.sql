CREATE TABLE reading (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    date DATETIME2,
    description VARCHAR(500),
    humor VARCHAR(255),

    person_id BIGINT,
    CONSTRAINT fk_reading_person FOREIGN KEY (person_id) REFERENCES person(id)
);
