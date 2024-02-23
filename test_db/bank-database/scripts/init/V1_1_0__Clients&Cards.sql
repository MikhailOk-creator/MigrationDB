CREATE TABLE client_t (
                          id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          client_number VARCHAR(255) NOT NULL UNIQUE,
                          passport_number VARCHAR(255) NOT NULL UNIQUE,
                          user_id INT NOT NULL,
                          FOREIGN KEY (user_id) REFERENCES user_t (id)
);

CREATE TABLE bank_account_t (
                                id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                account_number VARCHAR(255) NOT NULL UNIQUE,
                                balance DECIMAL(19, 2) NOT NULL,
                                active BOOLEAN NOT NULL
);

CREATE TABLE client_bank_account_t (
                                       client_id INT NOT NULL,
                                       bank_account_id INT NOT NULL,
                                       PRIMARY KEY (client_id, bank_account_id),
                                       FOREIGN KEY (client_id) REFERENCES client_t (id),
                                       FOREIGN KEY (bank_account_id) REFERENCES bank_account_t (id)
);

CREATE TABLE debit_card_t (
                              id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              card_number VARCHAR(255) NOT NULL UNIQUE,
                              cvv VARCHAR(255) NOT NULL,
                              expiration_date DATE NOT NULL,
                              active BOOLEAN NOT NULL,
                              balance DECIMAL(19, 2) NOT NULL
);

CREATE TABLE bank_account_debit_card_t (
                                           bank_account_id INT NOT NULL,
                                           debit_card_id INT NOT NULL,
                                           PRIMARY KEY (bank_account_id, debit_card_id),
                                           FOREIGN KEY (bank_account_id) REFERENCES bank_account_t (id),
                                           FOREIGN KEY (debit_card_id) REFERENCES debit_card_t (id)
);