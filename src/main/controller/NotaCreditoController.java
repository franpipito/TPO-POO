package controller;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NotaCreditoController {

    private List<NotaCredito> notas = new ArrayList<>();
    private int proximoNumero = 1;

    // Registra una NC. Reduce el saldo del proveedor porque la NC disminuye la deuda.
    public NotaCredito registrar(NotaCredito nc, Proveedor proveedor) {
        nc.numero = String.format("NC-%04d", proximoNumero++);
        if (nc.fechaEmision == null) nc.fechaEmision = new Date();
        nc.calcularTotal();
        notas.add(nc);

        if (proveedor != null) {
            proveedor.documentos.add(nc);
            proveedor.saldoActual -= nc.importeTotal;
        }
        return nc;
    }

    public List<NotaCredito> listarTodas() {
        return Collections.unmodifiableList(notas);
    }

    public List<NotaCredito> listarPorProveedor(Proveedor proveedor) {
        List<NotaCredito> resultado = new ArrayList<>();
        for (NotaCredito nc : notas) {
            if (proveedor.documentos.contains(nc)) resultado.add(nc);
        }
        return resultado;
    }
}
