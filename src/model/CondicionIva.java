package model;

public class CondicionIva {
	// Responsabilidad frente al IVA del proveedor. Lista fija para evitar errores de tipeo.
	public static final String[] OPCIONES = {
			"Responsable Inscripto",
			"Monotributo",
			"Exento",
			"Consumidor Final"
	};

	// Atributos
	public int idCondicion;
	public String descripcion;
}
