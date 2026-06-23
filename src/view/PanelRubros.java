package view;

import model.Rubro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// ABM simple de rubros (dato maestro en memoria). El nombre identifica al rubro y no
// puede repetirse; la descripcion es opcional.
public class PanelRubros extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JTextField campoNombre = new JTextField(15);
    private final JTextField campoDescripcion = new JTextField(20);
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    public PanelRubros(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setBorder(BorderFactory.createTitledBorder("Rubro"));
        form.add(new JLabel("Nombre:"));
        form.add(campoNombre);
        form.add(new JLabel("Descripcion (opcional):"));
        form.add(campoDescripcion);
        JButton agregar = new JButton("Agregar");
        JButton eliminar = new JButton("Eliminar");
        agregar.addActionListener(e -> agregar());
        eliminar.addActionListener(e -> eliminar());
        form.add(agregar);
        form.add(eliminar);
        add(form, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre", "Descripcion"}, 0) {
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
        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            Ui.error(this, "El nombre es obligatorio.");
            return;
        }
        if (existeNombre(nombre)) {
            Ui.error(this, "Ya existe un rubro con el nombre " + nombre + ".");
            return;
        }
        Rubro r = new Rubro();
        r.idRubro = ctx.proximoIdRubro++;
        r.nombre = nombre;
        // La descripcion es opcional: si queda vacia se guarda como null.
        String descripcion = campoDescripcion.getText().trim();
        r.descripcion = descripcion.isEmpty() ? null : descripcion;
        ctx.rubros.add(r);
        campoNombre.setText("");
        campoDescripcion.setText("");
        refrescar();
    }

    // True si ya hay un rubro con ese nombre (comparacion sin distinguir mayusculas).
    private boolean existeNombre(String nombre) {
        for (Rubro r : ctx.rubros) {
            if (r.nombre != null && r.nombre.equalsIgnoreCase(nombre)) return true;
        }
        return false;
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Ui.error(this, "Selecciona un rubro.");
            return;
        }
        Rubro r = ctx.rubros.get(fila);
        // No se permite borrar un rubro que tenga productos asociados, para no dejar
        // productos sin rubro valido.
        if (estaEnUso(r)) {
            Ui.error(this, "No se puede eliminar el rubro porque hay productos asociados.");
            return;
        }
        ctx.rubros.remove(fila);
        refrescar();
    }

    // True si algun producto del catalogo esta asociado a ese rubro.
    private boolean estaEnUso(Rubro r) {
        for (model.ProductoServicio p : ctx.productos) {
            if (p.rubro == r) return true;
        }
        return false;
    }

    @Override
    public void refrescar() {
        modeloTabla.setRowCount(0);
        for (Rubro r : ctx.rubros) {
            modeloTabla.addRow(new Object[]{r.idRubro, r.nombre, r.descripcion == null ? "" : r.descripcion});
        }
    }
}
