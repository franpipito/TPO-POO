package controller;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class OrdenCompraController {

    private List<OrdenCompra> ordenes = new ArrayList<>();
    private int proximoNumero = 1;

    // Crea y confirma una OC. Si el monto supera el tope de deuda del proveedor,
    // la OC queda marcada con requiereSupervision — en ese caso el usuario que la
    // emite debe tener permiso de supervisión, de lo contrario se rechaza.
    public OrdenCompra crearOrden(Proveedor proveedor, List<DetalleOrdenCompra> detalles, Usuario usuario) {
        OrdenCompra oc = new OrdenCompra();
        oc.fechaEmision = new Date();
        oc.proveedor = proveedor;
        oc.detalles.addAll(detalles);
        oc.confirmarCreacion();

        if (oc.requiereSupervision) {
            if (usuario == null || !usuario.tienePermiso(Rol.PERMISO_APROBAR_SUPERVISION)) {
                throw new IllegalStateException(
                    "La OC supera el tope de deuda del proveedor y requiere aprobacion supervisorial.");
            }
        }

        // El número correlativo se asigna recién acá, una vez aprobada, para que una OC
        // rechazada por falta de supervisión no consuma un número.
        oc.numeroOC = String.format("OC-%04d", proximoNumero++);
        ordenes.add(oc);
        proveedor.ordenesCompra.add(oc);
        return oc;
    }

    // Construye un detalle de OC listo para pasarle a crearOrden().
    public DetalleOrdenCompra crearDetalle(ProductoServicio producto, int cantidad, double precioAcordado) {
        DetalleOrdenCompra detalle = new DetalleOrdenCompra();
        detalle.producto = producto;
        detalle.cantidad = cantidad;
        detalle.precioAcordado = precioAcordado;
        return detalle;
    }

    public OrdenCompra buscarPorNumero(String numeroOC) {
        for (OrdenCompra oc : ordenes) {
            if (oc.numeroOC != null && oc.numeroOC.equals(numeroOC)) return oc;
        }
        return null;
    }

    public List<OrdenCompra> listarTodas() {
        return Collections.unmodifiableList(ordenes);
    }

    public List<OrdenCompra> listarPorProveedor(Proveedor proveedor) {
        List<OrdenCompra> resultado = new ArrayList<>();
        for (OrdenCompra oc : ordenes) {
            if (oc.proveedor == proveedor) resultado.add(oc);
        }
        return resultado;
    }
}
