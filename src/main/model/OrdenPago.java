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
	public List<DocumentoComercial> documentos = new ArrayList<>();
	public List<MedioPago> mediosPago = new ArrayList<>();
	// Desglose de retenciones efectuadas (IVA, IIBB, Ganancias por separado)
	public List<RetencionAplicada> retencionesAplicadas = new ArrayList<>();

	public void asociarFactura(Factura factura) {
	}

	public double calcularTotalAPagar() {
		return 0.0;
	}

	public double calcularRetenciones() {
		// Suma el detalle de retenciones aplicadas y actualiza el total
		double total = 0.0;
		for (RetencionAplicada r : retencionesAplicadas) {
			total += r.montoRetenido;
		}
		totalRetenciones = total;
		return total;
	}

	public double getTotalACancelar() {
		return totalACancelar;
	}

	public void confirmarEmision() {
	}
}


