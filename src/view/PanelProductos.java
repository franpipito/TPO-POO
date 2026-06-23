package view;

import model.PrecioProveedor;
import model.ProductoServicio;
import model.Proveedor;
import model.Rubro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// ABM de productos/servicios. Cada producto se asocia a un rubro existente.
public class PanelProductos extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JTextField campoNombre = new JTextField(15);
    private final JTextField campoUnidad = new JTextField(6);
    // El IVA solo puede tomar las alicuotas fijas de AFIP (2.5%, 5%, 10.5%, 21%, 27%) o
    // Exento (0%). Por eso es un dropdown y no un campo libre: no se puede cargar un IVA invalido.
    private final JComboBox<String> comboIva = new JComboBox<>(
            new String[]{"2.5%", "5%", "10.5%", "21%", "27%", "Exento"});
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
        JButton precios = new JButton("Precios por proveedor");
        agregar.addActionListener(e -> agregar());
        eliminar.addActionListener(e -> eliminar());
        precios.addActionListener(e -> gestionarPrecios());
        form.add(agregar);
        form.add(eliminar);
        form.add(precios);
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
        if ("2.5%".equals(opcion)) return 2.5;
        if ("5%".equals(opcion)) return 5;
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

    // Abre el dialogo para cargar/actualizar el ultimo precio acordado de cada proveedor
    // para el producto seleccionado. Es la base de la Compulsa de Precios.
    private void gestionarPrecios() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Ui.error(this, "Selecciona un producto de la tabla.");
            return;
        }
        if (ctx.proveedores.listarTodos().isEmpty()) {
            Ui.info(this, "Primero carga proveedores en la pestaña Proveedores.");
            return;
        }
        ProductoServicio prod = ctx.productos.get(fila);

        JComboBox<Proveedor> comboProv = new JComboBox<>();
        Ui.render(comboProv, p -> p.razonSocial + " (" + p.cuit + ")");
        Ui.fill(comboProv, ctx.proveedores.listarTodos());
        JTextField campoPrecio = new JTextField(8);

        DefaultTableModel modelo = new DefaultTableModel(new String[]{"Proveedor", "Ultimo precio"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tablaPrecios = new JTable(modelo);
        recargarPrecios(prod, modelo);

        JButton guardar = new JButton("Guardar precio");
        guardar.addActionListener(e -> {
            Proveedor prov = (Proveedor) comboProv.getSelectedItem();
            if (prov == null) {
                Ui.error(this, "Selecciona un proveedor.");
                return;
            }
            double precio;
            try {
                precio = Double.parseDouble(campoPrecio.getText().trim());
            } catch (NumberFormatException ex) {
                Ui.error(this, "El precio debe ser numerico.");
                return;
            }
            if (precio <= 0) {
                Ui.error(this, "El precio debe ser mayor a 0.");
                return;
            }
            registrarPrecio(prod, prov, precio);
            campoPrecio.setText("");
            recargarPrecios(prod, modelo);
        });

        JPanel carga = new JPanel(new FlowLayout(FlowLayout.LEFT));
        carga.add(new JLabel("Proveedor:"));
        carga.add(comboProv);
        carga.add(new JLabel("Precio:"));
        carga.add(campoPrecio);
        carga.add(guardar);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(carga, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(tablaPrecios);
        scroll.setPreferredSize(new Dimension(360, 150));
        panel.add(scroll, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, panel,
                "Precios por proveedor - " + prod.nombre, JOptionPane.PLAIN_MESSAGE);
    }

    // Registra el precio del proveedor para el producto; si ya existia, lo actualiza.
    private void registrarPrecio(ProductoServicio prod, Proveedor prov, double precio) {
        for (PrecioProveedor pp : prod.precios) {
            if (pp.proveedor == prov) {
                pp.ultimoPrecioAcordado = precio; // actualiza el ultimo precio negociado
                return;
            }
        }
        PrecioProveedor pp = new PrecioProveedor();
        pp.producto = prod;
        pp.proveedor = prov;
        pp.ultimoPrecioAcordado = precio;
        prod.precios.add(pp);
        prov.precios.add(pp);
    }

    private void recargarPrecios(ProductoServicio prod, DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (PrecioProveedor pp : prod.precios) {
            modelo.addRow(new Object[]{
                    pp.proveedor == null ? "" : pp.proveedor.razonSocial,
                    pp.ultimoPrecioAcordado
            });
        }
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
