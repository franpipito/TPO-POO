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
    // El IVA solo puede tomar valores fijos: 21%, 10.5%, 27% o Exento (0%). Por eso es
    // un dropdown y no un campo libre: el usuario no puede cargar un IVA inválido.
    private final JComboBox<String> comboIva = new JComboBox<>(
            new String[]{"21%", "10.5%", "27%", "Exento"});
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
        form.add(new JLabel("IVA:"));
        form.add(comboIva);
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
        // Un producto debe pertenecer a un rubro existente.
        Rubro rubro = (Rubro) comboRubro.getSelectedItem();
        if (rubro == null) {
            Ui.error(this, "Selecciona un rubro. Si no hay, carga uno en Catalogo > Rubros.");
            return;
        }
        ProductoServicio p = new ProductoServicio();
        p.nombre = campoNombre.getText().trim();
        p.unidadMedida = campoUnidad.getText().trim();
        // El IVA sale del dropdown, así que siempre es uno de los valores válidos.
        p.tipoIva = ivaSeleccionado();
        p.rubro = rubro;
        ctx.productos.add(p);
        campoNombre.setText("");
        campoUnidad.setText("");
        refrescar();
    }

    // Convierte la opción elegida del dropdown al porcentaje numérico que guarda el
    // modelo. "Exento" se representa como 0% (no se le retiene/aplica IVA).
    private double ivaSeleccionado() {
        String opcion = (String) comboIva.getSelectedItem();
        if ("10.5%".equals(opcion)) return 10.5;
        if ("27%".equals(opcion)) return 27;
        if ("Exento".equals(opcion)) return 0;
        return 21; // "21%"
    }

    // Da formato al IVA para mostrarlo en la tabla: "Exento" si es 0, o el % si no.
    private static String formatIva(double iva) {
        if (iva == 0) return "Exento";
        if (iva == Math.floor(iva)) return (int) iva + "%"; // 21, 27 -> sin decimales
        return iva + "%"; // 10.5%
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
                    p.nombre, p.unidadMedida, formatIva(p.tipoIva),
                    p.rubro == null ? "" : p.rubro.nombre
            });
        }
    }
}
