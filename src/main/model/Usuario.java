package model;

public class Usuario {
	// Atributos
	public int idUsuario;
	public String nombreUsuario;
	public String contrasenia;

	// Relaciones
	public Rol rol;

	// Verifica si el usuario tiene un permiso, apoyándose en su rol
	public boolean tienePermiso(String permiso) {
		return rol != null && rol.tienePermiso(permiso);
	}
}
