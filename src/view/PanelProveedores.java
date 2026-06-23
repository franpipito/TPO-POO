package view;

import model.CondicionIva;
import model.Impuestos;
import model.Proveedor;
import model.Rubro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

// ABM de proveedores. Delega en ProveedorController (CUIT único, alta/baja) y permite
// modificarlos, asignar los impuestos a los que están sujetos a retención y los rubros
// que comercializan (un proveedor puede pertenecer a uno o más rubros).
public class PanelProveedores extends JPanel implements Refrescable {

    private final AppContext ctx;

    private final JTextField campoRazonSocial = new JTextField();
    private final JTextField campoNombreFantasia = new JTextField();
    private final JTextField campoCuit = new JTextField();
    private final JComboBox<String> comboCondicionIva = new JComboBox<>(CondicionIva.OPCIONES);
    private final JTextField campoDireccion = new JTextField();
    private final JTextField campoTelefono = new JTextField();
    private final JTextField campoEmail = new JTextField();
    private final JTextField campoIngresosBrutos = new JTextField();
    private final JTextField campoFechaInicio = new JTextField();
    private final JTextField campoTopeDeuda = new JTextField();

    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    public PanelProveedores(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));

        add(crearFormulario(), BorderLayout.NORTH);

        String[] columnas = {"ID", "Razon social", "CUIT", "Cond. IVA", "Tope deuda",
                "Saldo actual", "Rubros", "Impuestos", "Certificados"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        // Al seleccionar una fila se cargan los datos del proveedor en el formulario.
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(crearBotones(), BorderLayout.SOUTH);
        refrescar();
    }

    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridLayout(5, 4, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del proveedor"));
        panel.add(new JLabel("Razon social:"));
        panel.add(campoRazonSocial);
        panel.add(new JLabel("Nombre fantasia:"));
        panel.add(campoNombreFantasia);
        panel.add(new JLabel("CUIT:"));
        panel.add(campoCuit);
        panel.add(new JLabel("Responsabilidad IVA:"));
        panel.add(comboCondicionIva);
        panel.add(new JLabel("Direccion:"));
        panel.add(campoDireccion);
        panel.add(new JLabel("Telefono:"));
        panel.add(campoTelefono);
        panel.add(new JLabel("Email:"));
        panel.add(campoEmail);
        panel.add(new JLabel("Ingresos brutos:"));
        panel.add(campoIngresosBrutos);
        panel.add(new JLabel("Inicio actividades (aaaa-mm-dd):"));
        panel.add(campoFechaInicio);
        panel.add(new JLabel("Tope de deuda:"));
        panel.add(campoTopeDeuda);
        return panel;
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton agregar = new JButton("Agregar");
        JButton modificar = new JButton("Modificar");
        JButton eliminar = new JButton("Eliminar");
        JButton asignarImp = new JButton("Asignar impuestos");
        JButton asignarRubros = new JButton("Asignar rubros");
        JButton limpiar = new JButton("Limpiar");
        agregar.addActionListener(e -> agregar());
        modificar.addActionListener(e -> modificar());
        eliminar.addActionListener(e -> eliminar());
        asignarImp.addActionListener(e -> asignarImpuestos());
        asignarRubros.addActionListener(e -> asignarRubros());
        limpiar.addActionListener(e -> limpiar());
        panel.add(agregar);
        panel.add(modificar);
        panel.add(eliminar);
        panel.add(asignarImp);
        panel.add(asignarRubros);
        panel.add(limpiar);
        return panel;
    }

    // Valida los campos del formulario. Devuelve la fecha de inicio parseada (o null si
    // está vacía). Si algo no es válido, muestra el error y devuelve un resultado inválido.
    private Date validarFormulario() {
        String razonSocial = campoRazonSocial.getText().trim();
        String cuit = campoCuit.getText().trim();
        String email = campoEmail.getText().trim();
        String telefono = campoTelefono.getText().trim();

        if (razonSocial.isEmpty()) {
            Ui.error(this, "La razon social es obligatoria.");
            return INVALIDO;
        }
        if (!Ui.esCuitValido(cuit)) {
            Ui.error(this, "El CUIT no es valido. Debe tener 11 digitos con digito verificador correcto "
                    + "(ej. 20-12345678-6).");
            return INVALIDO;
        }
        if (!email.isEmpty() && !Ui.esEmailValido(email)) {
            Ui.error(this, "El email no tiene un formato valido (ej. nombre@dominio.com).");
            return INVALIDO;
        }
        if (!telefono.isEmpty() && !Ui.esTelefonoValido(telefono)) {
            Ui.error(this, "El telefono solo puede tener numeros y simbolos (+ - ( )), con al menos 6 digitos.");
            return INVALIDO;
        }
        double tope;
        try {
            String t = campoTopeDeuda.getText().trim();
            tope = t.isEmpty() ? 0 : Double.parseDouble(t);
        } catch (NumberFormatException ex) {
            Ui.error(this, "El tope de deuda debe ser un numero.");
            return INVALIDO;
        }
        if (tope < 0) {
            Ui.error(this, "El tope de deuda no puede ser negativo.");
            return INVALIDO;
        }
        Date fechaInicio;
        try {
            fechaInicio = Ui.parseFecha(campoFechaInicio.getText());
        } catch (ParseException ex) {
            Ui.error(this, "La fecha de inicio de actividades debe tener formato aaaa-mm-dd.");
            return INVALIDO;
        }
        return fechaInicio;
    }

    // Centinela para distinguir "formulario inválido" de "fecha vacía (null)".
    private static final Date INVALIDO = new Date(Long.MIN_VALUE);

    // Vuelca los datos del formulario al proveedor (campos comunes a alta y modificación).
    private void volcarDatos(Proveedor p, Date fechaInicio) {
        p.razonSocial = campoRazonSocial.getText().trim();
        p.nombreFantasia = campoNombreFantasia.getText().trim();
        p.cuit = campoCuit.getText().trim();
        CondicionIva ci = new CondicionIva();
        ci.descripcion = (String) comboCondicionIva.getSelectedItem();
        p.condicionIva = ci;
        p.direccion = campoDireccion.getText().trim();
        p.telefono = campoTelefono.getText().trim();
        p.email = campoEmail.getText().trim();
        p.ingresosBrutos = campoIngresosBrutos.getText().trim();
        p.fechaInicioActividades = fechaInicio;
        String t = campoTopeDeuda.getText().trim();
        p.topeDeuda = t.isEmpty() ? 0 : Double.parseDouble(t);
    }

    private void agregar() {
        Date fechaInicio = validarFormulario();
        if (fechaInicio == INVALIDO) return;

        Proveedor p = new Proveedor();
        volcarDatos(p, fechaInicio);
        try {
            ctx.proveedores.registrar(p);
        } catch (IllegalArgumentException ex) {
            Ui.error(this, ex.getMessage());
            return;
        }
        limpiar();
        refrescar();
    }

    // Modifica el proveedor seleccionado. El CUIT puede cambiar pero debe seguir siendo
    // único (no puede coincidir con el de otro proveedor).
    private void modificar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Ui.error(this, "Selecciona un proveedor de la tabla.");
            return;
        }
        Proveedor p = ctx.proveedores.listarTodos().get(fila);
        Date fechaInicio = validarFormulario();
        if (fechaInicio == INVALIDO) return;

        String nuevoCuit = campoCuit.getText().trim();
        Proveedor otro = ctx.proveedores.buscarPorCuit(nuevoCuit);
        if (otro != null && otro != p) {
            Ui.error(this, "Ya existe otro proveedor con el CUIT " + nuevoCuit + ".");
            return;
        }
        volcarDatos(p, fechaInicio);
        limpiar();
        refrescar();
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Ui.error(this, "Selecciona un proveedor de la tabla.");
            return;
        }
        Proveedor p = ctx.proveedores.listarTodos().get(fila);
        ctx.proveedores.eliminar(p.idProveedor);
        limpiar();
        refrescar();
    }

    // Permite tildar, de la lista maestra de impuestos, a cuáles está sujeto el proveedor.
    private void asignarImpuestos() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Ui.error(this, "Selecciona un proveedor de la tabla.");
            return;
        }
        if (ctx.impuestos.isEmpty()) {
            Ui.info(this, "Primero carga impuestos en la pestaña Catalogo.");
            return;
        }
        Proveedor p = ctx.proveedores.listarTodos().get(fila);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        JCheckBox[] checks = new JCheckBox[ctx.impuestos.size()];
        for (int i = 0; i < ctx.impuestos.size(); i++) {
            Impuestos imp = ctx.impuestos.get(i);
            checks[i] = new JCheckBox(imp.nombre + " (" + imp.porcentajeBase + "%)", p.impuestos.contains(imp));
            panel.add(checks[i]);
        }

        int op = JOptionPane.showConfirmDialog(this, panel,
                "Impuestos de " + p.razonSocial, JOptionPane.OK_CANCEL_OPTION);
        if (op == JOptionPane.OK_OPTION) {
            p.impuestos.clear();
            for (int i = 0; i < checks.length; i++) {
                if (checks[i].isSelected()) {
                    p.impuestos.add(ctx.impuestos.get(i));
                }
            }
            refrescar();
        }
    }

    // Permite tildar, de la lista maestra de rubros, los que comercializa el proveedor.
    private void asignarRubros() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Ui.error(this, "Selecciona un proveedor de la tabla.");
            return;
        }
        if (ctx.rubros.isEmpty()) {
            Ui.info(this, "Primero carga rubros en la pestaña Catalogo.");
            return;
        }
        Proveedor p = ctx.proveedores.listarTodos().get(fila);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        JCheckBox[] checks = new JCheckBox[ctx.rubros.size()];
        for (int i = 0; i < ctx.rubros.size(); i++) {
            Rubro r = ctx.rubros.get(i);
            checks[i] = new JCheckBox(r.nombre, p.rubros.contains(r));
            panel.add(checks[i]);
        }

        int op = JOptionPane.showConfirmDialog(this, panel,
                "Rubros de " + p.razonSocial, JOptionPane.OK_CANCEL_OPTION);
        if (op == JOptionPane.OK_OPTION) {
            p.rubros.clear();
            for (int i = 0; i < checks.length; i++) {
                if (checks[i].isSelected()) {
                    p.rubros.add(ctx.rubros.get(i));
                }
            }
            refrescar();
        }
    }

    // Carga el proveedor seleccionado en el formulario para poder modificarlo.
    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) return;
        Proveedor p = ctx.proveedores.listarTodos().get(fila);
        campoRazonSocial.setText(p.razonSocial);
        campoNombreFantasia.setText(p.nombreFantasia);
        campoCuit.setText(p.cuit);
        comboCondicionIva.setSelectedItem(p.condicionIva == null ? null : p.condicionIva.descripcion);
        campoDireccion.setText(p.direccion);
        campoTelefono.setText(p.telefono);
        campoEmail.setText(p.email);
        campoIngresosBrutos.setText(p.ingresosBrutos);
        campoFechaInicio.setText(Ui.fmt(p.fechaInicioActividades));
        campoTopeDeuda.setText(String.valueOf(p.topeDeuda));
    }

    private void limpiar() {
        for (JTextField c : new JTextField[]{campoRazonSocial, campoNombreFantasia, campoCuit,
                campoDireccion, campoTelefono, campoEmail, campoIngresosBrutos, campoFechaInicio, campoTopeDeuda}) {
            c.setText("");
        }
        comboCondicionIva.setSelectedIndex(0);
    }

    @Override
    public void refrescar() {
        modeloTabla.setRowCount(0);
        List<Proveedor> lista = ctx.proveedores.listarTodos();
        for (Proveedor p : lista) {
            modeloTabla.addRow(new Object[]{
                    p.idProveedor, p.razonSocial, p.cuit,
                    p.condicionIva == null ? "" : p.condicionIva.descripcion,
                    p.topeDeuda, p.saldoActual,
                    p.rubros.size(), p.impuestos.size(), p.certificados.size()
            });
        }
    }
}
