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
// impuestos). Mantiene además el USUARIO LOGUEADO, con el que se ejecutan y autorizan
// las operaciones.
public class AppContext {

    public final ProveedorController proveedores = new ProveedorController();
    public final OrdenCompraController ordenesCompra = new OrdenCompraController();
    public final FacturaController facturas = new FacturaController();
    public final OrdenPagoController ordenesPago = new OrdenPagoController();
    public final NotaCreditoController notasCredito = new NotaCreditoController();
    public final NotaDebitoController notasDebito = new NotaDebitoController();
    public final CertificadoNoRetencionController certificados = new CertificadoNoRetencionController();
    public final UsuarioController usuarios = new UsuarioController();
    // El controller de roles necesita el de usuarios para no borrar roles en uso.
    public final RolController roles = new RolController(usuarios);

    // Datos maestros en memoria (sin controller propio en el alcance actual)
    public final List<Rubro> rubros = new ArrayList<>();
    public final List<ProductoServicio> productos = new ArrayList<>();
    public final List<Impuestos> impuestos = new ArrayList<>();
    public int proximoIdRubro = 1;

    // Usuario que tiene la sesión iniciada. Lo setea el login y lo limpia el logout.
    private Usuario usuarioLogueado;

    public AppContext() {
        // --- Roles semilla (los que ya existían en el sistema) ---
        Rol rolSupervisor = new Rol();
        rolSupervisor.nombre = "Supervisor";
        rolSupervisor.permisos.add(Rol.PERMISO_APROBAR_SUPERVISION);
        roles.crear(rolSupervisor);

        Rol rolOperador = new Rol();
        rolOperador.nombre = "Operador";
        roles.crear(rolOperador);

        // --- Usuarios semilla ---
        // admin/admin: supervisor, necesario para poder entrar la primera vez.
        Usuario admin = new Usuario();
        admin.nombreUsuario = "admin";
        admin.contrasenia = "admin";
        admin.rol = rolSupervisor;
        usuarios.registrar(admin);

        // operador/operador: usuario común de ejemplo (sin permisos de supervisión).
        Usuario operador = new Usuario();
        operador.nombreUsuario = "operador";
        operador.contrasenia = "operador";
        operador.rol = rolOperador;
        usuarios.registrar(operador);
    }

    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
    }

    // Usuario con el que se ejecutan las operaciones (el que inició sesión).
    public Usuario getUsuarioActual() {
        return usuarioLogueado;
    }

    // True si el usuario logueado puede gestionar usuarios y roles. Reutiliza el rol
    // Supervisor existente: tener APROBAR_SUPERVISION habilita la administración.
    public boolean puedeGestionarUsuarios() {
        return usuarioLogueado != null
                && usuarioLogueado.tienePermiso(Rol.PERMISO_APROBAR_SUPERVISION);
    }
}
