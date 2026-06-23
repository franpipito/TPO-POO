package controller;

import model.Rol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller de Roles. Permite crear/editar roles y eliminarlos, pero NO deja
 * borrar un rol que esté asignado a algún usuario (para no dejar usuarios sin rol
 * válido). Para esa verificación se apoya en el {@link UsuarioController}.
 */
public class RolController {

    private final List<Rol> roles = new ArrayList<>();
    private int proximoId = 1;

    // Referencia al controller de usuarios para chequear si un rol está en uso.
    private final UsuarioController usuarios;

    public RolController(UsuarioController usuarios) {
        this.usuarios = usuarios;
    }

    /**
     * Crea un rol nuevo. El nombre es único. Recibe el rol ya armado (con su lista
     * de permisos tildados desde la vista).
     */
    public Rol crear(Rol r) {
        if (buscarPorNombre(r.nombre) != null) {
            throw new IllegalArgumentException("Ya existe un rol con el nombre " + r.nombre);
        }
        r.idRol = proximoId++;
        roles.add(r);
        return r;
    }

    /**
     * Elimina un rol, salvo que esté asignado a algún usuario: en ese caso lanza
     * excepción y no lo borra (regla de integridad confirmada con el usuario).
     */
    public void eliminar(Rol r) {
        if (r == null) return;
        if (usuarios.algunoUsaRol(r)) {
            throw new IllegalStateException(
                "No se puede eliminar el rol porque hay usuarios que lo tienen asignado.");
        }
        roles.remove(r);
    }

    public Rol buscarPorNombre(String nombre) {
        for (Rol r : roles) {
            if (r.nombre != null && r.nombre.equals(nombre)) return r;
        }
        return null;
    }

    public List<Rol> listarTodos() {
        return Collections.unmodifiableList(roles);
    }
}
