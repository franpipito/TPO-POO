package model;

import java.util.ArrayList;
import java.util.List;

public class ProductoServicio {
	// Atributos
	public String nombre;
	public String unidadMedida;
	public double tipoIva;

	// Relaciones
	public Rubro rubro;
	// Proveedores que comercializan este producto con su último precio acordado
	public List<PrecioProveedor> precios = new ArrayList<>();
}

