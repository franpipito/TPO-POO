package model;

import java.util.Date;

public class Factura extends DocumentoComercial {
	// Atributos
	public char tipoFactura;
	public Date fechaVencimiento;

	public boolean validarPreciosConOC(OrdenCompra orden) {
		return false;
	}

	public void confirmarRegistro() {
	}
}


