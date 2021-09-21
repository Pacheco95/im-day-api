INSERT INTO users(name)
VALUES ('Michael'),
       ('Ana'),
       ('Carlos')
RETURNING *;
