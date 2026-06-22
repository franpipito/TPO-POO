package view;

import model.Rubro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// ABM simple de rubros (dato maestro en memoria).
public class PanelRubros extends JPanel implements Refrescable {

    private final AppContext ctx;
    private final JTextField campoDescripcion = new JTextField(20);
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    public PanelRubros(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(5, 5));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setBorder(BorderFactory.createTitledBorder("Rubro"));
        form.add(new JLabel("Descripcion:"));
        form.add(campoDescripcion);
        JButton agregar = new JButton("Agregar");
        JButton eliminar = new JButton("Eliminar");
        agregar.addActionListener(e -> agregar());
        eliminar.addActionListener(e -> eliminar());
        form.add(agregar);
        form.add(eliminar);
        add(form, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new String[]{"ID", "Descripcion"}, 0) {
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
        if (campoDescripcion.getText().trim().isEmpty()) {
            Ui.error(this, "La descripcion es obligatoria.");
            return;
        }
        Rubro r = new Rubro();
        r.idRubro = ctx.proximoIdRubro++;
        r.descripcion = campoDescripcion.getText().trim();
        ctx.rubros.add(r);
        campoDescripcion.setText("");
        refrescar();
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            Ui.error(this, "Selecciona un rubro.");
            return;
        }
        ctx.rubros.remove(fila);
        refrescar();
    }

    @Override
    public void refrescar() {
        modeloTabla.setRowCount(0);
        for (Rubro r : ctx.rubros) {
            modeloTabla.addRow(new Object[]{r.idRubro, r.descripcion});
        }
    }
}
