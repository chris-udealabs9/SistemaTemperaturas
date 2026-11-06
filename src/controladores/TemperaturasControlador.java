package controladores;

import modelos.Temperatura;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import servicios.TemperaturasServicio;
import vistas.TemperaturasVista;

import java.awt.Dimension;
import java.io.IOException;
import java.text.DecimalFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Coordina los eventos de la vista y las operaciones del servicio. */
public class TemperaturasControlador {

    private final TemperaturasVista vista;
    private final TemperaturasServicio servicio;
    private List<Temperatura> temperaturas;

    public TemperaturasControlador(TemperaturasVista vista) {
        this.vista = vista;
        this.servicio = new TemperaturasServicio();

        this.vista.setGraficarClick(evento -> graficarPromedios());
        this.vista.setCalcularEstadisticasClick(evento -> calcularEstadisticas());
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            Path archivo = localizarArchivoDatos();
            temperaturas = servicio.cargarDatos(archivo);

            LocalDate fechaInicial = temperaturas.stream()
                    .map(Temperatura::getFecha)
                    .min(Comparator.naturalOrder())
                    .orElseThrow();
            LocalDate fechaFinal = temperaturas.stream()
                    .map(Temperatura::getFecha)
                    .max(Comparator.naturalOrder())
                    .orElseThrow();

            vista.establecerFechasIniciales(fechaInicial, fechaFinal);
            vista.mostrarMensajeEstado(String.format(
                    "Datos cargados: %d registros | %d ciudades | Período: %s a %s",
                    temperaturas.size(), servicio.obtenerCiudades(temperaturas).size(), fechaInicial, fechaFinal));
        } catch (IOException | RuntimeException excepcion) {
            temperaturas = List.of();
            vista.mostrarMensajeEstado("No fue posible cargar el archivo de temperaturas.");
            vista.mostrarError("No se pudieron cargar los datos.\n" + excepcion.getMessage()
                    + "\n\nVerifica que exista src/datos/Temperaturas.csv.");
        }
    }

    private Path localizarArchivoDatos() throws IOException {
        Path directorio = Path.of(System.getProperty("user.dir"));
        List<Path> posiblesRutas = List.of(
                directorio.resolve("src/datos/Temperaturas.csv"),
                directorio.resolve("datos/Temperaturas.csv"),
                directorio.resolve("Temperaturas.csv")
        );

        return posiblesRutas.stream()
                .filter(Files::isRegularFile)
                .findFirst()
                .orElseThrow(() -> new IOException("No se encontró Temperaturas.csv en las rutas del proyecto."));
    }

    private void graficarPromedios() {
        if (temperaturas.isEmpty()) {
            vista.mostrarAdvertencia("No hay datos cargados para generar la gráfica.");
            return;
        }

        try {
            LocalDate desde = vista.getFechaDesde();
            LocalDate hasta = vista.getFechaHasta();
            Map<String, Double> promedios = servicio.calcularPromediosPorCiudad(temperaturas, desde, hasta);

            if (promedios.isEmpty()) {
                vista.mostrarAdvertencia("No hay registros para el rango de fechas seleccionado.");
                return;
            }

            DefaultCategoryDataset conjuntoDatos = new DefaultCategoryDataset();
            promedios.forEach((ciudad, promedio) ->
                    conjuntoDatos.addValue(promedio, "Temperatura promedio", ciudad));

            JFreeChart grafica = ChartFactory.createBarChart(
                    "Promedio de temperatura por ciudad",
                    "Ciudad",
                    "Temperatura promedio (°C)",
                    conjuntoDatos,
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    false
            );

            // Fija el eje de temperatura en intervalos exactos de 1 °C.
            CategoryPlot graficaBarras = grafica.getCategoryPlot();
            NumberAxis ejeTemperatura = (NumberAxis) graficaBarras.getRangeAxis();
            ejeTemperatura.setAutoTickUnitSelection(false);
            ejeTemperatura.setTickUnit(new NumberTickUnit(1.0, new DecimalFormat("0")));

            ChartPanel panelGrafica = new ChartPanel(grafica);
            panelGrafica.setPreferredSize(new Dimension(820, 430));
            vista.mostrarGrafica(panelGrafica);
            vista.activarPestana(0);
        } catch (IllegalArgumentException excepcion) {
            vista.mostrarAdvertencia(excepcion.getMessage());
        }
    }

    private void calcularEstadisticas() {
        if (temperaturas.isEmpty()) {
            vista.mostrarAdvertencia("No hay datos cargados para calcular las estadísticas.");
            return;
        }

        LocalDate fecha = vista.getFechaConsulta();
        Optional<Temperatura> masCalurosa = servicio.obtenerMasCalurosa(temperaturas, fecha);
        Optional<Temperatura> menosCalurosa = servicio.obtenerMenosCalurosa(temperaturas, fecha);

        if (masCalurosa.isEmpty() || menosCalurosa.isEmpty()) {
            vista.mostrarAdvertencia("No hay registros disponibles para la fecha " + fecha + ".");
            return;
        }

        vista.mostrarEstadisticas(fecha, masCalurosa.get(), menosCalurosa.get());
        vista.activarPestana(1);
    }
}
