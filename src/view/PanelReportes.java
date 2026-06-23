package view;

import model.DocumentoComercial;
import model.OrdenPago;
import model.Proveedor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

// Reportes por proveedor y rango de fechas: documentos comerciales y órdenes de pago.
// Usa los métodos de consulta de ProveedorController (filtran por fecha de emisión).
public class PanelReportes extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JComboBox<Proveedor> comboProveedor = new JComboBox<>();
    private final JTextField campoDesde = new JTextField(10);
    private final JTextField campoHasta = new JTextField(10);
    private final DefaultTableModel modeloDocs;
    private final DefaultTableModel modeloPagos;

    public PanelReportes(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));
        Ui.render(comboProveedor, p -> p.razonSocial + " (" + p.cuit + ")");

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

        modeloDocs = new DefaultTableModel(new String[]{"Tipo", "Numero", "Fecha", "Importe"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modeloPagos = new DefaultTableModel(
                new String[]{"Numero", "Fecha", "Total a cancelar", "Retenciones"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JScrollPane scrollDocs = new JScrollPane(new JTable(modeloDocs));
        scrollDocs.setBorder(BorderFactory.createTitledBorder("Documentos comerciales"));
        JScrollPane scrollPagos = new JScrollPane(new JTable(modeloPagos));
        scrollPagos.setBorder(BorderFactory.createTitledBorder("Ordenes de pago"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollDocs, scrollPagos);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        refrescar();
    }

    private void generar() {
        Proveedor prov = (Proveedor) comboProveedor.getSelectedItem();
        if (prov == null) {
            Ui.error(this, "Selecciona un proveedor.");
            return;
        }
        Date desde;
        Date hasta;
        try {
            desde = Ui.parseFecha(campoDesde.getText());
            hasta = Ui.parseFecha(campoHasta.getText());
        } catch (ParseException ex) {
            Ui.error(this, "Formato de fecha invalido. Usa aaaa-mm-dd.");
            return;
        }

        modeloDocs.setRowCount(0);
        List<DocumentoComercial> docs = ctx.proveedores.getDocumentosComerciales(prov.idProveedor, desde, hasta);
        for (DocumentoComercial doc : docs) {
            modeloDocs.addRow(new Object[]{doc.getTipo(), doc.getNumero(), Ui.fmt(doc.fechaEmision), doc.importeTotal});
        }

        modeloPagos.setRowCount(0);
        List<OrdenPago> pagos = ctx.proveedores.getOrdenesPago(prov.idProveedor, desde, hasta);
        for (OrdenPago op : pagos) {
            modeloPagos.addRow(new Object[]{
                    op.numeroOperacion, Ui.fmt(op.fechaEmision),
                    op.getTotalACancelar(), op.totalRetenciones
            });
        }
    }

    @Override
    public void refrescar() {
        Ui.fill(comboProveedor, ctx.proveedores.listarTodos());
    }
}
