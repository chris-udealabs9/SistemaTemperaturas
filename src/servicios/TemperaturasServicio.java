package servicios;

import modelos.Temperatura;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Carga y procesa registros de temperatura mediante operaciones funcionales. */
public class TemperaturasServicio {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Lee el CSV línea por línea y transforma cada fila en un objeto Temperatura.
     * La primera línea se considera encabezado.
     */
    public List<Temperatura> cargarDatos(Path archivo) throws IOException {
        try (Stream<String> lineas = Files.lines(archivo, StandardCharsets.UTF_8)) {
            List<Temperatura> temperaturas = lineas
                    .skip(1)
                    .filter(linea -> !linea.isBlank())
                    .map(this::convertirLinea)
                    .collect(Collectors.toList());

            if (temperaturas.isEmpty()) {
                throw new IllegalArgumentException("El archivo CSV no contiene registros de temperatura.");
            }
            return temperaturas;
        }
    }

    private Temperatura convertirLinea(String linea) {
        String[] campos = linea.split(",", -1);
        if (campos.length != 3) {
            throw new IllegalArgumentException("La fila no tiene tres columnas: " + linea);
        }

        String ciudad = campos[0].trim();
        if (ciudad.isEmpty()) {
            throw new IllegalArgumentException("Se encontró una fila sin ciudad: " + linea);
        }

        try {
            LocalDate fecha = LocalDate.parse(campos[1].trim(), FORMATO_FECHA);
            double temperatura = Double.parseDouble(campos[2].trim());
            return new Temperatura(ciudad, fecha, temperatura);
        } catch (RuntimeException excepcion) {
            throw new IllegalArgumentException("No se pudo interpretar la fila del CSV: " + linea, excepcion);
        }
    }

    /** Obtiene las ciudades sin duplicados y en orden alfabético. */
    public List<String> obtenerCiudades(List<Temperatura> temperaturas) {
        return temperaturas.stream()
                .map(Temperatura::getCiudad)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Calcula el promedio de temperatura por ciudad para un rango inclusivo de fechas.
     * averagingDouble es un collector reductor/estadístico aplicado sobre el Stream.
     * groupingBy recibe una función (referencia a método) y averagingDouble recibe
     * otra función para extraer el valor numérico; ambas se pasan como parámetros.
     */
    public Map<String, Double> calcularPromediosPorCiudad(
            List<Temperatura> temperaturas, LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);

        return temperaturas.stream()
                .filter(registro -> !registro.getFecha().isBefore(desde)
                        && !registro.getFecha().isAfter(hasta))
                .collect(Collectors.groupingBy(
                        Temperatura::getCiudad,
                        TreeMap::new,
                        Collectors.averagingDouble(Temperatura::getTemperatura)
                ));
    }

    /** Busca el registro de mayor temperatura para una fecha específica. */
    public Optional<Temperatura> obtenerMasCalurosa(List<Temperatura> temperaturas, LocalDate fecha) {
        return temperaturas.stream()
                .filter(registro -> registro.getFecha().equals(fecha))
                .max(Comparator.comparingDouble(Temperatura::getTemperatura));
    }

    /** Busca el registro de menor temperatura para una fecha específica. */
    public Optional<Temperatura> obtenerMenosCalurosa(List<Temperatura> temperaturas, LocalDate fecha) {
        return temperaturas.stream()
                .filter(registro -> registro.getFecha().equals(fecha))
                .min(Comparator.comparingDouble(Temperatura::getTemperatura));
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debes seleccionar las dos fechas del rango.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final.");
        }
    }
}
