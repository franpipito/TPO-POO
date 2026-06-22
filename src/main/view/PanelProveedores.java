package view;

import model.Impuestos;
import model.Proveedor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// ABM de proveedores. Delega en ProveedorController (CUIT único, alta/baja) y permite
// asignar a cada proveedor los impuestos a los que está sujeto a retención.
public class PanelProveedores extends JPanel implements Refrescable {

    private final AppContext ctx;

    private final JTextField campoRazonSocial = new JTextField();
    private final JTextField campoNombreFantasia = new JTextField();
    private final JTextField campoCuit = new JTextField();
    private final JTextField campoDireccion = new JTextField();
    private final JTextField campoTelefono = new JTextField();
    private final JTextField campoEmail = new JTextField();
    private final JTextField campoIngresosBrutos = new JTextField();
    private final JTextField campoTopeDeuda = new JTextField();

    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    public PanelProveedores(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));

        add(crearFormulario(), BorderLayout.NORTH);

        String[] columnas = {"ID", "Razon social", "CUIT", "Tope deuda", "Saldo actual", "Impuestos", "Certificados"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(crearBotones(), BorderLayout.SOUTH);
        refrescar();
    }

    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridLayout(4, 4, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del proveedor"));
        panel.add(new JLabel("Razon social:"));
        panel.add(campoRazonSocial);
        panel.add(new JLabel("Nombre fantasia:"));
        panel.add(campoNombreFantasia);
        panel.add(new JLabel("CUIT:"));
        panel.add(campoCuit);
        panel.add(new JLabel("Direccion:"));
        panel.add(campoDireccion);
        panel.add(new JLabel("Telefono:"));
        panel.add(campoTelefono);
        panel.add(new JLabel("Email:"));
        panel.add(campoEmail);
        panel.add(new JLabel("Ingresos brutos:"));
        panel.add(campoIngresosBrutos);
        panel.add(new JLabel("Tope de deuda:"));
        panel.add(campoTopeDeuda);
        return panel;
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton agregar = new JButton("Agregar");
        JButton eliminar = new JButton("Eliminar");
        JButton asignar = new JButton("Asignar impuestos");
        JButton limpiar = new JButton("Limpiar");
        agregar.addActionListener(e -> agregar());
        eliminar.addActionListener(e -> eliminar());
        asignar.addActionListener(e -> asignarImpuestos());
        limpiar.addActionListener(e -> limpiar());
        panel.add(agregar);
        panel.add(eliminar);
        panel.add(asignar);
        panel.add(limpiar);
        return panel;
    }

    private void agregar() {
        if (campoRazonSocial.getText().trim().isEmpty()) {
            Ui.error(this, "La razon social es obligatoria.");
            return;
        }
        double tope;
        try {
            String t = campoTopeDeuda.getText().trim();
            tope = t.isEmpty() ? 0 : Double.parseDouble(t);
        } catch (NumberFormatException ex) {
            Ui.error(this, "El tope de deuda debe ser un numero.");
            return;
        }

        Proveedor p = new Proveedor();
        p.razonSocial = campoRazonSocial.getText().trim();
        p.nombreFantasia = campoNombreFantasia.getText().trim();
        p.cuit = campoCuit.getText().trim();
        p.direccion = campoDireccion.getText().trim();
        p.telefono = campoTelefono.getText().trim();
        p.email = campoEmail.getText().trim();
        p.ingresosBrutos = campoIngresosBrutos.getText().trim();
        p.topeDeuda = tope;

        try {
            ctx.proveedores.registrar(p);
        } catch (IllegalArgumentException ex) {
            Ui.error(this, ex.getMessage());
            return;
        }
        limpiar();
        refrescar();
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Ui.error(this, "Selecciona un proveedor de la tabla.");
            return;
        }
        Proveedor p = ctx.proveedores.listarTodos().get(fila);
        ctx.proveedores.eliminar(p.idProveedor);
        refrescar();
    }

    // Permite tildar, de la lista maestra de impuestos, a cuáles está sujeto el proveedor.
    private void asignarImpuestos() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Ui.error(this, "Selecciona un proveedor de la tabla.");
            return;
        }
        if (ctx.impuestos.isEmpty()) {
            Ui.info(this, "Primero carga impuestos en la pestaña Catalogo.");
            return;
        }
        Proveedor p = ctx.proveedores.listarTodos().get(fila);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        JCheckBox[] checks = new JCheckBox[ctx.impuestos.size()];
        for (int i = 0; i < ctx.impuestos.size(); i++) {
            Impuestos imp = ctx.impuestos.get(i);
            checks[i] = new JCheckBox(imp.nombre + " (" + imp.porcentajeBase + "%)", p.impuestos.contains(imp));
            panel.add(checks[i]);
        }

        int op = JOptionPane.showConfirmDialog(this, panel,
                "Impuestos de " + p.razonSocial, JOptionPane.OK_CANCEL_OPTION);
        if (op == JOptionPane.OK_OPTION) {
            p.impuestos.clear();
            for (int i = 0; i < checks.length; i++) {
                if (checks[i].isSelected()) {
                    p.impuestos.add(ctx.impuestos.get(i));
                }
            }
            refrescar();
        }
    }

    private void limpiar() {
        for (JTextField c : new JTextField[]{campoRazonSocial, campoNombreFantasia, campoCuit,
                campoDireccion, campoTelefono, campoEmail, campoIngresosBrutos, campoTopeDeuda}) {
            c.setText("");
        }
    }

    @Override
    public void refrescar() {
        modeloTabla.setRowCount(0);
        List<Proveedor> lista = ctx.proveedores.listarTodos();
        for (Proveedor p : lista) {
            modeloTabla.addRow(new Object[]{
                    p.idProveedor, p.razonSocial, p.cuit, p.topeDeuda, p.saldoActual,
                    p.impuestos.size(), p.certificados.size()
            });
        }
    }
}
