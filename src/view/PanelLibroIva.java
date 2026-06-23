package view;

import model.DetalleDocumento;
import model.DocumentoComercial;
import model.Proveedor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Libro IVA Compras: reporte fiscal con un renglon por documento recibido (Factura, NC
// o ND), mostrando CUIT y razon social del proveedor, fecha, tipo, neto gravado, el IVA
// discriminado por alicuota (2.5%, 5%, 10.5%, 21%, 27%) y el total. Las Notas de Credito
// se muestran en negativo porque restan del IVA compras.
public class PanelLibroIva extends JPanel implements Refrescable {

    // Alicuotas que se discriminan en columnas separadas.
    private static final double[] ALICUOTAS = {2.5, 5, 10.5, 21, 27};

    private final AppContext ctx;
    private final DefaultTableModel modeloTabla;

    public PanelLibroIva(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton generar = new JButton("Generar Libro IVA");
        generar.addActionListener(e -> refrescar());
        barra.add(generar);
        add(barra, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new String[]{
                "CUIT", "Razon social", "Fecha", "Tipo", "Numero", "Neto gravado",
                "IVA 2.5%", "IVA 5%", "IVA 10.5%", "IVA 21%", "IVA 27%", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JScrollPane scroll = new JScrollPane(new JTable(modeloTabla));
        scroll.setBorder(BorderFactory.createTitledBorder("Libro IVA Compras"));
        add(scroll, BorderLayout.CENTER);

        refrescar();
    }

    @Override
    public void refrescar() {
        modeloTabla.setRowCount(0);
        double totalNeto = 0;
        double[] totalIva = new double[ALICUOTAS.length];
        double totalGeneral = 0;

        for (Proveedor p : ctx.proveedores.listarTodos()) {
            for (DocumentoComercial doc : p.documentos) {
                double signo = "NC".equals(doc.getTipo()) ? -1 : 1;
                double neto = 0;
                double[] iva = new double[ALICUOTAS.length];
                for (DetalleDocumento det : doc.detalles) {
                    double sub = det.calcularSubtotal();
                    neto += sub;
                    for (int i = 0; i < ALICUOTAS.length; i++) {
                        if (det.alicuotaIva == ALICUOTAS[i]) {
                            iva[i] += sub * ALICUOTAS[i] / 100.0;
                        }
                    }
                }
                double ivaDoc = 0;
                for (double v : iva) ivaDoc += v;
                double total = neto + ivaDoc;

                Object[] fila = new Object[12];
                fila[0] = p.cuit;
                fila[1] = p.razonSocial;
                fila[2] = Ui.fmt(doc.fechaEmision);
                fila[3] = doc.getTipo();
                fila[4] = doc.getNumero();
                fila[5] = redondear(neto * signo);
                for (int i = 0; i < ALICUOTAS.length; i++) {
                    fila[6 + i] = redondear(iva[i] * signo);
                }
                fila[11] = redondear(total * signo);
                modeloTabla.addRow(fila);

                totalNeto += neto * signo;
                for (int i = 0; i < ALICUOTAS.length; i++) totalIva[i] += iva[i] * signo;
                totalGeneral += total * signo;
            }
        }

        // Renglon de totales del libro.
        Object[] totales = new Object[12];
        totales[0] = "";
        totales[1] = "";
        totales[2] = "";
        totales[3] = "";
        totales[4] = "TOTALES";
        totales[5] = redondear(totalNeto);
        for (int i = 0; i < ALICUOTAS.length; i++) totales[6 + i] = redondear(totalIva[i]);
        totales[11] = redondear(totalGeneral);
        modeloTabla.addRow(totales);
    }

    // Redondea a 2 decimales para la presentacion del reporte.
    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
