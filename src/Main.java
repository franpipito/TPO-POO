import view.AppContext;
import view.VentanaLogin;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación. Crea el contexto compartido (datos en memoria)
 * y abre la pantalla de login; recién tras autenticarse se abre la ventana principal.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppContext ctx = new AppContext();
            new VentanaLogin(ctx).setVisible(true);
        });
    }
}
