CREATE TABLE user_t (
                        id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        first_name VARCHAR(255) NOT NULL,
                        last_name VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL UNIQUE,
                        telephone_number VARCHAR(255) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        active BOOLEAN NOT NULL
);

CREATE TABLE group_t (
                         id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         code VARCHAR(255) NOT NULL UNIQUE,
                         description_of_group VARCHAR(255) NOT NULL
);

CREATE TABLE user_group_t (
                              user_id INT NOT NULL,
                              group_id INT NOT NULL,
                              PRIMARY KEY (user_id, group_id),
                              FOREIGN KEY (user_id) REFERENCES user_t (id),
                              FOREIGN KEY (group_id) REFERENCES group_t (id)
);

INSERT INTO group_t (code, description_of_group) VALUES ('admin', 'Administrator of the bank system');
INSERT INTO group_t (code, description_of_group) VALUES ('client', 'Client of the bank');

-- Password: 123456789
INSERT INTO user_t (first_name, last_name, email, telephone_number, password, active) VALUES
    ('Admin', 'Admin', 'admin@localhost', '+7(123)456-78-90', '$2y$10$YMLm7GGQZPtq.fpe0d6pMOIBiJToAqjEL1fRba1d5EWFDzUhvUEH.', TRUE);
INSERT INTO user_group_t (user_id, group_id) VALUES (1, 1);

-- Password: 1234
INSERT INTO user_t (first_name, last_name, email, telephone_number, password, active) VALUES
    ('User', 'User', 'user@localhost', '+7(321)456-78-90', '$2y$10$kBRdheQ.nQbcQgxfYn5H5.Eg9oE7MGyCgjwwXuK/lItaomZ6ZXE.u', TRUE);
INSERT INTO user_group_t (user_id, group_id) VALUES (2, 2);
