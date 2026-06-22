# TPO-POO

Sistema de Gestión de Facturas y Proveedores para **CDCSoft** — TPO de Programación Orientada a Objetos 2026.

Aplicación de escritorio en Java + Swing, sin frameworks ni base de datos: todos los datos se mantienen en memoria. Arquitectura **MVC** (modelo, controladores y vista) en español, siguiendo los diagramas de clases y de secuencia de la primera entrega como fuente de verdad.

## Funcionalidad

- ABM de proveedores con CUIT único, impuestos a los que están sujetos y certificados de no retención.
- Catálogo de rubros, productos/servicios e impuestos (porcentaje base y mínimo no imponible).
- Órdenes de compra con armado de detalle y **control de tope de deuda**: si la OC excede el tope del proveedor, queda marcada como "requiere supervisión".
- Registro de comprobantes (Factura / Nota de Crédito / Nota de Débito) con **validación de precios contra la OC** asociada.
- Órdenes de pago con medios de pago y **cálculo automático de retenciones**, respetando mínimos no imponibles y certificados de no retención vigentes.
- Reportes de documentos comerciales y órdenes de pago por proveedor y rango de fechas.
- **Modo supervisor** (conmutador en la barra superior) para aprobar las operaciones que requieren supervisión (OC que exceden el tope, facturas con precios que no coinciden con la OC).

## Estructura

```text
src/main/
├── Main.java                 # Punto de entrada (lanza la ventana principal)
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
│   ├── ProveedorController.java
│   ├── OrdenCompraController.java
│   ├── FacturaController.java
│   ├── OrdenPagoController.java
│   ├── NotaCreditoController.java
│   ├── NotaDebitoController.java
│   └── CertificadoNoRetencionController.java
└── view/                     # Interfaz Swing (una pestaña por módulo)
    ├── VentanaPrincipal.java  # Shell con pestañas y modo supervisor
    ├── AppContext.java        # Controllers compartidos y datos maestros en memoria
    ├── Ui.java                # Utilidades de UI (combos, fechas, diálogos)
    ├── Refrescable.java       # Interfaz para recargar datos al activar la pestaña
    ├── PanelProveedores.java
    ├── PanelRubros.java
    ├── PanelProductos.java
    ├── PanelImpuestos.java
    ├── PanelOrdenCompra.java
    ├── PanelComprobantes.java
    ├── PanelOrdenPago.java
    ├── PanelCertificados.java
    └── PanelReportes.java
```
