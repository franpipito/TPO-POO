package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

	// Relaciones
	public List<Rubro> rubros = new ArrayList<>();
	public CondicionIva condicionIva;
	public List<CertificadoNoRetencion> certificados = new ArrayList<>();
	public List<DocumentoComercial> documentos = new ArrayList<>();
	public List<OrdenCompra> ordenesCompra = new ArrayList<>();

	public List<DocumentoComercial> getDocumentosComerciales(Date desde, Date hasta) {
		return null;
	}

	public List<OrdenPago> getOrdenesPago(Date desde, Date hasta) {
		return null;
	}

	public boolean tieneCertificadoVigente(Impuestos impuesto, Date fecha) {
		return false;
	}

	public void agregarCertificado(CertificadoNoRetencion cert) {
	}

	public static Map<String, Object> getProveedor(int idProveedor) {
		return null;
	}
}


