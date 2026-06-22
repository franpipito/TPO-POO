package model;

import java.util.ArrayList;
import java.util.List;

public class Rol {
	// Permiso clave para aprobar operaciones que requieren supervisión
	// (OC que exceden el tope de deuda, facturas sin OC o con precios que no coinciden)
	public static final String PERMISO_APROBAR_SUPERVISION = "APROBAR_SUPERVISION";

	// Atributos
	public int idRol;
	public String nombre;
	public List<String> permisos = new ArrayList<>();

	// Indica si el rol habilita un permiso determinado
	public boolean tienePermiso(String permiso) {
		return permisos.contains(permiso);
	}
}
