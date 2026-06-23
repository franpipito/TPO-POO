package model;

public class Usuario {
	// Atributos
	public int idUsuario;
	public String nombreUsuario;
	public String contrasenia;
	// Estado para la baja LÓGICA: un usuario inactivo no se borra de la lista, pero
	// no puede iniciar sesión. Permite conservar el historial y reactivarlo luego.
	public boolean activo = true;

	// Relaciones
	public Rol rol;

	// Verifica si el usuario tiene un permiso, apoyándose en su rol
	public boolean tienePermiso(String permiso) {
		return rol != null && rol.tienePermiso(permiso);
	}
}
