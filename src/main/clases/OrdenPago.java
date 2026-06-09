package clases;

import java.util.Date;

public class OrdenPago {
	// Atributos
	public String numeroOperacion;
	public Date fechaEmision;
	public double totalACancelar;
	public double totalRetenciones;

	public void asociarFactura(Factura factura) {
	}

	public double calcularTotalAPagar() {
		return 0.0;
	}

	public double calcularRetenciones() {
		return 0.0;
	}

	public double getTotalACancelar() {
		return totalACancelar;
	}

	public void confirmarEmision() {
	}
}


