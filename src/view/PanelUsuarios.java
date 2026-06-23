package view;

import model.Rol;
import model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Gestión de usuarios (solo accesible para Supervisor). Permite crear usuarios,
 * modificarlos (contraseña y rol), darlos de baja (baja LÓGICA: quedan inactivos) y
 * reactivarlos. La baja no borra al usuario, solo cambia su estado.
 */
public class PanelUsuarios extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JTextField campoUsuario = new JTextField(12);
    private final JPasswordField campoClave = new JPasswordField(12);
    private final JComboBox<Rol> comboRol = new JComboBox<>();
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    public PanelUsuarios(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));
        Ui.render(comboRol, r -> r.nombre);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setBorder(BorderFactory.createTitledBorder("Usuario"));
        form.add(new JLabel("Usuario:"));
        form.add(campoUsuario);
        form.add(new JLabel("Contraseña:"));
        form.add(campoClave);
        form.add(new JLabel("Rol:"));
        form.add(comboRol);
        add(form, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new String[]{"ID", "Usuario", "Rol", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        // Al seleccionar una fila, se cargan nombre y rol en el formulario para editarlos
        // (la contraseña no se muestra; se completa solo si se quiere cambiar).
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(crearBotones(), BorderLayout.SOUTH);
        refrescar();
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton agregar = new JButton("Agregar");
        JButton modificar = new JButton("Modificar");
        JButton baja = new JButton("Dar de baja");
        JButton reactivar = new JButton("Reactivar");
        agregar.addActionListener(e -> agregar());
        modificar.addActionListener(e -> modificar());
        baja.addActionListener(e -> darDeBaja());
        reactivar.addActionListener(e -> reactivar());
        panel.add(agregar);
        panel.add(modificar);
        panel.add(baja);
        panel.add(reactivar);
        return panel;
    }

    /** Crea un usuario nuevo: valida usuario, clave y rol. El controller exige nombre único. */
    private void agregar() {
        String usuario = campoUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());
        Rol rol = (Rol) comboRol.getSelectedItem();
        if (usuario.isEmpty() || clave.isEmpty()) {
            Ui.error(this, "Usuario y contraseña son obligatorios.");
            return;
        }
        if (usuario.contains(" ")) {
            Ui.error(this, "El nombre de usuario no puede contener espacios.");
            return;
        }
        if (rol == null) {
            Ui.error(this, "Selecciona un rol.");
            return;
        }
        Usuario u = new Usuario();
        u.nombreUsuario = usuario;
        u.contrasenia = clave;
        u.rol = rol;
        try {
            ctx.usuarios.registrar(u);
        } catch (IllegalArgumentException ex) {
            Ui.error(this, ex.getMessage()); // nombre de usuario duplicado
            return;
        }
        limpiar();
        refrescar();
    }

    /**
     * Modifica el usuario seleccionado: cambia el nombre de usuario, el rol y, si se
     * escribió una clave nueva, también la contraseña. El nombre debe seguir siendo
     * único; si choca con otro usuario, el controller lo rechaza.
     */
    private void modificar() {
        Usuario u = usuarioSeleccionado();
        if (u == null) return;
        String nuevoNombre = campoUsuario.getText().trim();
        if (nuevoNombre.isEmpty()) {
            Ui.error(this, "El nombre de usuario es obligatorio.");
            return;
        }
        if (nuevoNombre.contains(" ")) {
            Ui.error(this, "El nombre de usuario no puede contener espacios.");
            return;
        }
        String nuevaClave = new String(campoClave.getPassword());
        Rol nuevoRol = (Rol) comboRol.getSelectedItem();
        try {
            ctx.usuarios.modificar(u, nuevoNombre, nuevaClave, nuevoRol);
        } catch (IllegalArgumentException ex) {
            Ui.error(this, ex.getMessage()); // nombre de usuario duplicado
            return;
        }
        limpiar();
        refrescar();
    }

    // Carga el nombre y el rol del usuario seleccionado en el formulario (la contraseña
    // se deja vacía: solo se cambia si se escribe una nueva).
    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) return;
        Usuario u = ctx.usuarios.listarTodos().get(fila);
        campoUsuario.setText(u.nombreUsuario);
        campoClave.setText("");
        comboRol.setSelectedItem(u.rol);
    }

    // Baja lógica del usuario seleccionado (queda inactivo, no se borra).
    private void darDeBaja() {
        Usuario u = usuarioSeleccionado();
        if (u == null) return;
        ctx.usuarios.darDeBaja(u);
        refrescar();
    }

    // Reactiva el usuario seleccionado.
    private void reactivar() {
        Usuario u = usuarioSeleccionado();
        if (u == null) return;
        ctx.usuarios.reactivar(u);
        refrescar();
    }

    // Devuelve el usuario de la fila seleccionada (o null avisando si no hay selección).
    private Usuario usuarioSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Ui.error(this, "Selecciona un usuario de la tabla.");
            return null;
        }
        return ctx.usuarios.listarTodos().get(fila);
    }

    private void limpiar() {
        campoUsuario.setText("");
        campoClave.setText("");
    }

    @Override
    public void refrescar() {
        Ui.fill(comboRol, ctx.roles.listarTodos());
        modeloTabla.setRowCount(0);
        List<Usuario> lista = ctx.usuarios.listarTodos();
        for (Usuario u : lista) {
            modeloTabla.addRow(new Object[]{
                    u.idUsuario,
                    u.nombreUsuario,
                    u.rol == null ? "" : u.rol.nombre,
                    u.activo ? "Activo" : "Inactivo"
            });
        }
    }
}
