package view;

import model.OrdenPago;
import model.Proveedor;
import model.RetencionAplicada;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Analisis Financiero: dos consultas de gestion.
//  - Ranking de acreedores: proveedores ordenados por deuda (saldo) de mayor a menor.
//  - Total de impuestos retenidos, desglosado por tipo de impuesto, sumando todas las
//    retenciones aplicadas en las ordenes de pago emitidas.
public class PanelAnalisisFinanciero extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final DefaultTableModel modeloDeuda;
    private final DefaultTableModel modeloRetenciones;

    public PanelAnalisisFinanciero(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> refrescar());
        barra.add(actualizar);
        add(barra, BorderLayout.NORTH);

        modeloDeuda = new DefaultTableModel(
                new String[]{"#", "Proveedor", "CUIT", "Deuda (saldo)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modeloRetenciones = new DefaultTableModel(
                new String[]{"Impuesto", "Total retenido"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JScrollPane scrollDeuda = new JScrollPane(new JTable(modeloDeuda));
        scrollDeuda.setBorder(BorderFactory.createTitledBorder("Ranking de deuda por proveedor (acreedores)"));
        JScrollPane scrollRet = new JScrollPane(new JTable(modeloRetenciones));
        scrollRet.setBorder(BorderFactory.createTitledBorder("Total de impuestos retenidos por tipo"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollDeuda, scrollRet);
        split.setResizeWeight(0.6);
        add(split, BorderLayout.CENTER);

        refrescar();
    }

    @Override
    public void refrescar() {
        // --- Ranking de deuda (saldo descendente) ---
        modeloDeuda.setRowCount(0);
        List<Proveedor> ranking = new ArrayList<>(ctx.proveedores.listarTodos());
        ranking.sort((a, b) -> Double.compare(b.saldoActual, a.saldoActual));
        int pos = 1;
        for (Proveedor p : ranking) {
            modeloDeuda.addRow(new Object[]{pos++, p.razonSocial, p.cuit, redondear(p.saldoActual)});
        }

        // --- Total de retenciones por impuesto (sumando todas las OP) ---
        modeloRetenciones.setRowCount(0);
        Map<String, Double> totalPorImpuesto = new LinkedHashMap<>();
        for (OrdenPago op : ctx.ordenesPago.listarTodas()) {
            for (RetencionAplicada r : op.retencionesAplicadas) {
                String nombre = r.impuesto == null ? "(sin impuesto)" : r.impuesto.nombre;
                totalPorImpuesto.merge(nombre, r.montoRetenido, Double::sum);
            }
        }
        double totalGeneral = 0;
        for (Map.Entry<String, Double> e : totalPorImpuesto.entrySet()) {
            modeloRetenciones.addRow(new Object[]{e.getKey(), redondear(e.getValue())});
            totalGeneral += e.getValue();
        }
        modeloRetenciones.addRow(new Object[]{"TOTAL", redondear(totalGeneral)});
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
