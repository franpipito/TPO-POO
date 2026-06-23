package controller;

import model.Rol;
import model.Usuario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller de Usuarios: administra la colección en memoria y resuelve la
 * autenticación (login). Aplica baja LÓGICA: dar de baja no borra al usuario, solo
 * lo marca inactivo, de modo que se conserva su historial y se puede reactivar.
 */
public class UsuarioController {

    private final List<Usuario> usuarios = new ArrayList<>();
    private int proximoId = 1;

    /**
     * Registra un usuario nuevo. El nombre de usuario es único (es el identificador
     * de login). Lo crea activo y le asigna el próximo id.
     */
    public Usuario registrar(Usuario u) {
        if (buscarPorNombre(u.nombreUsuario) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con el nombre " + u.nombreUsuario);
        }
        u.idUsuario = proximoId++;
        u.activo = true;
        usuarios.add(u);
        return u;
    }

    /**
     * Modifica un usuario existente: puede cambiar su nombre de usuario, su contraseña
     * (si se indica una nueva no vacía) y su rol. El nombre de usuario sigue siendo único:
     * si se cambia, no puede coincidir con el de otro usuario.
     */
    public void modificar(Usuario u, String nuevoNombre, String nuevaClave, Rol nuevoRol) {
        if (u == null) return;
        if (nuevoNombre != null && !nuevoNombre.isEmpty() && !nuevoNombre.equals(u.nombreUsuario)) {
            Usuario existente = buscarPorNombre(nuevoNombre);
            if (existente != null && existente != u) {
                throw new IllegalArgumentException("Ya existe un usuario con el nombre " + nuevoNombre);
            }
            u.nombreUsuario = nuevoNombre;
        }
        if (nuevaClave != null && !nuevaClave.isEmpty()) {
            u.contrasenia = nuevaClave;
        }
        if (nuevoRol != null) {
            u.rol = nuevoRol;
        }
    }

    // Baja lógica: el usuario queda inactivo (no se elimina de la colección).
    public void darDeBaja(Usuario u) {
        if (u != null) u.activo = false;
    }

    // Reactiva un usuario dado de baja.
    public void reactivar(Usuario u) {
        if (u != null) u.activo = true;
    }

    /**
     * Autentica un intento de login: devuelve el usuario si existe, la contraseña
     * coincide y está activo; si no, devuelve null (no se distingue el motivo, por
     * seguridad).
     */
    public Usuario autenticar(String nombre, String clave) {
        Usuario u = buscarPorNombre(nombre);
        if (u != null && u.activo && u.contrasenia != null && u.contrasenia.equals(clave)) {
            return u;
        }
        return null;
    }

    // Búsqueda por nombre de usuario (usada para validar unicidad y para el login).
    public Usuario buscarPorNombre(String nombre) {
        for (Usuario u : usuarios) {
            if (u.nombreUsuario != null && u.nombreUsuario.equals(nombre)) return u;
        }
        return null;
    }

    // True si algún usuario tiene asignado ese rol (lo usa RolController para no
    // permitir borrar un rol en uso).
    public boolean algunoUsaRol(Rol rol) {
        for (Usuario u : usuarios) {
            if (u.rol == rol) return true;
        }
        return false;
    }

    public List<Usuario> listarTodos() {
        return Collections.unmodifiableList(usuarios);
    }
}
