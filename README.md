# TPO-POO

Sistema de Gestión de Facturas y Proveedores para **CDCSoft** — TPO de Programación Orientada a Objetos 2026.

Aplicación de escritorio en Java + Swing, sin frameworks ni base de datos: todos los datos se mantienen en memoria. Arquitectura **MVC** (modelo, controladores y vista) en español, siguiendo los diagramas de clases y de secuencia de la primera entrega como fuente de verdad.

## Funcionalidad

- **Usuarios y seguridad**: pantalla de login, ABM de usuarios (con baja lógica) y de roles con permisos. Las operaciones que requieren aprobación las habilita el rol de supervisor; las pestañas de administración solo las ve un supervisor.
- **Proveedores**: alta, modificación y baja, con CUIT único (sin importar el formato), responsabilidad frente al IVA, fecha de inicio de actividades, rubros que comercializa, impuestos a los que está sujeto a retención y certificados de no retención.
- **Catálogo**: rubros (nombre único, descripción opcional), productos/servicios (rubro único, alícuota de IVA y **precio acordado por proveedor**) e impuestos (porcentaje base y mínimo no imponible).
- **Órdenes de compra** con armado de detalle y **control de tope de deuda**: si la OC excede el tope del proveedor, queda marcada como "requiere supervisión".
- **Comprobantes** (Factura / Nota de Crédito / Nota de Débito) con **validación de precios contra la OC** asociada. El importe del documento **incluye el IVA**.
- **Órdenes de pago** con medios de pago (efectivo, transferencia y cheque propio/de terceros con sus datos) y **cálculo automático de retenciones**, respetando mínimos no imponibles y certificados vigentes. Los documentos pagados no pueden volver a cancelarse.
- **Consultas**: cuenta corriente del proveedor (deuda, documentos recibidos, impagos e historial de pagos), **Libro IVA Compras**, **compulsa de precios** y **análisis financiero** (ranking de deuda y total de retenciones por impuesto). Más reportes por proveedor y rango de fechas.

## Estructura

```text
src/
├── Main.java                 # Punto de entrada (lanza la pantalla de login)
├── model/                    # Modelo de dominio (atributos y reglas de negocio)
│   ├── Proveedor.java
│   ├── CondicionIva.java
│   ├── Rubro.java
│   ├── ProductoServicio.java
│   ├── PrecioProveedor.java
│   ├── Impuestos.java
│   ├── CertificadoNoRetencion.java
│   ├── DocumentoComercial.java
│   ├── Factura.java
│   ├── NotaCredito.java
│   ├── NotaDebito.java
│   ├── DetalleDocumento.java
│   ├── OrdenCompra.java
│   ├── DetalleOrdenCompra.java
│   ├── OrdenPago.java
│   ├── RetencionAplicada.java
│   ├── MedioPago.java
│   ├── Efectivo.java
│   ├── Cheque.java
│   ├── Transferencia.java
│   ├── Usuario.java
│   └── Rol.java
├── controller/               # Orquestación: un controller por módulo funcional
│   ├── UsuarioController.java
│   ├── RolController.java
│   ├── ProveedorController.java
│   ├── OrdenCompraController.java
│   ├── FacturaController.java
│   ├── OrdenPagoController.java
│   ├── NotaCreditoController.java
│   ├── NotaDebitoController.java
│   └── CertificadoNoRetencionController.java
└── view/                     # Interfaz Swing (una pestaña por módulo)
    ├── VentanaLogin.java      # Pantalla de inicio de sesión
    ├── VentanaPrincipal.java  # Shell con pestañas y usuario logueado
    ├── AppContext.java        # Controllers compartidos y datos maestros en memoria
    ├── Ui.java                # Utilidades de UI (combos, fechas, diálogos, validaciones)
    ├── Refrescable.java       # Interfaz para recargar datos al activar la pestaña
    ├── PanelUsuarios.java
    ├── PanelRoles.java
    ├── PanelProveedores.java
    ├── PanelRubros.java
    ├── PanelProductos.java
    ├── PanelImpuestos.java
    ├── PanelOrdenCompra.java
    ├── PanelComprobantes.java
    ├── PanelOrdenPago.java
    ├── PanelCertificados.java
    ├── PanelCuentaCorriente.java
    ├── PanelLibroIva.java
    ├── PanelCompulsaPrecios.java
    ├── PanelAnalisisFinanciero.java
    └── PanelReportes.java
```
