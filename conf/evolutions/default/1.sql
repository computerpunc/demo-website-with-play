--  The MIT License (MIT)
--  Copyright (c) 2012 Ram Hardy & Elad Hemar
--
--    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
--    documentation files (the "Software"), to deal in the Software without restriction, including without limitation
--    the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
--    and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
--
--    The above copyright notice and this permission notice shall be included in all copies or substantial portions
--    of the Software.
--
--    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
--    TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
--    THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
--    CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
--    IN THE SOFTWARE.

# --- !Ups

CREATE TABLE users (
    id serial PRIMARY KEY,
    name varchar(255) NOT NULL,
    email varchar(255) NOT NULL UNIQUE,
    password varchar(255) NOT NULL,
    access_token varchar(63) NOT NULL,
    reset_password_token varchar(63)
);

INSERT INTO users (name, email, password, access_token) VALUES
    ('Ram Hardy', 'ram@example.com', '$s0$e0801$UTcluMO95WiIDOWWvvdgnw==$gnxcEj/kFk1LrfT8rm3OtD8zv1TaisZksvsbsh6R6kk=', 'at12345'),
    ('Elad Hemar', 'elad@example.com', 'h54321', 'at54321');

CREATE TABLE apps (
    id bigint PRIMARY KEY,
    user_id bigint,
    access_token character varying(63) NOT NULL,
    CONSTRAINT user_fkey FOREIGN KEY (user_id)
      REFERENCES users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);

INSERT INTO apps (id, user_id, access_token) VALUES
    (1, 1, '2d295da7d88d4ef7b1d2e9be1bf80c37'),
    (2, 2, '2d295da7d88d4ef7b1d2e9be1bf80c37'),
    (3, 1, '2d295da7d88d4ef7b1d2e9be1bf80c37'),
    (4, 2, '2d295da7d88d4ef7b1d2e9be1bf80c37');

# --- !Downs

DROP TABLE users CASCADE;
DROP TABLE apps CASCADE;