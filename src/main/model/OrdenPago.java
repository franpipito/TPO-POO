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


