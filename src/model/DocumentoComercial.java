package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DocumentoComercial {
	// Atributos
	public String numero;
	public Date fechaEmision;
	public double importeTotal;
	// Indica si el documento ya fue cancelado por una orden de pago. Sirve para listar
	// los documentos impagos en la cuenta corriente del proveedor.
	public boolean pagado;

	// Relaciones
	public List<DetalleDocumento> detalles = new ArrayList<>();

	// Total del documento = neto + IVA. Cada línea suma su subtotal neto más el IVA según
	// su alícuota, de modo que el importe registrado (deuda del proveedor) incluye el IVA
	// y coincide con el total del Libro IVA Compras.
	public double calcularTotal() {
		double total = 0.0;
		for (DetalleDocumento d : detalles) {
			double neto = d.calcularSubtotal();
			total += neto + neto * d.alicuotaIva / 100.0;
		}
		importeTotal = total;
		return total;
	}

	// Tipo de documento para la cuenta corriente. Lo redefine cada subclase (F / NC / ND).
	public String getTipo() {
		return "DC";
	}

	public String getNumero() {
		return numero;
	}
}


