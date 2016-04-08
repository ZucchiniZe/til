-- :name create-tils-table :! :raw
-- :doc create the table for the tils
CREATE TABLE tils (
       id serial PRIMARY KEY,
       title varchar(100),
       body varchar(2000),
       tags text,
       user_id integer,
       created_at timestamp
);

-- :name create :! :n
-- :doc insert one til into the database
INSERT INTO tils (title, body, tags, user_id, created_at)
VALUES (:title, :body, :tags, :user_id, current_timestamp)

-- :name get-by-id :? :1
-- :doc gets a til by it's id
SELECT * FROM tils WHERE id = :id;

-- :name get-20-recent :? :*
-- :doc gets all of the tils
SELECT * FROM tils ORDER BY created_at DESC LIMIT 20;

-- :name all :? :*
-- :doc gets all of the tils
SELECT * FROM tils;

-- :name get-count :? :*
-- :doc gets the total count of tils
SELECT count(*) FROM tils;
