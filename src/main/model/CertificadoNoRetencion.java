package model;

import java.util.Date;

public class CertificadoNoRetencion {
	// Atributos
	public Date fechaInicio;
	public Date fechaFin;

	// Relaciones
	public Impuestos impuesto;

	public static CertificadoNoRetencion crear(String idProv, Impuestos imp, Date inicio, Date fin) {
		return null;
	}

	public boolean estaVigente(Date fecha) {
		return false;
	}
}


