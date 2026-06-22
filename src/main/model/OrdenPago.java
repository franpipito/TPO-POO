package model;

 import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrdenPago {
	// Atributos
	public String numeroOperacion;
	public Date fechaEmision;
	public double totalACancelar;
	public double totalRetenciones;

	// Relaciones
	public Proveedor proveedor;
	public List<DocumentoComercial> documentos = new ArrayList<>();
	public List<MedioPago> mediosPago = new ArrayList<>();
	// Desglose de retenciones efectuadas (IVA, IIBB, Ganancias por separado)
	public List<RetencionAplicada> retencionesAplicadas = new ArrayList<>();

	public void asociarFactura(Factura factura) {
		// Asocia un documento a cancelar. El total se recalcula en calcularTotalAPagar().
		if (factura != null) {
			documentos.add(factura);
		}
	}

	public double calcularTotalAPagar() {
		// Total bruto de los documentos menos las retenciones aplicadas
		totalACancelar = calcularBaseImponible();
		return totalACancelar - calcularRetenciones();
	}

	// Base sobre la que se calculan las retenciones: suma de los documentos a
	// cancelar (las NC restan; las Facturas y ND suman).
	public double calcularBaseImponible() {
		double bruto = 0.0;
		for (DocumentoComercial doc : documentos) {
			if ("NC".equals(doc.getTipo())) {
				bruto -= doc.calcularTotal();
			} else {
				bruto += doc.calcularTotal();
			}
		}
		return bruto;
	}

	// Calcula las retenciones por cada impuesto al que está sujeto el proveedor
	// (IVA, IIBB, Ganancias). Respeta los mínimos no imponibles de cada impuesto
	// y omite los impuestos con certificado de no retención vigente.
	public double calcularRetenciones() {
		retencionesAplicadas.clear();
		double base = calcularBaseImponible();
		double total = 0.0;
		if (proveedor != null) {
			for (Impuestos imp : proveedor.impuestos) {
				// Si hay certificado de no retención vigente, no se retiene este impuesto
				if (proveedor.tieneCertificadoVigente(imp, fechaEmision)) {
					continue;
				}
				// calcularRetencion aplica el mínimo no imponible (ej. Ganancias)
				double monto = imp.calcularRetencion(base);
				if (monto > 0) {
					RetencionAplicada r = new RetencionAplicada();
					r.impuesto = imp;
					r.porcentajeAplicado = imp.porcentajeBase;
					r.montoRetenido = monto;
					retencionesAplicadas.add(r);
					total += monto;
				}
			}
		}
		totalRetenciones = total;
		return total;
	}

	public double getTotalACancelar() {
		return totalACancelar;
	}

	public void confirmarEmision() {
		// Al emitir la OP se consolidan el total a cancelar y las retenciones
		calcularTotalAPagar();
	}
}


