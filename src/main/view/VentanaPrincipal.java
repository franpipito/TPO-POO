package view;

import javax.swing.*;
import java.awt.*;

// Ventana principal del Sistema de Gestión de Facturas y Proveedores (CDCSoft).
// Organiza los módulos en pestañas, cada una cableada a su controller a través del
// AppContext compartido. La barra superior permite conmutar el "modo supervisor",
// que habilita la aprobación de operaciones que exceden el tope de deuda o cuyos
// precios de factura no coinciden con la OC.
public class VentanaPrincipal extends JFrame {

    private final AppContext ctx = new AppContext();
    private final JTabbedPane pestanias = new JTabbedPane();

    public VentanaPrincipal() {
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
        pestanias.addTab("Reportes", new PanelReportes(ctx));

        // Al cambiar de pestaña, el panel recarga sus combos/tablas con los datos vigentes.
        pestanias.addChangeListener(e -> refrescarSeleccion());
        add(pestanias, BorderLayout.CENTER);
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox chkSupervisor = new JCheckBox("Modo supervisor (aprueba operaciones que requieren supervision)");
        chkSupervisor.addActionListener(e -> ctx.setModoSupervisor(chkSupervisor.isSelected()));
        barra.add(chkSupervisor);
        return barra;
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
