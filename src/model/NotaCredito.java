package model;

public class NotaCredito extends DocumentoComercial {
	// Atributos
	public String motivoDevolucion;

	@Override
	public String getTipo() {
		return "NC";
	}
}

