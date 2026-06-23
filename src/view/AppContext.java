package view;

import controller.*;
import model.Impuestos;
import model.ProductoServicio;
import model.Rol;
import model.Rubro;
import model.Usuario;

import java.util.ArrayList;
import java.util.List;

// Estado compartido de la aplicación. Crea una única instancia de cada controller
// (mantienen los datos en memoria) y la comparte entre todos los paneles, junto con
// los datos maestros que aún no tienen un controller dedicado (rubros, productos,
// impuestos). Provee también un usuario "supervisor" conmutable para poder ejercer
// los flujos de aprobación desde la interfaz.
public class AppContext {

    public final ProveedorController proveedores = new ProveedorController();
    public final OrdenCompraController ordenesCompra = new OrdenCompraController();
    public final FacturaController facturas = new FacturaController();
    public final OrdenPagoController ordenesPago = new OrdenPagoController();
    public final NotaCreditoController notasCredito = new NotaCreditoController();
    public final NotaDebitoController notasDebito = new NotaDebitoController();
    public final CertificadoNoRetencionController certificados = new CertificadoNoRetencionController();

    // Datos maestros en memoria (sin controller propio en el alcance actual)
    public final List<Rubro> rubros = new ArrayList<>();
    public final List<ProductoServicio> productos = new ArrayList<>();
    public final List<Impuestos> impuestos = new ArrayList<>();
    public int proximoIdRubro = 1;

    private final Usuario supervisor;
    private final Usuario operador;
    private boolean modoSupervisor = false;

    public AppContext() {
        Rol rolSupervisor = new Rol();
        rolSupervisor.nombre = "Supervisor";
        rolSupervisor.permisos.add(Rol.PERMISO_APROBAR_SUPERVISION);
        supervisor = new Usuario();
        supervisor.nombreUsuario = "supervisor";
        supervisor.rol = rolSupervisor;

        Rol rolOperador = new Rol();
        rolOperador.nombre = "Operador";
        operador = new Usuario();
        operador.nombreUsuario = "operador";
        operador.rol = rolOperador;
    }

    public void setModoSupervisor(boolean modoSupervisor) {
        this.modoSupervisor = modoSupervisor;
    }

    public boolean isModoSupervisor() {
        return modoSupervisor;
    }

    // Usuario con el que se ejecutan las operaciones: supervisor (con permiso de
    // aprobación) u operador común, según el conmutador de la ventana principal.
    public Usuario getUsuarioActual() {
        return modoSupervisor ? supervisor : operador;
    }
}
