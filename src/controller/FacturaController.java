package controller;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FacturaController {

    private List<Factura> facturas = new ArrayList<>();
    private int proximoNumero = 1;

    // Registra una factura recibida. Si no hay OC asociada o los precios no coinciden
    // con la OC, se requiere aprobacion de un supervisor antes de confirmar el registro.
    public Factura registrar(Factura factura, OrdenCompra oc, Proveedor proveedor, Usuario usuario) {
        if (factura.fechaEmision == null) factura.fechaEmision = new Date();
        factura.ordenCompra = oc;

        boolean preciosValidos = (oc != null) && factura.validarPreciosConOC(oc);

        if (!preciosValidos) {
            if (usuario == null || !usuario.tienePermiso(Rol.PERMISO_APROBAR_SUPERVISION)) {
                throw new IllegalStateException(
                    "La factura no tiene OC asociada o sus precios difieren. Requiere aprobacion supervisorial.");
            }
        }

        // El número correlativo se asigna recién acá, cuando el registro está confirmado,
        // para que una factura rechazada no consuma un número.
        factura.numero = String.format("F-%04d", proximoNumero++);
        factura.confirmarRegistro();
        facturas.add(factura);

        if (proveedor != null) {
            proveedor.documentos.add(factura);
            proveedor.saldoActual += factura.importeTotal;
        }
        return factura;
    }

    // Construye un detalle de factura listo para agregar a la factura antes de registrarla.
    public DetalleDocumento crearDetalle(ProductoServicio producto, int cantidad, double precioUnitario) {
        DetalleDocumento detalle = new DetalleDocumento();
        detalle.producto = producto;
        detalle.cantidad = cantidad;
        detalle.precioUnitarioAplicado = precioUnitario;
        detalle.subTotalLinea = detalle.calcularSubtotal();
        return detalle;
    }

    public List<Factura> listarTodas() {
        return Collections.unmodifiableList(facturas);
    }

    public List<Factura> listarPorProveedor(Proveedor proveedor) {
        List<Factura> resultado = new ArrayList<>();
        for (Factura f : facturas) {
            if (proveedor.documentos.contains(f)) resultado.add(f);
        }
        return resultado;
    }
}
