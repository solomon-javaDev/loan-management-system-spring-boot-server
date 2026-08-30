-- Seed basic user and employee records.
-- Passwords are intentionally plain text here; they are encoded at runtime in SeedDataConfig.

INSERT INTO users (id, username, password, email, role)
SELECT 1, 'admin', 'admin123', 'admin@lms.com', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO employees (id, username, password, email, telephone, role, active, first_name, last_name, salary)
SELECT 1, 'Solomon Twist', '123', 'solomon@twist.com', '0789847372', 'FIELD_OFFICER', true, 'Solomon', 'Twist', 20000
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE email = 'solomon@twist.com');

INSERT INTO employees (id, username, password, email, telephone, role, active, first_name, last_name, salary)
SELECT 2, 'Nisha Twist', '123', 'nisha@twist.com', '085958332', 'FIELD_OFFICER', true, 'Nisha', 'Twist', 20300
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE email = 'nisha@twist.com');

INSERT INTO employees (id, username, password, email, telephone, role, active, first_name, last_name, salary)
SELECT 3, 'Hansa Gans', '123', 'hans@gans.com', '0758437722', 'FIELD_OFFICER', true, 'Hansa', 'Gans', 20000
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE email = 'hans@gans.com');

INSERT INTO employees (id, username, password, email, telephone, role, active, first_name, last_name, salary)
SELECT 4, 'Hasifa Muus', '123', 'has@has.com', '089483722', 'FIELD_OFFICER', true, 'Hasifa', 'Muus', 12000
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE email = 'has@has.com');

INSERT INTO employees (id, username, password, email, telephone, role, active, first_name, last_name, salary)
SELECT 5, 'Wendy Glav', '123', 'wens@wen.com', '093728923', 'FIELD_OFFICER', true, 'Wendy', 'Glav', 20000
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE email = 'wens@wen.com');
