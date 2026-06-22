package controller;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class OrdenPagoController {

    private List<OrdenPago> ordenes = new ArrayList<>();
    private int proximoNumero = 1;

    // Emite una OP cancelando los documentos indicados. Calcula y desglosa
    // automáticamente las retenciones por impuesto (IVA, IIBB, Ganancias),
    // respetando los certificados de no retención vigentes del proveedor.
    public OrdenPago emitir(Proveedor proveedor, List<DocumentoComercial> documentos,
                            List<MedioPago> medios, Date fecha) {
        OrdenPago op = new OrdenPago();
        op.numeroOperacion = String.format("OP-%04d", proximoNumero++);
        op.fechaEmision = fecha != null ? fecha : new Date();
        op.proveedor = proveedor;
        op.documentos.addAll(documentos);
        op.mediosPago.addAll(medios);
        op.confirmarEmision();

        // Reducir el saldo del proveedor por los documentos cancelados
        for (DocumentoComercial doc : documentos) {
            if ("NC".equals(doc.getTipo())) {
                proveedor.saldoActual += doc.importeTotal;
            } else {
                proveedor.saldoActual -= doc.importeTotal;
            }
        }

        ordenes.add(op);
        proveedor.ordenesPago.add(op);
        return op;
    }

    public List<OrdenPago> listarTodas() {
        return Collections.unmodifiableList(ordenes);
    }

    public List<OrdenPago> listarPorProveedor(Proveedor proveedor) {
        List<OrdenPago> resultado = new ArrayList<>();
        for (OrdenPago op : ordenes) {
            if (op.proveedor == proveedor) resultado.add(op);
        }
        return resultado;
    }
}
