package model;

public class Impuestos {
	// Atributos
	public String nombre;
	public String tipoImpuesto;
	public double porcentajeBase;
	public double minimoNoImponible;

	public Impuestos buscarPorNombre(String nombre) {
		if (nombre != null && nombre.equals(this.nombre)) {
			return this;
		}
		return null;
	}

	public boolean esValido() {
		return nombre != null && !nombre.isEmpty() && porcentajeBase > 0;
	}

	// Calcula la retención sobre un monto base. Si el monto no supera el mínimo no
	// imponible (caso típico de Ganancias) no corresponde retención.
	public double calcularRetencion(double montoBase) {
		if (montoBase <= minimoNoImponible) {
			return 0.0;
		}
		return montoBase * porcentajeBase / 100.0;
	}
}


