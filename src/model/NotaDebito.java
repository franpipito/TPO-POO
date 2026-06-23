package model;

public class NotaDebito extends DocumentoComercial {
	// Atributos
	public String motivoCargo;

	@Override
	public String getTipo() {
		return "ND";
	}
}

