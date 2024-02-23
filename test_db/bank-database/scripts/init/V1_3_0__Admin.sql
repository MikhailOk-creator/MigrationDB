CREATE TABLE admin_t (
                         id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         admin_number VARCHAR(255) NOT NULL UNIQUE,
                         user_id INT NOT NULL,
                         FOREIGN KEY (user_id) REFERENCES user_t (id)
);

INSERT INTO admin_t (admin_number, user_id) VALUES ('A_0001', 1);