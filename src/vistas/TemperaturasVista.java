package vistas;

import modelos.Temperatura;
import org.jfree.chart.ChartPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Calendar;

/** Interfaz gráfica de la aplicación de análisis de temperaturas. */
public class TemperaturasVista extends JFrame {

    private final JSpinner spFechaDesde;
    private final JSpinner spFechaHasta;
    private final JSpinner spFechaConsulta;
    private final JTabbedPane pestanas;
    private final JPanel panelGrafica;
    private final JTextArea areaEstadisticas;
    private final JLabel lblEstado;
    private final JButton btnGraficar;
    private final JButton btnEstadisticas;

    public TemperaturasVista() {
        setTitle("Análisis de temperaturas por ciudad");
        setSize(900, 580);
        setMinimumSize(new Dimension(760, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JToolBar barraHerramientas = new JToolBar();
        barraHerramientas.setFloatable(false);

        btnGraficar = new JButton("Promedios por ciudad");
        btnGraficar.setIcon(cargarIcono("/iconos/Grafica.png"));
        btnGraficar.setToolTipText("Graficar el promedio de temperatura de cada ciudad en el rango seleccionado");
        barraHerramientas.add(btnGraficar);

        btnEstadisticas = new JButton("Más y menos calurosa");
        btnEstadisticas.setIcon(cargarIcono("/iconos/Datos.png"));
        btnEstadisticas.setToolTipText("Consultar las ciudades más y menos calurosas de una fecha");
        barraHerramientas.add(btnEstadisticas);

        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelControles.setBorder(BorderFactory.createTitledBorder("Parámetros de consulta"));

        spFechaDesde = crearSelectorFecha();
        spFechaHasta = crearSelectorFecha();
        spFechaConsulta = crearSelectorFecha();

        panelControles.add(new JLabel("Desde:"));
        panelControles.add(spFechaDesde);
        panelControles.add(new JLabel("Hasta:"));
        panelControles.add(spFechaHasta);
        panelControles.add(Box.createHorizontalStrut(12));
        panelControles.add(new JLabel("Fecha específica:"));
        panelControles.add(spFechaConsulta);

        panelGrafica = new JPanel(new BorderLayout());
        panelGrafica.add(new JLabel("Selecciona un rango de fechas y pulsa «Promedios por ciudad».",
                SwingConstants.CENTER), BorderLayout.CENTER);

        areaEstadisticas = new JTextArea();
        areaEstadisticas.setEditable(false);
        areaEstadisticas.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        areaEstadisticas.setLineWrap(true);
        areaEstadisticas.setWrapStyleWord(true);
        areaEstadisticas.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        areaEstadisticas.setText("Selecciona una fecha y pulsa «Más y menos calurosa».");

        pestanas = new JTabbedPane();
        pestanas.addTab("Gráfica de promedios", panelGrafica);
        pestanas.addTab("Estadísticas por fecha", areaEstadisticas);

        lblEstado = new JLabel("Cargando datos...");
        lblEstado.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 5));
        panelPrincipal.add(panelControles, BorderLayout.NORTH);
        panelPrincipal.add(pestanas, BorderLayout.CENTER);
        panelPrincipal.add(lblEstado, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(barraHerramientas, BorderLayout.NORTH);
        getContentPane().add(panelPrincipal, BorderLayout.CENTER);
    }

    private JSpinner crearSelectorFecha() {
        SpinnerModel modelo = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner selector = new JSpinner(modelo);
        selector.setEditor(new JSpinner.DateEditor(selector, "dd/MM/yyyy"));
        selector.setPreferredSize(new Dimension(112, 28));
        return selector;
    }

    private ImageIcon cargarIcono(String ruta) {
        var recurso = getClass().getResource(ruta);
        return recurso == null ? null : new ImageIcon(recurso);
    }

    private LocalDate obtenerFecha(JSpinner selector) {
        Date fecha = (Date) selector.getValue();
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void establecerFecha(JSpinner selector, LocalDate fecha) {
        Date valor = Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
        selector.setValue(valor);
    }

    public LocalDate getFechaDesde() {
        return obtenerFecha(spFechaDesde);
    }

    public LocalDate getFechaHasta() {
        return obtenerFecha(spFechaHasta);
    }

    public LocalDate getFechaConsulta() {
        return obtenerFecha(spFechaConsulta);
    }

    public void establecerFechasIniciales(LocalDate desde, LocalDate hasta) {
        establecerFecha(spFechaDesde, desde);
        establecerFecha(spFechaHasta, hasta);
        establecerFecha(spFechaConsulta, desde);
    }

    public void setGraficarClick(ActionListener evento) {
        btnGraficar.addActionListener(evento);
    }

    public void setCalcularEstadisticasClick(ActionListener evento) {
        btnEstadisticas.addActionListener(evento);
    }

    public void mostrarGrafica(ChartPanel grafica) {
        panelGrafica.removeAll();
        panelGrafica.add(grafica, BorderLayout.CENTER);
        panelGrafica.revalidate();
        panelGrafica.repaint();
    }

    public void mostrarEstadisticas(LocalDate fecha,
                                    Temperatura masCalurosa,
                                    Temperatura menosCalurosa) {
        StringBuilder resultado = new StringBuilder();
        resultado.append("RESULTADOS PARA EL ").append(fecha).append("\n\n");
        resultado.append("CIUDAD MÁS CALUROSA\n");
        resultado.append("Ciudad: ").append(masCalurosa.getCiudad()).append("\n");
        resultado.append(String.format("Temperatura: %.1f °C\n\n", masCalurosa.getTemperatura()));
        resultado.append("CIUDAD MENOS CALUROSA\n");
        resultado.append("Ciudad: ").append(menosCalurosa.getCiudad()).append("\n");
        resultado.append(String.format("Temperatura: %.1f °C", menosCalurosa.getTemperatura()));
        areaEstadisticas.setText(resultado.toString());
        areaEstadisticas.setCaretPosition(0);
    }

    public void mostrarMensajeEstado(String mensaje) {
        lblEstado.setText(mensaje);
    }

    public void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Consulta de temperaturas", JOptionPane.WARNING_MESSAGE);
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void activarPestana(int posicion) {
        if (posicion >= 0 && posicion < pestanas.getTabCount()) {
            pestanas.setSelectedIndex(posicion);
        }
    }
}
