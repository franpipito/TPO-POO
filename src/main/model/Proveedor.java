package model;

import java.util.Date;
import java.util.List;

public class Proveedor {
	// Atributos
	public int idProveedor;
	public String razonSocial;
	public String cuit;
	public String direccion;
	public String telefono;
	public String email;
	public double topeDeuda;
	public double saldoActual;

	public List<DocumentoComercial> getDocumentosComerciales(Date desde, Date hasta) {
		return null;
	}

	public List<OrdenPago> getOrdenesPago(Date desde, Date hasta) {
		return null;
	}

	public boolean tieneCertificado(CertificadoNoRetencion cert) {
		return false;
	}

	public void agregarCertificado(CertificadoNoRetencion cert) {
	}

	// Método para obtener un proveedor por id (sin implementación)
	public static Proveedor getProveedor(int idProveedor) {
		return null;
	}
}


