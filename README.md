# Celda 4 · Panel de la pinza

La planta usa esta app en tablets industriales montadas junto a la celda del robot. Los operadores trabajan con guantes, la luz cambia según el turno y las tablets funcionan a batería. La versión 1.4 ya está en uso en el turno mañana. Tenés una hora para entregar las correcciones.

## Preparación previa

Abrí esta carpeta en Android Studio con JDK 17, Android SDK 34 y un emulador de API 26 o superior. Sincronizá las dependencias con Internet antes de clase. Ejecutá app o compilá con ./gradlew assembleDebug (gradlew.bat assembleDebug en Windows). No agregues dependencias ni cambies versiones de librerías.

Banco controla el robot de prueba. Cada comando tarda 1,5 segundos, o 6 con «Demora larga». «Rechazar el próximo comando» hace que el robot rechace la siguiente orden de pinza; la parada nunca se rechaza. «Batería baja» hace que la telemetría informe 15 %. Los contadores de consultas de telemetría y de comandos recibidos vuelven a cero al morir el proceso.

## Condiciones de entrega

| Pedido | Comportamiento observable |
| --- | --- |
| 51 · Velocidad de la pinza | Escribir «80» y tocar Aplicar muestra «66,7 % · Alta». De 0 a 79 es Normal, de 80 a 120 es Alta. Otro número muestra «Fuera de rango» y no se aplica ni se registra. Un texto que no es un número («8O», vacío, «abc») muestra «Ingresá un número entre 0 y 120» y la app sigue abierta. El campo ofrece el teclado numérico. |
| 52 · Turno sin pérdidas | Con una velocidad escrita sin aplicar, el modo manual activo, eventos en el historial y un filtro o búsqueda puestos, rotá dos veces: todo sigue igual, incluido el estado de la pinza. Cada Aplicar, comando o alarma aparece en el historial al instante, también si el historial está abierto. Buscar «pinza» encuentra los eventos cuyo tipo o texto contiene la palabra, sin importar mayúsculas. Borrar un evento con un filtro activo borra exactamente ese evento. Si un evento está expandido y se borra otro, sigue expandido el mismo. |
| 53 · Telemetría y batería | Con la app visible hay una lectura de telemetría por segundo. Después de rotar varias veces sigue siendo una por segundo. Con la app en el fondo (Inicio), deja de leer en menos de 5 segundos; al volver, se retoma. Usá el contador de Banco para comprobarlo. |
| 54 · Operar con guantes | DETENER se puede tocar desde cualquier pestaña con un solo toque, es el control más grande de la pantalla y funciona incluso mientras se envía otro comando; la pantalla muestra «Celda detenida». Abrir o cerrar la pinza muestra «Enviando…» hasta que el robot responde: si acepta, muestra el estado nuevo; si rechaza, informa el rechazo y conserva el estado anterior. Dos toques durante un envío mandan un solo comando (contador de Banco). La batería baja se distingue sin depender del color. Todo control táctil mide al menos 48 dp. «Compartir reporte» abre el selector de apps con el resumen del turno. Los textos de la pestaña Panel están en recursos. |

Entregá un link a tu repositorio público con un CAMBIOS.md que explique, por pedido, el problema, el cambio y cómo lo verificaste. No se aceptan archivos .zip.
