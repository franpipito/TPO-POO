package view;

import model.Proveedor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Ventana principal del Sistema de Gestion de Proveedores.
// Permite cargar proveedores en un formulario y verlos en una tabla.
public class VentanaPrincipal extends JFrame {

	// Lista en memoria con los proveedores cargados
	private List<Proveedor> proveedores = new ArrayList<>();
	private int proximoId = 1;

	// Campos del formulario
	private JTextField campoRazonSocial = new JTextField();
	private JTextField campoCuit = new JTextField();
	private JTextField campoDireccion = new JTextField();
	private JTextField campoTelefono = new JTextField();
	private JTextField campoEmail = new JTextField();
	private JTextField campoTopeDeuda = new JTextField();

	// Tabla y su modelo
	private DefaultTableModel modeloTabla;
	private JTable tabla;

	public VentanaPrincipal() {
		setTitle("Sistema de Gestion de Proveedores");
		setSize(700, 450);
		setLocationRelativeTo(null); // centra la ventana
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		// Arriba: formulario de carga
		add(crearPanelFormulario(), BorderLayout.NORTH);

		// Centro: tabla con los proveedores
		add(crearPanelTabla(), BorderLayout.CENTER);

		// Abajo: botones
		add(crearPanelBotones(), BorderLayout.SOUTH);
	}

	// Panel con los campos para cargar un proveedor
	private JPanel crearPanelFormulario() {
		JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
		panel.setBorder(BorderFactory.createTitledBorder("Datos del proveedor"));

		panel.add(new JLabel("Razon social:"));
		panel.add(campoRazonSocial);

		panel.add(new JLabel("CUIT:"));
		panel.add(campoCuit);

		panel.add(new JLabel("Direccion:"));
		panel.add(campoDireccion);

		panel.add(new JLabel("Telefono:"));
		panel.add(campoTelefono);

		panel.add(new JLabel("Email:"));
		panel.add(campoEmail);

		panel.add(new JLabel("Tope de deuda:"));
		panel.add(campoTopeDeuda);

		return panel;
	}

	// Panel con la tabla de proveedores
	private JScrollPane crearPanelTabla() {
		String[] columnas = {"ID", "Razon social", "CUIT", "Direccion", "Telefono", "Email", "Tope deuda"};
		modeloTabla = new DefaultTableModel(columnas, 0);
		tabla = new JTable(modeloTabla);
		return new JScrollPane(tabla);
	}

	// Panel con los botones de accion
	private JPanel crearPanelBotones() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JButton botonAgregar = new JButton("Agregar");
		JButton botonEliminar = new JButton("Eliminar");
		JButton botonLimpiar = new JButton("Limpiar");

		botonAgregar.addActionListener(e -> agregarProveedor());
		botonEliminar.addActionListener(e -> eliminarProveedor());
		botonLimpiar.addActionListener(e -> limpiarFormulario());

		panel.add(botonAgregar);
		panel.add(botonEliminar);
		panel.add(botonLimpiar);

		return panel;
	}

	// Toma los datos del formulario, crea un Proveedor y lo agrega a la tabla
	private void agregarProveedor() {
		// Validacion simple: la razon social no puede estar vacia
		if (campoRazonSocial.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "La razon social es obligatoria.");
			return;
		}

		// El tope de deuda debe ser un numero
		double topeDeuda;
		try {
			String texto = campoTopeDeuda.getText().trim();
			topeDeuda = texto.isEmpty() ? 0 : Double.parseDouble(texto);
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this, "El tope de deuda debe ser un numero.");
			return;
		}

		// Creamos el proveedor y cargamos sus atributos
		Proveedor p = new Proveedor();
		p.idProveedor = proximoId++;
		p.razonSocial = campoRazonSocial.getText().trim();
		p.cuit = campoCuit.getText().trim();
		p.direccion = campoDireccion.getText().trim();
		p.telefono = campoTelefono.getText().trim();
		p.email = campoEmail.getText().trim();
		p.topeDeuda = topeDeuda;

		proveedores.add(p);

		// Agregamos una fila a la tabla
		modeloTabla.addRow(new Object[]{
				p.idProveedor, p.razonSocial, p.cuit, p.direccion,
				p.telefono, p.email, p.topeDeuda
		});

		limpiarFormulario();
	}

	// Elimina el proveedor seleccionado en la tabla
	private void eliminarProveedor() {
		int fila = tabla.getSelectedRow();
		if (fila == -1) {
			JOptionPane.showMessageDialog(this, "Selecciona un proveedor de la tabla.");
			return;
		}
		proveedores.remove(fila);
		modeloTabla.removeRow(fila);
	}

	// Deja los campos del formulario en blanco
	private void limpiarFormulario() {
		campoRazonSocial.setText("");
		campoCuit.setText("");
		campoDireccion.setText("");
		campoTelefono.setText("");
		campoEmail.setText("");
		campoTopeDeuda.setText("");
	}
}
