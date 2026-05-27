-- ================================================================
-- MIGRACIÓN: Contraseñas a BCrypt
-- Ejecutar en Neon ANTES de hacer deploy del nuevo backend
-- ================================================================
UPDATE usuarios SET password = '$2b$10$/yu1O.SDQXxmyZCw5eXxcOg1nkTdQEZYfwEuGtDc6MjdEiJWr6Oq.' WHERE id = 1;
UPDATE usuarios SET password = '$2b$10$r5nHgV7W3pDyewkJNjGL6e07e/Utzojyti4Jft7zeIVPsZ4ou/ufa' WHERE id = 2;
UPDATE usuarios SET password = '$2b$10$XPH8JKw9uMNMQoAmRbdqIOizE6bpOyTL1kB6o8/wE9ueHPZmo.v6W' WHERE id = 4;
-- ================================================================
