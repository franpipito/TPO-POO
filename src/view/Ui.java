package view;

import javax.swing.*;
import java.awt.Component;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;
import java.util.regex.Pattern;

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

    // Patrón simple de email: algo@algo.dominio (sin espacios).
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Valida un CUIT argentino. Acepta el texto con o sin guiones/espacios; exige 11
     * dígitos y que el ÚLTIMO sea el dígito verificador correcto (algoritmo módulo 11).
     * Así no se aceptan números inventados que "parezcan" un CUIT.
     */
    public static boolean esCuitValido(String cuit) {
        if (cuit == null) {
            return false;
        }
        String d = cuit.replaceAll("[^0-9]", ""); // deja solo los dígitos
        if (d.length() != 11) {
            return false;
        }
        int[] pesos = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            suma += (d.charAt(i) - '0') * pesos[i];
        }
        int verificador = 11 - (suma % 11);
        if (verificador == 11) {
            verificador = 0;
        }
        if (verificador == 10) {
            return false; // CUIT no estándar
        }
        return verificador == (d.charAt(10) - '0');
    }

    // Email válido según el patrón simple (no vacío y con forma nombre@dominio.ext).
    public static boolean esEmailValido(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    /**
     * Teléfono válido: solo dígitos y símbolos telefónicos (+ - ( ) y espacios) y al
     * menos 6 dígitos. Rechaza textos como "a" o letras sueltas.
     */
    public static boolean esTelefonoValido(String tel) {
        if (tel == null) {
            return false;
        }
        String t = tel.trim();
        if (!t.matches("[0-9+\\-() ]+")) {
            return false;
        }
        return t.replaceAll("[^0-9]", "").length() >= 6;
    }
}
