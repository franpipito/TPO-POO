package view;

import model.Usuario;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal del Sistema de Gestión de Facturas y Proveedores (CDCSoft).
 * Es el "shell" de la interfaz: organiza los módulos en pestañas, cada una cableada
 * a su controller a través del {@link AppContext} compartido.
 *
 * <p>Recibe el contexto ya con un usuario logueado. La barra superior muestra quién
 * está logueado y permite cerrar sesión. Las pestañas de administración (Usuarios y
 * Roles) solo se agregan si el usuario es Supervisor (tiene permiso de aprobación).</p>
 */
public class VentanaPrincipal extends JFrame {

    private final AppContext ctx;
    private final JTabbedPane pestanias = new JTabbedPane();

    public VentanaPrincipal(AppContext ctx) {
        this.ctx = ctx;
        setTitle("CDCSoft - Gestion de Facturas y Proveedores");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(crearBarraSuperior(), BorderLayout.NORTH);

        pestanias.addTab("Proveedores", new PanelProveedores(ctx));
        pestanias.addTab("Catalogo", crearPestaniaCatalogo());
        pestanias.addTab("Ordenes de Compra", new PanelOrdenCompra(ctx));
        pestanias.addTab("Comprobantes", new PanelComprobantes(ctx));
        pestanias.addTab("Ordenes de Pago", new PanelOrdenPago(ctx));
        pestanias.addTab("Certificados", new PanelCertificados(ctx));
        pestanias.addTab("Consultas", crearPestaniaConsultas());
        pestanias.addTab("Reportes", new PanelReportes(ctx));

        // Solo un Supervisor ve las pestañas de administración de usuarios y roles.
        if (ctx.puedeGestionarUsuarios()) {
            pestanias.addTab("Usuarios", new PanelUsuarios(ctx));
            pestanias.addTab("Roles", new PanelRoles(ctx));
        }

        // Al cambiar de pestaña, el panel recarga sus combos/tablas con los datos vigentes.
        pestanias.addChangeListener(e -> refrescarSeleccion());
        add(pestanias, BorderLayout.CENTER);
    }

    /**
     * Barra superior: muestra el usuario logueado y su rol, y ofrece "Cerrar sesión",
     * que vuelve a la pantalla de login (conservando los datos en memoria).
     */
    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Usuario u = ctx.getUsuarioActual();
        String rol = (u != null && u.rol != null) ? u.rol.nombre : "-";
        String nombre = (u != null) ? u.nombreUsuario : "-";
        barra.add(new JLabel("Usuario: " + nombre + " (" + rol + ")"));

        JButton cerrarSesion = new JButton("Cerrar sesion");
        cerrarSesion.addActionListener(e -> cerrarSesion());
        barra.add(cerrarSesion);
        return barra;
    }

    // Cierra la sesión: limpia el usuario logueado, cierra esta ventana y reabre el login.
    private void cerrarSesion() {
        ctx.setUsuarioLogueado(null);
        new VentanaLogin(ctx).setVisible(true);
        dispose();
    }

    // Sub-pestañas de datos maestros: rubros, productos e impuestos.
    private JTabbedPane crearPestaniaCatalogo() {
        JTabbedPane catalogo = new JTabbedPane();
        catalogo.addTab("Rubros", new PanelRubros(ctx));
        catalogo.addTab("Productos", new PanelProductos(ctx));
        catalogo.addTab("Impuestos", new PanelImpuestos(ctx));
        catalogo.addChangeListener(e -> refrescarComponente(catalogo.getSelectedComponent()));
        return catalogo;
    }

    // Sub-pestañas de consultas de gestión: cuenta corriente, libro IVA, compulsa de
    // precios y análisis financiero.
    private JTabbedPane crearPestaniaConsultas() {
        JTabbedPane consultas = new JTabbedPane();
        consultas.addTab("Cuenta Corriente", new PanelCuentaCorriente(ctx));
        consultas.addTab("Libro IVA Compras", new PanelLibroIva(ctx));
        consultas.addTab("Compulsa de Precios", new PanelCompulsaPrecios(ctx));
        consultas.addTab("Analisis Financiero", new PanelAnalisisFinanciero(ctx));
        consultas.addChangeListener(e -> refrescarComponente(consultas.getSelectedComponent()));
        return consultas;
    }

    private void refrescarSeleccion() {
        Component sel = pestanias.getSelectedComponent();
        if (sel instanceof JTabbedPane) {
            // Pestaña Catálogo: refresca la sub-pestaña activa.
            refrescarComponente(((JTabbedPane) sel).getSelectedComponent());
        } else {
            refrescarComponente(sel);
        }
    }

    private void refrescarComponente(Component componente) {
        if (componente instanceof Refrescable) {
            ((Refrescable) componente).refrescar();
        }
    }
}
