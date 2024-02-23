CREATE TABLE manager_t (
                           id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           manager_number VARCHAR(255) NOT NULL UNIQUE,
                           user_id INT NOT NULL,
                           FOREIGN KEY (user_id) REFERENCES user_t (id)
);

ALTER TABLE client_t ADD COLUMN manager_id INT;
ALTER TABLE client_t ADD FOREIGN KEY (manager_id) REFERENCES manager_t (id);

CREATE TABLE transaction_t (
                               id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               transaction_number VARCHAR(255) NOT NULL UNIQUE,
                               amount DECIMAL(19, 2) NOT NULL,
                               transaction_date TIMESTAMP NOT NULL,
                               bank_account_id INT NOT NULL,
                               client_id INT NOT NULL,
                               manager_id INT NOT NULL,
                               bank_account_id_to INT NOT NULL,
                               FOREIGN KEY (bank_account_id) REFERENCES bank_account_t (id),
                               FOREIGN KEY (client_id) REFERENCES client_t (id),
                               FOREIGN KEY (manager_id) REFERENCES manager_t (id),
                               FOREIGN KEY (bank_account_id_to) REFERENCES bank_account_t (id)
);