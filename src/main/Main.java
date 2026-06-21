import view.VentanaPrincipal;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Abrimos la ventana principal de la aplicacion
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
