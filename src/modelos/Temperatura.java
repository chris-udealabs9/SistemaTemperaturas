package modelos;

import java.time.LocalDate;
import java.util.Objects;

/** Representa el registro de temperatura de una ciudad en una fecha. */
public final class Temperatura {

    private final String ciudad;
    private final LocalDate fecha;
    private final double temperatura;

    public Temperatura(String ciudad, LocalDate fecha, double temperatura) {
        this.ciudad = Objects.requireNonNull(ciudad, "La ciudad es obligatoria").trim();
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
        if (this.ciudad.isEmpty()) {
            throw new IllegalArgumentException("La ciudad no puede estar vacía");
        }
        if (!Double.isFinite(temperatura)) {
            throw new IllegalArgumentException("La temperatura debe ser un número válido");
        }
        this.temperatura = temperatura;
    }

    public String getCiudad() {
        return ciudad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getTemperatura() {
        return temperatura;
    }
}
