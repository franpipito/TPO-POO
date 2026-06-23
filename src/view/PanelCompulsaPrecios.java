package view;

import model.PrecioProveedor;
import model.ProductoServicio;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Compulsa de Precios (analisis de costos): se elige un producto y se ven todos los
// proveedores que lo comercializan con su ultimo precio acordado, ordenados de menor a
// mayor para facilitar la decision de compra. El precio mas bajo se marca como "(menor)".
public class PanelCompulsaPrecios extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JComboBox<ProductoServicio> comboProducto = new JComboBox<>();
    private final DefaultTableModel modeloTabla;

    public PanelCompulsaPrecios(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));
        Ui.render(comboProducto, p -> p.nombre);
        comboProducto.addActionListener(e -> refrescarTabla());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.setBorder(BorderFactory.createTitledBorder("Compulsa de precios"));
        barra.add(new JLabel("Producto:"));
        barra.add(comboProducto);
        add(barra, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(
                new String[]{"Proveedor", "CUIT", "Ultimo precio acordado", ""}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JScrollPane scroll = new JScrollPane(new JTable(modeloTabla));
        scroll.setBorder(BorderFactory.createTitledBorder("Proveedores que comercializan el producto"));
        add(scroll, BorderLayout.CENTER);

        refrescar();
    }

    private void refrescarTabla() {
        modeloTabla.setRowCount(0);
        ProductoServicio prod = (ProductoServicio) comboProducto.getSelectedItem();
        if (prod == null) {
            return;
        }
        // Copia ordenada por precio ascendente.
        java.util.List<PrecioProveedor> precios = new java.util.ArrayList<>(prod.precios);
        precios.sort(java.util.Comparator.comparingDouble(pp -> pp.ultimoPrecioAcordado));

        for (int i = 0; i < precios.size(); i++) {
            PrecioProveedor pp = precios.get(i);
            modeloTabla.addRow(new Object[]{
                    pp.proveedor == null ? "" : pp.proveedor.razonSocial,
                    pp.proveedor == null ? "" : pp.proveedor.cuit,
                    pp.ultimoPrecioAcordado,
                    i == 0 ? "(menor)" : ""
            });
        }
    }

    @Override
    public void refrescar() {
        Ui.fill(comboProducto, ctx.productos);
        refrescarTabla();
    }
}
