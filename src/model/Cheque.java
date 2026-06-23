package model;

import java.util.Date;

public class Cheque extends MedioPago {
	// Atributos
	public Date fechaEmision;
	public Date fechaVencimiento;
	public String nombreFirmante;
	public String bancoEmisor;
	public String numeroCheque;
	// Tipo de cheque: "Propio" o "De terceros".
	public String tipoCheque;
}

