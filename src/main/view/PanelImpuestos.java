package view;

import model.Impuestos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// ABM del catálogo de impuestos (IVA, IIBB, Ganancias, etc.) con su porcentaje base
// y mínimo no imponible. Estos impuestos luego se asignan a los proveedores y son la
// base del cálculo de retenciones en las órdenes de pago.
public class PanelImpuestos extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JTextField campoNombre = new JTextField(10);
    private final JTextField campoTipo = new JTextField(10);
    private final JTextField campoPorcentaje = new JTextField(5);
    private final JTextField campoMinimo = new JTextField(8);
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    public PanelImpuestos(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setBorder(BorderFactory.createTitledBorder("Impuesto"));
        form.add(new JLabel("Nombre:"));
        form.add(campoNombre);
        form.add(new JLabel("Tipo:"));
        form.add(campoTipo);
        form.add(new JLabel("% base:"));
        form.add(campoPorcentaje);
        form.add(new JLabel("Minimo no imponible:"));
        form.add(campoMinimo);
        JButton agregar = new JButton("Agregar");
        JButton eliminar = new JButton("Eliminar");
        agregar.addActionListener(e -> agregar());
        eliminar.addActionListener(e -> eliminar());
        form.add(agregar);
        form.add(eliminar);
        add(form, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(
                new String[]{"Nombre", "Tipo", "% base", "Minimo no imponible"}, 0) {
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
        double porcentaje;
        double minimo;
        try {
            porcentaje = Double.parseDouble(campoPorcentaje.getText().trim());
            String m = campoMinimo.getText().trim();
            minimo = m.isEmpty() ? 0 : Double.parseDouble(m);
        } catch (NumberFormatException ex) {
            Ui.error(this, "El porcentaje y el minimo deben ser numeros.");
            return;
        }
        Impuestos imp = new Impuestos();
        imp.nombre = campoNombre.getText().trim();
        imp.tipoImpuesto = campoTipo.getText().trim();
        imp.porcentajeBase = porcentaje;
        imp.minimoNoImponible = minimo;
        if (!imp.esValido()) {
            Ui.error(this, "El impuesto no es valido: requiere nombre y porcentaje base mayor a 0.");
            return;
        }
        ctx.impuestos.add(imp);
        campoNombre.setText("");
        campoTipo.setText("");
        campoPorcentaje.setText("");
        campoMinimo.setText("");
        refrescar();
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Ui.error(this, "Selecciona un impuesto.");
            return;
        }
        ctx.impuestos.remove(fila);
        refrescar();
    }

    @Override
    public void refrescar() {
        modeloTabla.setRowCount(0);
        for (Impuestos imp : ctx.impuestos) {
            modeloTabla.addRow(new Object[]{imp.nombre, imp.tipoImpuesto, imp.porcentajeBase, imp.minimoNoImponible});
        }
    }
}
