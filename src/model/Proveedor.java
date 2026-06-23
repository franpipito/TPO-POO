package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Proveedor {
	// Atributos
	public int idProveedor;
	public String razonSocial;
	public String nombreFantasia;
	public String cuit;
	public String direccion;
	public String telefono;
	public String email;
	public String ingresosBrutos;
	public Date fechaInicioActividades;
	public double topeDeuda;
	public double saldoActual;

	// Relaciones
	public List<Rubro> rubros = new ArrayList<>();
	public CondicionIva condicionIva;
	public List<CertificadoNoRetencion> certificados = new ArrayList<>();
	// Impuestos a los que el proveedor está sujeto a retención (IVA, IIBB, Ganancias)
	public List<Impuestos> impuestos = new ArrayList<>();
	public List<DocumentoComercial> documentos = new ArrayList<>();
	public List<OrdenCompra> ordenesCompra = new ArrayList<>();
	public List<OrdenPago> ordenesPago = new ArrayList<>();
	// Productos que comercializa con su último precio acordado (compulsa de precios)
	public List<PrecioProveedor> precios = new ArrayList<>();

	public List<DocumentoComercial> getDocumentosComerciales(Date desde, Date hasta) {
		List<DocumentoComercial> resultado = new ArrayList<>();
		for (DocumentoComercial doc : documentos) {
			if (estaEnRango(doc.fechaEmision, desde, hasta)) {
				resultado.add(doc);
			}
		}
		return resultado;
	}

	public List<OrdenPago> getOrdenesPago(Date desde, Date hasta) {
		List<OrdenPago> resultado = new ArrayList<>();
		for (OrdenPago op : ordenesPago) {
			if (estaEnRango(op.fechaEmision, desde, hasta)) {
				resultado.add(op);
			}
		}
		return resultado;
	}

	// Indica si una fecha cae dentro del rango [desde, hasta] (límites inclusivos y opcionales)
	private boolean estaEnRango(Date fecha, Date desde, Date hasta) {
		if (fecha == null) {
			return false;
		}
		if (desde != null && fecha.before(desde)) {
			return false;
		}
		if (hasta != null && fecha.after(hasta)) {
			return false;
		}
		return true;
	}

	// True si el proveedor tiene un certificado de no retención vigente para ese impuesto.
	// Mientras esté vigente no se aplica la retención correspondiente.
	public boolean tieneCertificadoVigente(Impuestos impuesto, Date fecha) {
		if (impuesto == null) {
			return false;
		}
		for (CertificadoNoRetencion cert : certificados) {
			if (cert.impuesto != null
					&& cert.impuesto.nombre != null
					&& cert.impuesto.nombre.equals(impuesto.nombre)
					&& cert.estaVigente(fecha)) {
				return true;
			}
		}
		return false;
	}

	public void agregarCertificado(CertificadoNoRetencion cert) {
		if (cert != null) {
			certificados.add(cert);
		}
	}
}


