-- Catálogo de tipos de día (ids fijos que usa la clase Tipo)
INSERT INTO tipo (id, tipo) VALUES
    (1, 'Día laboral'),
    (2, 'Fin de Semana'),
    (3, 'Día festivo')
ON CONFLICT (id) DO NOTHING;
