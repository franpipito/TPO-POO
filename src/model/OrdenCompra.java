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
	public Proveedor proveedor;

	public double calcularTotal() {
		double total = 0.0;
		for (DetalleOrdenCompra d : detalles) {
			total += d.calcularSubtotal();
		}
		totalEstimado = total;
		return total;
	}

	// Control de límite de deuda: el total de la nueva OC, sumado a la deuda
	// pendiente del proveedor, no debe superar su tope de endeudamiento.
	public boolean excedeTopeDeuda(Proveedor proveedor) {
		double deudaProyectada = proveedor.saldoActual + calcularTotal();
		return deudaProyectada > proveedor.topeDeuda;
	}

	public void setRequiereSupervision(boolean requiere) {
		this.requiereSupervision = requiere;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	// Confirma la creación de la OC controlando el tope de deuda del proveedor.
	// Si la nueva OC + la deuda pendiente supera el tope, queda marcada para
	// supervisión/aprobación gerencial obligatoria antes de emitirse.
	public void confirmarCreacion() {
		if (proveedor != null) {
			requiereSupervision = excedeTopeDeuda(proveedor);
		}
		this.estado = "CONFIRMADA";
	}
}


