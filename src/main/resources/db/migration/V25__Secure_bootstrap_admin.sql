INSERT INTO webge.role(role)
SELECT 'ROLE_USER'
WHERE NOT EXISTS (
    SELECT 1 FROM webge.role WHERE role = 'ROLE_USER'
);

DELETE FROM webge.users_roles
WHERE role_name = 'ROLE_ADMIN'
  AND username <> 'admin';

INSERT INTO webge.users_roles(user_id, username, role_id, role_name, email)
SELECT u.user_id, u.username, r.role_id, r.role, u.email
FROM webge.user u
JOIN webge.role r ON r.role = 'ROLE_USER'
WHERE NOT EXISTS (
    SELECT 1
    FROM webge.users_roles ur
    WHERE ur.user_id = u.user_id
      AND ur.role_name = 'ROLE_USER'
);

UPDATE webge.user
SET enabled = false
WHERE username = 'admin'
  AND password = '$2a$11$hwnvHO4u./7PBsClAXe1fuPIat1sqitn7EYvti9ajWpONIqx7pYB2';
