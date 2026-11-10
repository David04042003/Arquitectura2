package apicalendario.core.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Catálogo de tipos de día (tabla "tipo"): Día laboral, Fin de Semana y Día festivo.
 * Los ids son fijos y se cargan en data.sql.
 */
@Entity
@Table(name = "tipo")
public class Tipo {

    public static final int DIA_LABORAL = 1;
    public static final int FIN_DE_SEMANA = 2;
    public static final int DIA_FESTIVO = 3;

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "tipo", length = 50, nullable = false, unique = true)
    private String tipo;

    public Tipo() {
    }

    public Tipo(Integer id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
