package view;

import model.DocumentoComercial;
import model.OrdenPago;
import model.Proveedor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Cuenta Corriente del proveedor: muestra la deuda total acumulada, el detalle de los
// documentos recibidos (F / NC / ND), el listado de documentos impagos (facturas y notas
// de debito pendientes de cancelacion) y el historial de pagos realizados (ordenes de pago).
public class PanelCuentaCorriente extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JComboBox<Proveedor> comboProveedor = new JComboBox<>();
    private final JLabel labelDeuda = new JLabel("Deuda total: -");
    private final DefaultTableModel modeloRecibidos;
    private final DefaultTableModel modeloImpagos;
    private final DefaultTableModel modeloPagos;

    public PanelCuentaCorriente(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));
        Ui.render(comboProveedor, p -> p.razonSocial + " (" + p.cuit + ")");
        comboProveedor.addActionListener(e -> refrescarDatos());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.setBorder(BorderFactory.createTitledBorder("Cuenta corriente"));
        barra.add(new JLabel("Proveedor:"));
        barra.add(comboProveedor);
        barra.add(Box.createHorizontalStrut(20));
        labelDeuda.setFont(labelDeuda.getFont().deriveFont(Font.BOLD));
        barra.add(labelDeuda);
        add(barra, BorderLayout.NORTH);

        modeloRecibidos = tabla(new String[]{"Tipo", "Numero", "Fecha", "Importe", "Estado"});
        modeloImpagos = tabla(new String[]{"Tipo", "Numero", "Fecha", "Importe"});
        modeloPagos = tabla(new String[]{"Numero", "Fecha", "Total a cancelar", "Retenciones"});

        JScrollPane scrollRecibidos = new JScrollPane(new JTable(modeloRecibidos));
        scrollRecibidos.setBorder(BorderFactory.createTitledBorder("Documentos recibidos (F / NC / ND)"));
        JScrollPane scrollImpagos = new JScrollPane(new JTable(modeloImpagos));
        scrollImpagos.setBorder(BorderFactory.createTitledBorder("Documentos impagos"));
        JScrollPane scrollPagos = new JScrollPane(new JTable(modeloPagos));
        scrollPagos.setBorder(BorderFactory.createTitledBorder("Historial de pagos (ordenes de pago)"));

        JSplitPane abajo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollImpagos, scrollPagos);
        abajo.setResizeWeight(0.5);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollRecibidos, abajo);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

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

    private void refrescarDatos() {
        modeloRecibidos.setRowCount(0);
        modeloImpagos.setRowCount(0);
        modeloPagos.setRowCount(0);
        Proveedor prov = (Proveedor) comboProveedor.getSelectedItem();
        if (prov == null) {
            labelDeuda.setText("Deuda total: -");
            return;
        }
        labelDeuda.setText("Deuda total: $" + redondear(prov.saldoActual));

        for (DocumentoComercial doc : prov.documentos) {
            modeloRecibidos.addRow(new Object[]{
                    doc.getTipo(), doc.getNumero(), Ui.fmt(doc.fechaEmision),
                    doc.importeTotal, doc.pagado ? "Pagado" : "Impago"
            });
            // Las NC no son deuda a cancelar; los impagos son facturas y ND pendientes.
            if (!doc.pagado && !"NC".equals(doc.getTipo())) {
                modeloImpagos.addRow(new Object[]{
                        doc.getTipo(), doc.getNumero(), Ui.fmt(doc.fechaEmision), doc.importeTotal
                });
            }
        }

        for (OrdenPago op : ctx.ordenesPago.listarPorProveedor(prov)) {
            modeloPagos.addRow(new Object[]{
                    op.numeroOperacion, Ui.fmt(op.fechaEmision),
                    op.getTotalACancelar(), op.totalRetenciones
            });
        }
    }

    @Override
    public void refrescar() {
        Ui.fill(comboProveedor, ctx.proveedores.listarTodos());
        refrescarDatos();
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
