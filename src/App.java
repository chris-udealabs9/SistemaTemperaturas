import controladores.TemperaturasControlador;
import vistas.TemperaturasVista;

import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TemperaturasVista vista = new TemperaturasVista();
            new TemperaturasControlador(vista);
            vista.setVisible(true);
        });
    }
}
