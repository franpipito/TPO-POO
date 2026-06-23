package view;

import model.DetalleDocumento;
import model.DocumentoComercial;
import model.Factura;
import model.NotaCredito;
import model.NotaDebito;
import model.OrdenCompra;
import model.ProductoServicio;
import model.Proveedor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Registro de comprobantes: Factura, Nota de Crédito y Nota de Débito (todos comparten
// DocumentoComercial). Para Factura, valida los precios contra la OC asociada; si no
// coinciden (o no hay OC) requiere aprobación supervisorial.
public class PanelComprobantes extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JComboBox<String> comboTipo = new JComboBox<>(
            new String[]{"Factura", "Nota de Credito", "Nota de Debito"});
    private final JComboBox<Proveedor> comboProveedor = new JComboBox<>();
    private final JComboBox<OrdenCompra> comboOrden = new JComboBox<>();
    private final JComboBox<ProductoServicio> comboProducto = new JComboBox<>();
    private final JTextField campoCantidad = new JTextField(4);
    private final JTextField campoPrecio = new JTextField(8);

    private final List<DetalleDocumento> lineas = new ArrayList<>();
    private final DefaultTableModel modeloDetalle;
    private final DefaultTableModel modeloDocs;

    public PanelComprobantes(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));
        Ui.render(comboProveedor, p -> p.razonSocial + " (" + p.cuit + ")");
        Ui.render(comboOrden, oc -> oc.numeroOC + " - total " + oc.totalEstimado);
        Ui.render(comboProducto, prod -> prod.nombre);

        comboTipo.addActionListener(e -> actualizarHabilitacionOC());
        comboProveedor.addActionListener(e -> recargarOrdenes());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBorder(BorderFactory.createTitledBorder("Nuevo comprobante"));
        top.add(new JLabel("Tipo:"));
        top.add(comboTipo);
        top.add(new JLabel("Proveedor:"));
        top.add(comboProveedor);
        top.add(new JLabel("OC asociada:"));
        top.add(comboOrden);
        add(top, BorderLayout.NORTH);

        JPanel medio = new JPanel(new BorderLayout());
        JPanel lineaForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lineaForm.add(new JLabel("Producto:"));
        lineaForm.add(comboProducto);
        lineaForm.add(new JLabel("Cantidad:"));
        lineaForm.add(campoCantidad);
        lineaForm.add(new JLabel("Precio unitario:"));
        lineaForm.add(campoPrecio);
        JButton agregarLinea = new JButton("Agregar linea");
        agregarLinea.addActionListener(e -> agregarLinea());
        lineaForm.add(agregarLinea);
        medio.add(lineaForm, BorderLayout.NORTH);

        modeloDetalle = new DefaultTableModel(new String[]{"Producto", "Cantidad", "Precio", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        medio.add(new JScrollPane(new JTable(modeloDetalle)), BorderLayout.CENTER);
        JButton registrar = new JButton("Registrar comprobante");
        registrar.addActionListener(e -> registrar());
        JPanel accion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accion.add(registrar);
        medio.add(accion, BorderLayout.SOUTH);
        medio.setBorder(BorderFactory.createTitledBorder("Detalle"));
        add(medio, BorderLayout.CENTER);

        modeloDocs = new DefaultTableModel(new String[]{"Tipo", "Numero", "Fecha", "Importe"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JScrollPane scroll = new JScrollPane(new JTable(modeloDocs));
        scroll.setBorder(BorderFactory.createTitledBorder("Comprobantes registrados"));
        scroll.setPreferredSize(new Dimension(0, 150));
        add(scroll, BorderLayout.SOUTH);

        refrescar();
        actualizarHabilitacionOC();
    }

    // La OC solo aplica a Facturas (validación de precios). NC/ND no la usan.
    private void actualizarHabilitacionOC() {
        boolean esFactura = comboTipo.getSelectedIndex() == 0;
        comboOrden.setEnabled(esFactura);
    }

    private void recargarOrdenes() {
        Proveedor prov = (Proveedor) comboProveedor.getSelectedItem();
        if (prov == null) {
            Ui.fill(comboOrden, new ArrayList<>());
        } else {
            Ui.fill(comboOrden, ctx.ordenesCompra.listarPorProveedor(prov));
        }
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
        DetalleDocumento det = new DetalleDocumento();
        det.producto = prod;
        det.cantidad = cantidad;
        det.precioUnitarioAplicado = precio;
        det.calcularSubtotal();
        lineas.add(det);
        modeloDetalle.addRow(new Object[]{prod.nombre, cantidad, precio, det.subTotalLinea});
        campoCantidad.setText("");
        campoPrecio.setText("");
    }

    private void registrar() {
        Proveedor prov = (Proveedor) comboProveedor.getSelectedItem();
        if (prov == null) {
            Ui.error(this, "Selecciona un proveedor.");
            return;
        }
        if (lineas.isEmpty()) {
            Ui.error(this, "Agrega al menos una linea al detalle.");
            return;
        }
        int tipo = comboTipo.getSelectedIndex();
        try {
            DocumentoComercial registrado;
            if (tipo == 0) {
                Factura f = new Factura();
                f.fechaEmision = new Date();
                f.detalles.addAll(lineas);
                OrdenCompra oc = (OrdenCompra) comboOrden.getSelectedItem();
                registrado = ctx.facturas.registrar(f, oc, prov, ctx.getUsuarioActual());
            } else if (tipo == 1) {
                NotaCredito nc = new NotaCredito();
                nc.fechaEmision = new Date();
                nc.detalles.addAll(lineas);
                registrado = ctx.notasCredito.registrar(nc, prov);
            } else {
                NotaDebito nd = new NotaDebito();
                nd.fechaEmision = new Date();
                nd.detalles.addAll(lineas);
                registrado = ctx.notasDebito.registrar(nd, prov);
            }
            Ui.info(this, registrado.getTipo() + " " + registrado.getNumero()
                    + " registrada.\nImporte: " + registrado.importeTotal
                    + "\nSaldo del proveedor: " + prov.saldoActual);
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
        recargarOrdenes();
        modeloDocs.setRowCount(0);
        for (DocumentoComercial f : ctx.facturas.listarTodas()) {
            agregarFila(f);
        }
        for (DocumentoComercial nc : ctx.notasCredito.listarTodas()) {
            agregarFila(nc);
        }
        for (DocumentoComercial nd : ctx.notasDebito.listarTodas()) {
            agregarFila(nd);
        }
    }

    private void agregarFila(DocumentoComercial doc) {
        modeloDocs.addRow(new Object[]{doc.getTipo(), doc.getNumero(), Ui.fmt(doc.fechaEmision), doc.importeTotal});
    }
}
