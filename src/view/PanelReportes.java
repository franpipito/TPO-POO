package view;

import model.DocumentoComercial;
import model.Factura;
import model.OrdenPago;
import model.Proveedor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Reportes de gestión por rango de fechas:
//  - Flujo de facturas recibidas desagregado por día (cantidad y total facturado). Toma
//    todas las facturas o solo las del proveedor elegido.
//  - Documentos comerciales y órdenes de pago del proveedor seleccionado.
// El proveedor es opcional: con "(Todos los proveedores)" el flujo por día es global.
public class PanelReportes extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JComboBox<Proveedor> comboProveedor = new JComboBox<>();
    private final JTextField campoDesde = new JTextField(10);
    private final JTextField campoHasta = new JTextField(10);
    private final DefaultTableModel modeloFlujo;
    private final DefaultTableModel modeloDocs;
    private final DefaultTableModel modeloPagos;

    public PanelReportes(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));
        // Render del combo: "(Todos los proveedores)" cuando no hay uno seleccionado (null).
        comboProveedor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("(Todos los proveedores)");
                } else {
                    Proveedor p = (Proveedor) value;
                    setText(p.razonSocial + " (" + p.cuit + ")");
                }
                return this;
            }
        });

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setBorder(BorderFactory.createTitledBorder("Filtros"));
        form.add(new JLabel("Proveedor:"));
        form.add(comboProveedor);
        form.add(new JLabel("Desde (aaaa-mm-dd):"));
        form.add(campoDesde);
        form.add(new JLabel("Hasta (aaaa-mm-dd):"));
        form.add(campoHasta);
        JButton generar = new JButton("Generar");
        generar.addActionListener(e -> generar());
        form.add(generar);
        add(form, BorderLayout.NORTH);

        modeloFlujo = tabla(new String[]{"Dia", "Cantidad de facturas", "Total facturado"});
        modeloDocs = tabla(new String[]{"Tipo", "Numero", "Fecha", "Importe"});
        modeloPagos = tabla(new String[]{"Numero", "Fecha", "Total a cancelar", "Retenciones"});

        JScrollPane scrollFlujo = new JScrollPane(new JTable(modeloFlujo));
        scrollFlujo.setBorder(BorderFactory.createTitledBorder("Flujo de facturas recibidas por dia"));
        JScrollPane scrollDocs = new JScrollPane(new JTable(modeloDocs));
        scrollDocs.setBorder(BorderFactory.createTitledBorder("Documentos comerciales del proveedor"));
        JScrollPane scrollPagos = new JScrollPane(new JTable(modeloPagos));
        scrollPagos.setBorder(BorderFactory.createTitledBorder("Ordenes de pago del proveedor"));

        JPanel tablas = new JPanel(new GridLayout(3, 1, 5, 5));
        tablas.add(scrollFlujo);
        tablas.add(scrollDocs);
        tablas.add(scrollPagos);
        add(tablas, BorderLayout.CENTER);

        refrescar();
    }

    private DefaultTableModel tabla(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void generar() {
        Date desde;
        Date hasta;
        try {
            desde = Ui.parseFecha(campoDesde.getText());
            hasta = Ui.parseFecha(campoHasta.getText());
        } catch (ParseException ex) {
            Ui.error(this, "Formato de fecha invalido. Usa aaaa-mm-dd.");
            return;
        }

        Proveedor prov = (Proveedor) comboProveedor.getSelectedItem();
        generarFlujoPorDia(prov, desde, hasta);
        generarTablasProveedor(prov, desde, hasta);
    }

    // Total de facturas recibidas desagregadas por día (cantidad y monto). Si hay un
    // proveedor seleccionado considera solo sus facturas; si no, las de todos.
    private void generarFlujoPorDia(Proveedor prov, Date desde, Date hasta) {
        modeloFlujo.setRowCount(0);
        // TreeMap para que los días queden ordenados cronológicamente (clave aaaa-mm-dd).
        Map<String, double[]> porDia = new TreeMap<>(); // dia -> [cantidad, total]
        int totalCantidad = 0;
        double totalMonto = 0;

        for (Factura f : ctx.facturas.listarTodas()) {
            if (prov != null && !prov.documentos.contains(f)) continue;
            if (!estaEnRango(f.fechaEmision, desde, hasta)) continue;
            String dia = Ui.fmt(f.fechaEmision);
            double[] acum = porDia.computeIfAbsent(dia, k -> new double[2]);
            acum[0] += 1;
            acum[1] += f.importeTotal;
            totalCantidad += 1;
            totalMonto += f.importeTotal;
        }

        for (Map.Entry<String, double[]> e : porDia.entrySet()) {
            modeloFlujo.addRow(new Object[]{e.getKey(), (int) e.getValue()[0], redondear(e.getValue()[1])});
        }
        modeloFlujo.addRow(new Object[]{"TOTAL", totalCantidad, redondear(totalMonto)});
    }

    // Documentos comerciales y órdenes de pago del proveedor (solo si hay uno seleccionado).
    private void generarTablasProveedor(Proveedor prov, Date desde, Date hasta) {
        modeloDocs.setRowCount(0);
        modeloPagos.setRowCount(0);
        if (prov == null) {
            return;
        }
        List<DocumentoComercial> docs = ctx.proveedores.getDocumentosComerciales(prov.idProveedor, desde, hasta);
        for (DocumentoComercial doc : docs) {
            modeloDocs.addRow(new Object[]{doc.getTipo(), doc.getNumero(), Ui.fmt(doc.fechaEmision), doc.importeTotal});
        }
        List<OrdenPago> pagos = ctx.proveedores.getOrdenesPago(prov.idProveedor, desde, hasta);
        for (OrdenPago op : pagos) {
            modeloPagos.addRow(new Object[]{
                    op.numeroOperacion, Ui.fmt(op.fechaEmision),
                    op.getTotalACancelar(), op.totalRetenciones
            });
        }
    }

    // True si la fecha cae dentro de [desde, hasta] (límites inclusivos y opcionales).
    private boolean estaEnRango(Date fecha, Date desde, Date hasta) {
        if (fecha == null) return false;
        if (desde != null && fecha.before(desde)) return false;
        if (hasta != null && fecha.after(hasta)) return false;
        return true;
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    @Override
    public void refrescar() {
        // Primer elemento null => opción "(Todos los proveedores)".
        List<Proveedor> items = new ArrayList<>();
        items.add(null);
        items.addAll(ctx.proveedores.listarTodos());
        Ui.fill(comboProveedor, items);
    }
}
