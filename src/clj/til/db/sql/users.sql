-- :name create-users-table :! :raw
-- :doc create the users table
CREATE TABLE users (
       id serial PRIMARY KEY,
       username varchar(255),
       email varchar(255),
       password varchar(255)
);

-- :name create-sql :! :n
-- :doc creates a new user
-- :require [buddy.hashers :as hashers]
INSERT INTO users (username, email, password)
VALUES (:username, :email, :password);

-- :name get-by-name :? :1
-- :doc gets a user by their username
SELECT * FROM users WHERE username = :username;

-- :name get-by-id :? :1
-- :doc gets a user by their id
SELECT * FROM users WHERE id = :id;

-- :name change-password :! :1
-- :doc changes the users password
UPDATE users
SET password = :password
WHERE id = :id;
