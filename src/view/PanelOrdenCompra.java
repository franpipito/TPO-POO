package view;

import model.DetalleOrdenCompra;
import model.OrdenCompra;
import model.ProductoServicio;
import model.Proveedor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Creación de órdenes de compra. Arma el detalle línea por línea y delega en
// OrdenCompraController, que controla el tope de deuda del proveedor: si lo excede,
// la OC queda marcada como "requiere supervisión" y solo se confirma en modo supervisor.
public class PanelOrdenCompra extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JComboBox<Proveedor> comboProveedor = new JComboBox<>();
    private final JComboBox<ProductoServicio> comboProducto = new JComboBox<>();
    private final JTextField campoCantidad = new JTextField(4);
    private final JTextField campoPrecio = new JTextField(8);

    private final List<DetalleOrdenCompra> lineas = new ArrayList<>();
    private final DefaultTableModel modeloDetalle;
    private final DefaultTableModel modeloOrdenes;
    private final JTable tablaOrdenes;

    public PanelOrdenCompra(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));
        Ui.render(comboProveedor, p -> p.razonSocial + " (" + p.cuit + ")");
        Ui.render(comboProducto, ProductoServicio -> ProductoServicio.nombre);

        // --- Encabezado: proveedor + carga de líneas ---
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBorder(BorderFactory.createTitledBorder("Nueva orden de compra"));
        top.add(new JLabel("Proveedor:"));
        top.add(comboProveedor);
        top.add(new JLabel("Producto:"));
        top.add(comboProducto);
        top.add(new JLabel("Cantidad:"));
        top.add(campoCantidad);
        top.add(new JLabel("Precio acordado:"));
        top.add(campoPrecio);
        JButton agregarLinea = new JButton("Agregar linea");
        agregarLinea.addActionListener(e -> agregarLinea());
        top.add(agregarLinea);
        add(top, BorderLayout.NORTH);

        // --- Centro: detalle de la OC en construcción ---
        modeloDetalle = new DefaultTableModel(new String[]{"Producto", "Cantidad", "Precio", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createTitledBorder("Detalle"));
        centro.add(new JScrollPane(new JTable(modeloDetalle)), BorderLayout.CENTER);
        JButton crear = new JButton("Crear OC");
        crear.addActionListener(e -> crearOrden());
        JPanel accion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accion.add(crear);
        centro.add(accion, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        // --- Inferior: órdenes ya creadas ---
        modeloOrdenes = new DefaultTableModel(
                new String[]{"Numero", "Proveedor", "Total", "Estado", "Requiere supervision"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaOrdenes = new JTable(modeloOrdenes);
        JScrollPane scroll = new JScrollPane(tablaOrdenes);
        scroll.setBorder(BorderFactory.createTitledBorder("Ordenes de compra"));
        scroll.setPreferredSize(new Dimension(0, 150));
        add(scroll, BorderLayout.SOUTH);

        refrescar();
    }

    private void agregarLinea() {
        ProductoServicio prod = (ProductoServicio) comboProducto.getSelectedItem();
        if (prod == null) {
            Ui.error(this, "Selecciona un producto.");
            return;
        }
        int cantidad;
        double precio;
        try {
            cantidad = Integer.parseInt(campoCantidad.getText().trim());
            precio = Double.parseDouble(campoPrecio.getText().trim());
        } catch (NumberFormatException ex) {
            Ui.error(this, "Cantidad y precio deben ser numericos.");
            return;
        }
        if (cantidad <= 0 || precio <= 0) {
            Ui.error(this, "La cantidad y el precio deben ser mayores a 0.");
            return;
        }
        DetalleOrdenCompra det = ctx.ordenesCompra.crearDetalle(prod, cantidad, precio);
        lineas.add(det);
        modeloDetalle.addRow(new Object[]{prod.nombre, cantidad, precio, det.calcularSubtotal()});
        campoCantidad.setText("");
        campoPrecio.setText("");
    }

    private void crearOrden() {
        Proveedor prov = (Proveedor) comboProveedor.getSelectedItem();
        if (prov == null) {
            Ui.error(this, "Selecciona un proveedor.");
            return;
        }
        if (lineas.isEmpty()) {
            Ui.error(this, "Agrega al menos una linea al detalle.");
            return;
        }
        try {
            OrdenCompra oc = ctx.ordenesCompra.crearOrden(prov, lineas, ctx.getUsuarioActual());
            Ui.info(this, "OC " + oc.numeroOC + " creada.\nTotal: " + oc.totalEstimado
                    + "\nEstado: " + oc.estado
                    + "\nRequiere supervision: " + (oc.requiereSupervision ? "SI" : "NO"));
            lineas.clear();
            modeloDetalle.setRowCount(0);
            refrescar();
        } catch (IllegalStateException ex) {
            Ui.error(this, ex.getMessage()
                    + "\n\nActiva 'Modo supervisor' en la barra superior para aprobarla.");
        }
    }

    @Override
    public void refrescar() {
        Ui.fill(comboProveedor, ctx.proveedores.listarTodos());
        Ui.fill(comboProducto, ctx.productos);
        modeloOrdenes.setRowCount(0);
        for (OrdenCompra oc : ctx.ordenesCompra.listarTodas()) {
            modeloOrdenes.addRow(new Object[]{
                    oc.numeroOC,
                    oc.proveedor == null ? "" : oc.proveedor.razonSocial,
                    oc.totalEstimado, oc.estado,
                    oc.requiereSupervision ? "SI" : "NO"
            });
        }
    }
}
