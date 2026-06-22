package model;

import java.util.Date;

public class Factura extends DocumentoComercial {
	// Atributos
	public char tipoFactura;
	public Date fechaVencimiento;

	// Relaciones
	public OrdenCompra ordenCompra;

	@Override
	public String getTipo() {
		return "F";
	}

	// Control automático: los precios facturados deben coincidir con los de la OC asociada.
	// Si un producto no figura en la OC, o el precio difiere, la validación falla
	// (la factura solo podrá registrarse con aprobación de un supervisor).
	public boolean validarPreciosConOC(OrdenCompra orden) {
		if (orden == null) {
			return false;
		}
		for (DetalleDocumento detalleFactura : detalles) {
			double precioAcordado = buscarPrecioEnOC(orden, detalleFactura.producto);
			if (precioAcordado < 0) {
				return false; // el producto facturado no figura en la OC
			}
			if (precioAcordado != detalleFactura.precioUnitarioAplicado) {
				return false; // el precio facturado no coincide con el acordado
			}
		}
		return true;
	}

	// Busca el precio acordado en la OC para un producto. Devuelve -1 si no está.
	private double buscarPrecioEnOC(OrdenCompra orden, ProductoServicio producto) {
		for (DetalleOrdenCompra detalle : orden.detalles) {
			if (detalle.producto == producto) {
				return detalle.precioAcordado;
			}
		}
		return -1;
	}

	public void confirmarRegistro() {
		// Al registrar la factura se consolida su importe total a partir del detalle
		calcularTotal();
	}
}


