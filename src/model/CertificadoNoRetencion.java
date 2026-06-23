package model;

import java.util.Date;

public class CertificadoNoRetencion {
	// Atributos
	public Date fechaInicio;
	public Date fechaFin;

	// Relaciones
	public Impuestos impuesto;

	public static CertificadoNoRetencion crear(String idProv, Impuestos imp, Date inicio, Date fin) {
		CertificadoNoRetencion cert = new CertificadoNoRetencion();
		cert.impuesto = imp;
		cert.fechaInicio = inicio;
		cert.fechaFin = fin;
		return cert;
	}

	// Vigente si la fecha está dentro del período del certificado.
	// Pasado fechaFin deja de estar vigente y el sistema reinstaura la retención.
	public boolean estaVigente(Date fecha) {
		if (fecha == null || fechaInicio == null || fechaFin == null) {
			return false;
		}
		return !fecha.before(fechaInicio) && !fecha.after(fechaFin);
	}
}


