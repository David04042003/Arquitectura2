-- Script de población de la base de datos "calendario".
-- Mismo contenido que API-Calendario/src/main/resources/data.sql
-- (el usuario inicial "admin" lo crea la API al arrancar, con la clave cifrada en BCrypt)

\c calendario

/* Catálogo de tipos de día (ids fijos que usa la API) */
INSERT INTO tipo (id, tipo) VALUES
    (1, 'Día laboral'),
    (2, 'Fin de Semana'),
    (3, 'Día festivo')
ON CONFLICT (id) DO NOTHING;
