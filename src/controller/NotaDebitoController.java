package controller;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NotaDebitoController {

    private List<NotaDebito> notas = new ArrayList<>();
    private int proximoNumero = 1;

    // Registra una ND. Aumenta el saldo del proveedor porque la ND incrementa la deuda.
    public NotaDebito registrar(NotaDebito nd, Proveedor proveedor) {
        nd.numero = String.format("ND-%04d", proximoNumero++);
        if (nd.fechaEmision == null) nd.fechaEmision = new Date();
        nd.calcularTotal();
        notas.add(nd);

        if (proveedor != null) {
            proveedor.documentos.add(nd);
            proveedor.saldoActual += nd.importeTotal;
        }
        return nd;
    }

    public List<NotaDebito> listarTodas() {
        return Collections.unmodifiableList(notas);
    }

    public List<NotaDebito> listarPorProveedor(Proveedor proveedor) {
        List<NotaDebito> resultado = new ArrayList<>();
        for (NotaDebito nd : notas) {
            if (proveedor.documentos.contains(nd)) resultado.add(nd);
        }
        return resultado;
    }
}
