package apicalendario.core.dtos;

import java.time.LocalDate;

/**
 * Festivo tal como lo entrega la API Festivos: { "festivo": "Año nuevo", "fecha": "2023-01-01" }.
 */
public record FestivoDto(String festivo, LocalDate fecha) {
}
