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
		return 0.0;
	}

	public String getTipo() {
		return null;
	}

	public String getNumero() {
		return numero;
	}
}


