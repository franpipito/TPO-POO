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
        // Guarda defensiva: ningún documento ya pagado puede volver a cancelarse.
        for (DocumentoComercial doc : documentos) {
            if (doc.pagado) {
                throw new IllegalStateException(
                    "El documento " + doc.getNumero() + " ya fue pagado y no puede cancelarse de nuevo.");
            }
        }

        OrdenPago op = new OrdenPago();
        op.fechaEmision = fecha != null ? fecha : new Date();
        op.proveedor = proveedor;
        op.documentos.addAll(documentos);
        op.mediosPago.addAll(medios);

        // La OP debe cancelar un importe positivo. Si las NC igualan o superan a las
        // facturas/ND seleccionadas, el neto sería cero o negativo: no es un pago válido
        // (una OP no puede "pagar" un importe negativo ni aumentar la deuda).
        if (op.calcularBaseImponible() <= 0) {
            throw new IllegalStateException(
                "La orden de pago debe cancelar un importe positivo. Revisa los documentos seleccionados.");
        }

        // El número correlativo se asigna recién acá, una vez validada la OP, para que
        // una OP rechazada no consuma un número.
        op.numeroOperacion = String.format("OP-%04d", proximoNumero++);
        op.confirmarEmision();

        // Reducir el saldo del proveedor por los documentos cancelados y marcarlos como
        // pagados (para que dejen de figurar como impagos en la cuenta corriente).
        for (DocumentoComercial doc : documentos) {
            if ("NC".equals(doc.getTipo())) {
                proveedor.saldoActual += doc.importeTotal;
            } else {
                proveedor.saldoActual -= doc.importeTotal;
            }
            doc.pagado = true;
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
