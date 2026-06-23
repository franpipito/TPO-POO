package view;

import model.CertificadoNoRetencion;
import model.Impuestos;
import model.Proveedor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.util.Date;

// Alta de certificados de no retención y consulta de su vigencia. Mientras un
// certificado está vigente, su impuesto no se retiene al emitir órdenes de pago.
public class PanelCertificados extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JComboBox<Proveedor> comboProveedor = new JComboBox<>();
    private final JComboBox<Impuestos> comboImpuesto = new JComboBox<>();
    private final JTextField campoInicio = new JTextField(10);
    private final JTextField campoFin = new JTextField(10);
    private final DefaultTableModel modeloTabla;

    public PanelCertificados(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));
        Ui.render(comboProveedor, p -> p.razonSocial + " (" + p.cuit + ")");
        Ui.render(comboImpuesto, i -> i.nombre + " (" + i.porcentajeBase + "%)");
        comboProveedor.addActionListener(e -> refrescarTabla());

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setBorder(BorderFactory.createTitledBorder("Nuevo certificado de no retencion"));
        form.add(new JLabel("Proveedor:"));
        form.add(comboProveedor);
        form.add(new JLabel("Impuesto:"));
        form.add(comboImpuesto);
        form.add(new JLabel("Desde (aaaa-mm-dd):"));
        form.add(campoInicio);
        form.add(new JLabel("Hasta (aaaa-mm-dd):"));
        form.add(campoFin);
        JButton crear = new JButton("Crear certificado");
        crear.addActionListener(e -> crear());
        form.add(crear);
        add(form, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(
                new String[]{"Impuesto", "Desde", "Hasta", "Vigente hoy"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JScrollPane scroll = new JScrollPane(new JTable(modeloTabla));
        scroll.setBorder(BorderFactory.createTitledBorder("Certificados del proveedor seleccionado"));
        add(scroll, BorderLayout.CENTER);

        refrescar();
    }

    private void crear() {
        Proveedor prov = (Proveedor) comboProveedor.getSelectedItem();
        Impuestos imp = (Impuestos) comboImpuesto.getSelectedItem();
        if (prov == null || imp == null) {
            Ui.error(this, "Selecciona proveedor e impuesto.");
            return;
        }
        Date inicio;
        Date fin;
        try {
            inicio = Ui.parseFecha(campoInicio.getText());
            fin = Ui.parseFecha(campoFin.getText());
        } catch (ParseException ex) {
            Ui.error(this, "Formato de fecha invalido. Usa aaaa-mm-dd.");
            return;
        }
        if (inicio == null || fin == null) {
            Ui.error(this, "Ambas fechas son obligatorias.");
            return;
        }
        // La vigencia tiene que tener sentido: el inicio no puede ser posterior al fin.
        if (inicio.after(fin)) {
            Ui.error(this, "La fecha 'Desde' no puede ser posterior a 'Hasta'.");
            return;
        }
        ctx.certificados.crear(prov, imp, inicio, fin);
        campoInicio.setText("");
        campoFin.setText("");
        refrescarTabla();
    }

    private void refrescarTabla() {
        modeloTabla.setRowCount(0);
        Proveedor prov = (Proveedor) comboProveedor.getSelectedItem();
        if (prov == null) {
            return;
        }
        Date hoy = new Date();
        for (CertificadoNoRetencion cert : prov.certificados) {
            modeloTabla.addRow(new Object[]{
                    cert.impuesto == null ? "" : cert.impuesto.nombre,
                    Ui.fmt(cert.fechaInicio), Ui.fmt(cert.fechaFin),
                    cert.estaVigente(hoy) ? "SI" : "NO"
            });
        }
    }

    @Override
    public void refrescar() {
        Ui.fill(comboProveedor, ctx.proveedores.listarTodos());
        Ui.fill(comboImpuesto, ctx.impuestos);
        refrescarTabla();
    }
}
