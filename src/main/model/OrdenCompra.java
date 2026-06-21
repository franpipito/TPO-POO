package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrdenCompra {
	// Atributos
	public String numeroOC;
	public Date fechaEmision;
	public double totalEstimado;
	public String estado;
	public boolean requiereSupervision;

	// Relaciones
	public List<DetalleOrdenCompra> detalles = new ArrayList<>();

	public double calcularTotal() {
		return 0.0;
	}

	public void setRequiereSupervision(boolean requiere) {
	}

	public void setEstado(String estado) {
	}

	public void confirmarCreacion() {
	}
}


