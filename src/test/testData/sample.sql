-- Full SQL syntax sample.
-- Covers schemas, tables, constraints, indexes, views, sequences, DML,
-- joins, subqueries, CTEs, window functions, transactions, procedures,
-- functions, triggers and data types.

CREATE SCHEMA IF NOT EXISTS sample;

CREATE TABLE sample.departments (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(64) NOT NULL UNIQUE,
    budget      NUMERIC(12, 2) DEFAULT 0.00 CHECK (budget >= 0),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sample.employees (
    id             BIGSERIAL PRIMARY KEY,
    department_id  BIGINT NOT NULL REFERENCES sample.departments (id) ON DELETE CASCADE,
    first_name     VARCHAR(64) NOT NULL,
    last_name      VARCHAR(64) NOT NULL,
    email          VARCHAR(255) NOT NULL UNIQUE,
    salary         DECIMAL(10, 2) NOT NULL DEFAULT 0.0,
    status         VARCHAR(16) NOT NULL DEFAULT 'active',
    hired_on       DATE NOT NULL,
    tags           TEXT[],
    CONSTRAINT employees_salary_positive CHECK (salary > 0),
    CONSTRAINT employees_status_valid CHECK (status IN ('active', 'inactive', 'left'))
);

CREATE INDEX employees_department_idx ON sample.employees (department_id);
CREATE UNIQUE INDEX employees_full_name_idx ON sample.employees (last_name, first_name);

CREATE SEQUENCE sample.audit_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE VIEW sample.employee_summary AS
SELECT e.department_id,
       d.name AS department_name,
       COUNT(e.id) AS headcount,
       AVG(e.salary) AS average_salary,
       MAX(e.salary) AS top_salary
FROM sample.employees e
JOIN sample.departments d ON d.id = e.department_id
GROUP BY e.department_id, d.name;

INSERT INTO sample.departments (name, budget)
VALUES ('Engineering', 1000000),
       ('Design', 250000.5),
       ('Operations', 0);

INSERT INTO sample.employees (department_id, first_name, last_name, email, salary, hired_on, tags)
VALUES ((SELECT id FROM sample.departments WHERE name = 'Engineering'),
        'Alice', 'Smith', 'alice@example.com', 95000.00, DATE '2021-03-01', ARRAY['senior', 'backend']),
       (1, 'Bob', 'Jones', 'bob@example.com', 75000.00, DATE '2022-07-15', NULL),
       (2, 'Carol', 'Lee', 'carol@example.com', 68000.00, DATE '2023-01-09', ARRAY['design']);

UPDATE sample.employees
SET salary = salary * 1.05,
    status = 'active'
WHERE hired_on < DATE '2022-01-01'
  AND salary BETWEEN 50000 AND 150000;

DELETE FROM sample.employees
WHERE status = 'left'
  AND NOT EXISTS (SELECT 1 FROM sample.departments d WHERE d.id = sample.employees.department_id);

WITH ranked AS (
    SELECT id,
           department_id,
           first_name || ' ' || last_name AS full_name,
           salary,
           ROW_NUMBER() OVER (PARTITION BY department_id ORDER BY salary DESC) AS rank_in_department,
           SUM(salary) OVER (PARTITION BY department_id) AS department_total
    FROM sample.employees
    WHERE status = 'active'
)
SELECT r.full_name,
       r.salary,
       r.rank_in_department,
       r.salary / NULLIF(r.department_total, 0) AS share_of_department
FROM ranked r
WHERE r.rank_in_department <= 3
ORDER BY r.department_id, r.rank_in_department
LIMIT 10 OFFSET 0;

SELECT d.name,
       COUNT(e.id) AS employees,
       COALESCE(SUM(e.salary), 0) AS payroll,
       CASE
           WHEN COUNT(e.id) = 0 THEN 'empty'
           WHEN COUNT(e.id) > 10 THEN 'large'
           ELSE 'small'
       END AS size
FROM sample.departments d
LEFT JOIN sample.employees e
       ON e.department_id = d.id
      AND e.status = 'active'
WHERE d.budget > (SELECT AVG(budget) FROM sample.departments)
GROUP BY d.name
HAVING COUNT(e.id) >= 0
ORDER BY payroll DESC;

SELECT first_name
FROM sample.employees
WHERE department_id IN (SELECT id FROM sample.departments WHERE budget > 0)
  AND salary > ALL (SELECT AVG(salary) FROM sample.employees GROUP BY department_id)
INTERSECT
SELECT first_name FROM sample.employees WHERE status = 'active'
EXCEPT
SELECT 'Nobody';

BEGIN;

SAVEPOINT before_bonus;

UPDATE sample.employees SET salary = salary + 1000 WHERE department_id = 1;

ROLLBACK TO SAVEPOINT before_bonus;

COMMIT;

CREATE OR REPLACE FUNCTION sample.raise_salary(p_employee_id BIGINT, p_percent NUMERIC)
RETURNS NUMERIC
LANGUAGE plpgsql
AS $$
DECLARE
    v_new_salary NUMERIC;
BEGIN
    UPDATE sample.employees
    SET salary = salary * (1 + p_percent / 100)
    WHERE id = p_employee_id
    RETURNING salary INTO v_new_salary;

    IF v_new_salary IS NULL THEN
        RAISE EXCEPTION 'employee % not found', p_employee_id;
    END IF;

    RETURN v_new_salary;
END;
$$;

CREATE OR REPLACE PROCEDURE sample.audit_employee(p_employee_id BIGINT)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO sample.employees (department_id, first_name, last_name, email, salary, hired_on)
    SELECT department_id, first_name, last_name, email, salary, hired_on
    FROM sample.employees
    WHERE id = p_employee_id;
END;
$$;

CREATE OR REPLACE TRIGGER employees_audit
AFTER UPDATE OF salary ON sample.employees
FOR EACH ROW
EXECUTE FUNCTION sample.raise_salary(NEW.id, 0);

DROP TABLE IF EXISTS sample.employees CASCADE;
