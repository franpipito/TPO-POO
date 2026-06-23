package view;

import model.Usuario;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla de inicio de sesión. Es lo primero que ve el usuario al abrir la app.
 * Valida las credenciales contra el {@code UsuarioController}; si son correctas,
 * guarda el usuario logueado en el {@link AppContext} y abre la ventana principal.
 *
 * <p>Comparte el MISMO AppContext con el resto de la aplicación, de modo que los
 * datos en memoria (incluidos los usuarios creados) persisten entre login y logout.</p>
 */
public class VentanaLogin extends JFrame {

    private final AppContext ctx;
    private final JTextField campoUsuario = new JTextField(15);
    private final JPasswordField campoClave = new JPasswordField(15);

    public VentanaLogin(AppContext ctx) {
        this.ctx = ctx;
        setTitle("CDCSoft - Iniciar sesion");
        setSize(360, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // cerrar el login cierra la app
        setLayout(new BorderLayout(10, 10));

        // Formulario usuario/contraseña en grilla 2x2.
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        form.add(new JLabel("Usuario:"));
        form.add(campoUsuario);
        form.add(new JLabel("Contraseña:"));
        form.add(campoClave);
        add(form, BorderLayout.CENTER);

        JButton ingresar = new JButton("Ingresar");
        ingresar.addActionListener(e -> ingresar());
        // Enter en cualquiera de los campos también dispara el ingreso.
        getRootPane().setDefaultButton(ingresar);
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sur.add(ingresar);
        add(sur, BorderLayout.SOUTH);
    }

    /**
     * Intenta autenticar. Si las credenciales son válidas, registra el usuario
     * logueado, abre la ventana principal y cierra el login. Si no, avisa el error
     * (sin distinguir si fue usuario, clave o cuenta inactiva).
     */
    private void ingresar() {
        String usuario = campoUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());
        Usuario u = ctx.usuarios.autenticar(usuario, clave);
        if (u == null) {
            Ui.error(this, "Usuario o contraseña incorrectos, o la cuenta está inactiva.");
            return;
        }
        ctx.setUsuarioLogueado(u);
        new VentanaPrincipal(ctx).setVisible(true);
        dispose(); // cierra la ventana de login
    }
}
