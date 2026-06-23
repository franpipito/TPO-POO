package view;

import model.Cheque;
import model.DocumentoComercial;
import model.Efectivo;
import model.MedioPago;
import model.OrdenPago;
import model.Proveedor;
import model.RetencionAplicada;
import model.Transferencia;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Emisión de órdenes de pago. Se eligen los documentos del proveedor a cancelar y los
// medios de pago; el controller calcula la base imponible y desglosa las retenciones
// (respetando mínimos no imponibles y certificados de no retención vigentes).
public class PanelOrdenPago extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JComboBox<Proveedor> comboProveedor = new JComboBox<>();
    private final JList<DocumentoComercial> listaDocs = new JList<>();

    private final JComboBox<String> comboMedio = new JComboBox<>(
            new String[]{"Efectivo", "Cheque", "Transferencia"});
    private final JTextField campoMonto = new JTextField(8);
    private final List<MedioPago> medios = new ArrayList<>();
    private final DefaultTableModel modeloMedios;

    private final DefaultTableModel modeloOrdenes;
    private final JTextArea resultado = new JTextArea(6, 30);

    public PanelOrdenPago(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));
        Ui.render(comboProveedor, p -> p.razonSocial + " (" + p.cuit + ")");
        Ui.renderList(listaDocs, d -> d.getTipo() + " " + d.getNumero() + "  $" + d.importeTotal);
        comboProveedor.addActionListener(e -> recargarDocumentos());

        // --- Encabezado: proveedor + documentos a cancelar ---
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("Documentos a cancelar"));
        JPanel selProv = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selProv.add(new JLabel("Proveedor:"));
        selProv.add(comboProveedor);
        top.add(selProv, BorderLayout.NORTH);
        listaDocs.setVisibleRowCount(4);
        top.add(new JScrollPane(listaDocs), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // --- Centro: medios de pago ---
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createTitledBorder("Medios de pago"));
        JPanel medioForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        medioForm.add(new JLabel("Tipo:"));
        medioForm.add(comboMedio);
        medioForm.add(new JLabel("Monto:"));
        medioForm.add(campoMonto);
        JButton agregarMedio = new JButton("Agregar medio");
        agregarMedio.addActionListener(e -> agregarMedio());
        medioForm.add(agregarMedio);
        centro.add(medioForm, BorderLayout.NORTH);
        modeloMedios = new DefaultTableModel(new String[]{"Tipo", "Monto"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        centro.add(new JScrollPane(new JTable(modeloMedios)), BorderLayout.CENTER);
        JButton emitir = new JButton("Emitir OP");
        emitir.addActionListener(e -> emitir());
        JPanel accion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accion.add(emitir);
        resultado.setEditable(false);
        accion.add(new JScrollPane(resultado));
        centro.add(accion, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        // --- Inferior: OP emitidas ---
        modeloOrdenes = new DefaultTableModel(
                new String[]{"Numero", "Proveedor", "Base imponible", "Retenciones", "Total a cancelar"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JScrollPane scroll = new JScrollPane(new JTable(modeloOrdenes));
        scroll.setBorder(BorderFactory.createTitledBorder("Ordenes de pago"));
        scroll.setPreferredSize(new Dimension(0, 130));
        add(scroll, BorderLayout.SOUTH);

        refrescar();
    }

    private void recargarDocumentos() {
        Proveedor prov = (Proveedor) comboProveedor.getSelectedItem();
        DefaultListModel<DocumentoComercial> modelo = new DefaultListModel<>();
        if (prov != null) {
            for (DocumentoComercial doc : prov.documentos) {
                modelo.addElement(doc);
            }
        }
        listaDocs.setModel(modelo);
    }

    private void agregarMedio() {
        double monto;
        try {
            monto = Double.parseDouble(campoMonto.getText().trim());
        } catch (NumberFormatException ex) {
            Ui.error(this, "El monto debe ser numerico.");
            return;
        }
        String tipo = (String) comboMedio.getSelectedItem();
        MedioPago medio;
        if ("Cheque".equals(tipo)) {
            medio = new Cheque();
        } else if ("Transferencia".equals(tipo)) {
            medio = new Transferencia();
        } else {
            medio = new Efectivo();
        }
        medio.montoAplicado = monto;
        medios.add(medio);
        modeloMedios.addRow(new Object[]{tipo, monto});
        campoMonto.setText("");
    }

    private void emitir() {
        Proveedor prov = (Proveedor) comboProveedor.getSelectedItem();
        if (prov == null) {
            Ui.error(this, "Selecciona un proveedor.");
            return;
        }
        List<DocumentoComercial> seleccionados = listaDocs.getSelectedValuesList();
        if (seleccionados.isEmpty()) {
            Ui.error(this, "Selecciona al menos un documento a cancelar.");
            return;
        }
        OrdenPago op = ctx.ordenesPago.emitir(prov, seleccionados, medios, new Date());

        StringBuilder sb = new StringBuilder();
        sb.append("OP ").append(op.numeroOperacion).append("\n");
        sb.append("Base imponible: ").append(op.calcularBaseImponible()).append("\n");
        sb.append("Total retenciones: ").append(op.totalRetenciones).append("\n");
        sb.append("Total a cancelar: ").append(op.getTotalACancelar()).append("\n");
        if (op.retencionesAplicadas.isEmpty()) {
            sb.append("(Sin retenciones)\n");
        } else {
            sb.append("Detalle de retenciones:\n");
            for (RetencionAplicada r : op.retencionesAplicadas) {
                sb.append("  - ").append(r.impuesto.nombre)
                        .append(" ").append(r.porcentajeAplicado).append("% = ")
                        .append(r.montoRetenido).append("\n");
            }
        }
        resultado.setText(sb.toString());

        medios.clear();
        modeloMedios.setRowCount(0);
        refrescar();
    }

    @Override
    public void refrescar() {
        Ui.fill(comboProveedor, ctx.proveedores.listarTodos());
        recargarDocumentos();
        modeloOrdenes.setRowCount(0);
        for (OrdenPago op : ctx.ordenesPago.listarTodas()) {
            modeloOrdenes.addRow(new Object[]{
                    op.numeroOperacion,
                    op.proveedor == null ? "" : op.proveedor.razonSocial,
                    op.calcularBaseImponible(), op.totalRetenciones, op.getTotalACancelar()
            });
        }
    }
}
