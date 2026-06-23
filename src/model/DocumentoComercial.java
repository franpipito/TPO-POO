package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DocumentoComercial {
	// Atributos
	public String numero;
	public Date fechaEmision;
	public double importeTotal;

	// Relaciones
	public List<DetalleDocumento> detalles = new ArrayList<>();

	public double calcularTotal() {
		double total = 0.0;
		for (DetalleDocumento d : detalles) {
			total += d.calcularSubtotal();
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


