package controller;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ProveedorController {

    private List<Proveedor> proveedores = new ArrayList<>();
    private int proximoId = 1;

    // Registra un proveedor nuevo. El CUIT es identificador único — no permite duplicados.
    public Proveedor registrar(Proveedor p) {
        if (buscarPorCuit(p.cuit) != null) {
            throw new IllegalArgumentException("Ya existe un proveedor con el CUIT " + p.cuit);
        }
        p.idProveedor = proximoId++;
        proveedores.add(p);
        return p;
    }

    public void eliminar(int idProveedor) {
        proveedores.removeIf(p -> p.idProveedor == idProveedor);
    }

    public Proveedor buscarPorId(int idProveedor) {
        for (Proveedor p : proveedores) {
            if (p.idProveedor == idProveedor) return p;
        }
        return null;
    }

    // Busca por CUIT comparando solo los dígitos, de modo que "20-12345678-6" y
    // "20123456786" se consideren el mismo CUIT (no se puede duplicar cambiando el formato).
    public Proveedor buscarPorCuit(String cuit) {
        String objetivo = normalizarCuit(cuit);
        if (objetivo.isEmpty()) return null;
        for (Proveedor p : proveedores) {
            if (normalizarCuit(p.cuit).equals(objetivo)) return p;
        }
        return null;
    }

    // Deja solo los dígitos del CUIT (quita guiones, espacios, etc.).
    private static String normalizarCuit(String cuit) {
        return cuit == null ? "" : cuit.replaceAll("[^0-9]", "");
    }

    public List<Proveedor> listarTodos() {
        return Collections.unmodifiableList(proveedores);
    }

    public List<DocumentoComercial> getDocumentosComerciales(int idProveedor, Date desde, Date hasta) {
        Proveedor p = buscarPorId(idProveedor);
        if (p == null) return Collections.emptyList();
        return p.getDocumentosComerciales(desde, hasta);
    }

    public List<OrdenPago> getOrdenesPago(int idProveedor, Date desde, Date hasta) {
        Proveedor p = buscarPorId(idProveedor);
        if (p == null) return Collections.emptyList();
        return p.getOrdenesPago(desde, hasta);
    }

    public void agregarCertificado(int idProveedor, CertificadoNoRetencion cert) {
        Proveedor p = buscarPorId(idProveedor);
        if (p != null) p.agregarCertificado(cert);
    }
}
