package view;

import model.ProductoServicio;
import model.Rubro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// ABM de productos/servicios. Cada producto se asocia a un rubro existente.
public class PanelProductos extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JTextField campoNombre = new JTextField(15);
    private final JTextField campoUnidad = new JTextField(6);
    private final JTextField campoIva = new JTextField(4);
    private final JComboBox<Rubro> comboRubro = new JComboBox<>();
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    public PanelProductos(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));
        Ui.render(comboRubro, r -> r.nombre);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setBorder(BorderFactory.createTitledBorder("Producto / Servicio"));
        form.add(new JLabel("Nombre:"));
        form.add(campoNombre);
        form.add(new JLabel("Unidad:"));
        form.add(campoUnidad);
        form.add(new JLabel("% IVA:"));
        form.add(campoIva);
        form.add(new JLabel("Rubro:"));
        form.add(comboRubro);
        JButton agregar = new JButton("Agregar");
        JButton eliminar = new JButton("Eliminar");
        agregar.addActionListener(e -> agregar());
        eliminar.addActionListener(e -> eliminar());
        form.add(agregar);
        form.add(eliminar);
        add(form, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new String[]{"Nombre", "Unidad", "% IVA", "Rubro"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        refrescar();
    }

    private void agregar() {
        if (campoNombre.getText().trim().isEmpty()) {
            Ui.error(this, "El nombre es obligatorio.");
            return;
        }
        double iva;
        try {
            String t = campoIva.getText().trim();
            iva = t.isEmpty() ? 0 : Double.parseDouble(t);
        } catch (NumberFormatException ex) {
            Ui.error(this, "El IVA debe ser un numero.");
            return;
        }
        ProductoServicio p = new ProductoServicio();
        p.nombre = campoNombre.getText().trim();
        p.unidadMedida = campoUnidad.getText().trim();
        p.tipoIva = iva;
        p.rubro = (Rubro) comboRubro.getSelectedItem();
        ctx.productos.add(p);
        campoNombre.setText("");
        campoUnidad.setText("");
        campoIva.setText("");
        refrescar();
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Ui.error(this, "Selecciona un producto.");
            return;
        }
        ctx.productos.remove(fila);
        refrescar();
    }

    @Override
    public void refrescar() {
        Ui.fill(comboRubro, ctx.rubros);
        modeloTabla.setRowCount(0);
        for (ProductoServicio p : ctx.productos) {
            modeloTabla.addRow(new Object[]{
                    p.nombre, p.unidadMedida, p.tipoIva,
                    p.rubro == null ? "" : p.rubro.nombre
            });
        }
    }
}
