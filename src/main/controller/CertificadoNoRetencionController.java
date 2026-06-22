package controller;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CertificadoNoRetencionController {

    private List<CertificadoNoRetencion> certificados = new ArrayList<>();

    // Crea un certificado de no retención para un impuesto específico y lo asocia
    // al proveedor. Mientras esté vigente, ese impuesto no se retiene en las OP.
    public CertificadoNoRetencion crear(Proveedor proveedor, Impuestos impuesto, Date inicio, Date fin) {
        CertificadoNoRetencion cert = CertificadoNoRetencion.crear(
                String.valueOf(proveedor.idProveedor), impuesto, inicio, fin);
        proveedor.agregarCertificado(cert);
        certificados.add(cert);
        return cert;
    }

    public boolean estaVigente(CertificadoNoRetencion cert, Date fecha) {
        return cert != null && cert.estaVigente(fecha);
    }

    // Retorna los certificados vigentes del proveedor para una fecha dada.
    public List<CertificadoNoRetencion> listarVigentesPorProveedor(Proveedor proveedor, Date fecha) {
        List<CertificadoNoRetencion> resultado = new ArrayList<>();
        for (CertificadoNoRetencion cert : proveedor.certificados) {
            if (cert.estaVigente(fecha)) resultado.add(cert);
        }
        return resultado;
    }

    public List<CertificadoNoRetencion> listarTodos() {
        return Collections.unmodifiableList(certificados);
    }
}
