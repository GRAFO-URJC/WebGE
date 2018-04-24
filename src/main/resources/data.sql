/*INSERT INTO user (id, username, enabled, first_name, last_name, email, password) VALUES (1, 'ninouser', true, 'Alberto', 'Power', 'alberto.nii@hotmail.com', 'prueba1');
INSERT INTO user (id, username, enabled, first_name, last_name, email, password) VALUES (2, 'ninoadmin', true,'Nino', 'Admin', 'ninoadmin@hotmail.com', 'prueba1');
INSERT INTO user (id, username, enabled, first_name, last_name, email, password) VALUES (3, 'ninomanager', true,'Nino', 'Manager', 'ninomanager@hotmail.com', 'prueba1');

INSERT INTO role (id, role) VALUES (1, 'ROLE_ADMIN');
INSERT INTO role (id, role) VALUES (2, 'ROLE_MANAGER');
INSERT INTO role (id, role) VALUES (3, 'ROLE_USER');

INSERT INTO users_roles (user_id, role_id, username, email, role_name) VALUES (1, 3, 'ninouser', 'alberto.nii@hotmail.com', 'ROLE_USER');

INSERT INTO users_roles (user_id, role_id, username, email, role_name) VALUES (2, 1, 'ninoadmin', 'ninoadmin@hotmail.com', 'ROLE_ADMIN');

INSERT INTO users_roles (user_id, role_id, username, email, role_name) VALUES (3, 2, 'ninomanager', 'ninomanager@hotmail.com', 'ROLE_MANAGER');*/