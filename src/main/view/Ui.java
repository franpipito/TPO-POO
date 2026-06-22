package view;

import javax.swing.*;
import java.awt.Component;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;

// Utilidades comunes de la interfaz: combos con texto de presentación,
// parseo/formato de fechas y diálogos de mensajes.
public final class Ui {

    private Ui() {}

    private static final SimpleDateFormat FMT = new SimpleDateFormat("yyyy-MM-dd");

    // Define cómo se muestra cada elemento de un combo (los modelos no tienen toString).
    public static <T> void render(JComboBox<T> combo, Function<T, String> texto) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "" : texto.apply((T) value));
                return this;
            }
        });
    }

    // Igual que render(), pero para listas de selección múltiple.
    public static <T> void renderList(JList<T> lista, Function<T, String> texto) {
        lista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "" : texto.apply((T) value));
                return this;
            }
        });
    }

    // Reemplaza el contenido de un combo con la lista dada.
    public static <T> void fill(JComboBox<T> combo, List<T> items) {
        combo.setModel(new DefaultComboBoxModel<>(new Vector<>(items)));
    }

    // Parsea una fecha en formato yyyy-MM-dd. Devuelve null si el texto está vacío.
    public static Date parseFecha(String texto) throws ParseException {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        return FMT.parse(texto.trim());
    }

    public static String fmt(Date fecha) {
        return fecha == null ? "" : FMT.format(fecha);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Informacion", JOptionPane.INFORMATION_MESSAGE);
    }
}
