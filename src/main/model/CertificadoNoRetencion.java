package model;

import java.util.Date;

public class CertificadoNoRetencion {
	// Atributos
	public Date fechaInicio;
	public Date fechaFin;

	// Relaciones
	public Impuestos impuesto;

	public boolean estaVigente() {
		return false;
	}
}


