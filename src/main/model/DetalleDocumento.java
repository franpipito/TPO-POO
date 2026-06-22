package model;

public class DetalleDocumento {
	// Atributos
	public int cantidad;
	public double precioUnitarioAplicado;
	public double subTotalLinea;

	// Relaciones
	public ProductoServicio producto;

	public double calcularSubtotal() {
		subTotalLinea = cantidad * precioUnitarioAplicado;
		return subTotalLinea;
	}
}

