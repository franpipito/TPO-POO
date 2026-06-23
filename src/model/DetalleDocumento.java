package model;

public class DetalleDocumento {
	// Atributos
	public int cantidad;
	public double precioUnitarioAplicado;
	public double subTotalLinea;
	// Alicuota de IVA aplicada a esta linea (2.5, 5, 10.5, 21, 27 o 0=Exento). Se copia
	// del producto al cargar el comprobante y permite discriminar el IVA en el Libro IVA.
	public double alicuotaIva;

	// Relaciones
	public ProductoServicio producto;

	public double calcularSubtotal() {
		subTotalLinea = cantidad * precioUnitarioAplicado;
		return subTotalLinea;
	}
}

