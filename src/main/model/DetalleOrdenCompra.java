package model;

public class DetalleOrdenCompra {
	// Atributos
	public int cantidad;
	public double precioAcordado;

	// Relaciones
	public ProductoServicio producto;

	public double calcularSubtotal() {
		return cantidad * precioAcordado;
	}
}

