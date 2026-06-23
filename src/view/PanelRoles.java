package view;

import model.Rol;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Gestión de roles (solo accesible para Supervisor). Permite crear roles, modificar
 * sus permisos y eliminarlos. Un rol NO se puede eliminar si algún usuario lo tiene
 * asignado (esa regla la valida {@code RolController}).
 *
 * <p>Los permisos se eligen tildando de una lista fija ({@code Rol.PERMISOS_DISPONIBLES}),
 * lo que evita errores de tipeo y mantiene controlado el conjunto de permisos.</p>
 */
public class PanelRoles extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JTextField campoNombre = new JTextField(15);
    // Un checkbox por cada permiso disponible del sistema.
    private final JCheckBox[] checksPermisos;
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    public PanelRoles(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setBorder(BorderFactory.createTitledBorder("Rol"));
        form.add(new JLabel("Nombre:"));
        form.add(campoNombre);
        form.add(new JLabel("Permisos:"));
        // Crea dinámicamente un checkbox por permiso disponible.
        checksPermisos = new JCheckBox[Rol.PERMISOS_DISPONIBLES.length];
        for (int i = 0; i < Rol.PERMISOS_DISPONIBLES.length; i++) {
            checksPermisos[i] = new JCheckBox(Rol.PERMISOS_DISPONIBLES[i]);
            form.add(checksPermisos[i]);
        }
        add(form, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre", "Permisos"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(crearBotones(), BorderLayout.SOUTH);
        refrescar();
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton agregar = new JButton("Agregar");
        JButton modificar = new JButton("Modificar");
        JButton eliminar = new JButton("Eliminar");
        agregar.addActionListener(e -> agregar());
        modificar.addActionListener(e -> modificar());
        eliminar.addActionListener(e -> eliminar());
        panel.add(agregar);
        panel.add(modificar);
        panel.add(eliminar);
        return panel;
    }

    /** Crea un rol nuevo con el nombre y los permisos tildados. El nombre es único. */
    private void agregar() {
        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            Ui.error(this, "El nombre del rol es obligatorio.");
            return;
        }
        Rol r = new Rol();
        r.nombre = nombre;
        aplicarPermisosTildados(r);
        try {
            ctx.roles.crear(r);
        } catch (IllegalArgumentException ex) {
            Ui.error(this, ex.getMessage()); // nombre duplicado
            return;
        }
        limpiar();
        refrescar();
    }

    /** Modifica los permisos del rol seleccionado según los checkboxes tildados. */
    private void modificar() {
        Rol r = rolSeleccionado();
        if (r == null) return;
        aplicarPermisosTildados(r);
        refrescar();
    }

    /** Elimina el rol seleccionado; el controller lo bloquea si está en uso. */
    private void eliminar() {
        Rol r = rolSeleccionado();
        if (r == null) return;
        try {
            ctx.roles.eliminar(r);
        } catch (IllegalStateException ex) {
            Ui.error(this, ex.getMessage()); // rol asignado a algún usuario
            return;
        }
        refrescar();
    }

    // Reemplaza la lista de permisos del rol con los que estén tildados en el form.
    private void aplicarPermisosTildados(Rol r) {
        r.permisos.clear();
        for (int i = 0; i < checksPermisos.length; i++) {
            if (checksPermisos[i].isSelected()) {
                r.permisos.add(Rol.PERMISOS_DISPONIBLES[i]);
            }
        }
    }

    // Devuelve el rol de la fila seleccionada (o null avisando si no hay selección).
    private Rol rolSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Ui.error(this, "Selecciona un rol de la tabla.");
            return null;
        }
        return ctx.roles.listarTodos().get(fila);
    }

    private void limpiar() {
        campoNombre.setText("");
        for (JCheckBox c : checksPermisos) {
            c.setSelected(false);
        }
    }

    @Override
    public void refrescar() {
        modeloTabla.setRowCount(0);
        for (Rol r : ctx.roles.listarTodos()) {
            // Muestra los permisos separados por coma (o "-" si no tiene ninguno).
            String permisos = r.permisos.isEmpty() ? "-" : String.join(", ", r.permisos);
            modeloTabla.addRow(new Object[]{r.idRol, r.nombre, permisos});
        }
    }
}
