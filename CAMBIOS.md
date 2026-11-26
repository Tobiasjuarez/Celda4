# Cambios · Celda 4 v1.4

Un commit por pedido. No se agregaron dependencias ni se cambiaron versiones.

## 51 · Velocidad de la pinza

- **Problema:**
  - `toInt()` cerraba la app con «8O», vacío o «abc».
  - `80 / 120 * 100` daba 0 % por división entera.
  - En el `when`, `0..120` estaba antes que `80..120`, así que «Alta» nunca se alcanzaba.
  - No se validaba el rango.
  - El porcentaje se formateaba con el idioma del dispositivo.
- **Cambio:**
  - `leerVelocidad` usa `toIntOrNull()`.
  - El porcentaje se calcula como `v * 100.0 / 120` (Double antes de dividir).
  - Los niveles no se superponen: `0 until 80` es Normal y `80..120` es Alta.
  - `evaluarVelocidad` devuelve `Valida`, `FueraDeRango` o `NoEsNumero` (`sealed interface`). Solo `Valida` se registra.
  - El decimal se formatea con `Locale` es-AR.
  - El campo tiene teclado numérico.
- **Verificación:**
  - «80» → «66,7 % · Alta».
  - «79» → «65,8 % · Normal».
  - «150» → «Fuera de rango», sin evento.
  - «8O», vacío o «abc» → «Ingresá un número entre 0 y 120», sin crash.

## 52 · Turno sin pérdidas

- **Problema:**
  - El ViewModel se creaba con `PanelModel()` dentro de la Activity, así que al rotar se creaba uno nuevo y se perdían los eventos, la pinza y la parada.
  - La velocidad, el modo manual, el filtro, la búsqueda y el estado expandido estaban en `remember`.
  - Los eventos estaban en un `mutableListOf`, que no es observable: Borrar no se veía.
  - La búsqueda usaba `&&` y distinguía mayúsculas.
  - Se borraba por índice de la lista filtrada sobre la lista completa.
  - La lista no tenía `key`.
- **Cambio:**
  - El modelo se obtiene con `by viewModels()`.
  - Los eventos están en un `mutableStateListOf`, expuesto como `List`.
  - Se borra por `id`.
  - `coincide` usa `||` con `ignoreCase`.
  - Esos estados de pantalla pasaron a `rememberSaveable`.
  - La lista usa `items(…, key = { it.id })`.
- **Verificación:**
  - Con velocidad sin aplicar, modo manual, pinza cerrada y filtro «Pinza», dos rotaciones conservan todo.
  - Buscar «pinza» encuentra los eventos de pinza.
  - Con filtro, Borrar elimina el evento tocado.
  - Un evento expandido sigue expandido al borrar otro.

## 53 · Telemetría y batería

- **Problema:** un `Timer` arrancaba en `onCreate` y nunca se cancelaba. Cada rotación sumaba otro, y en el fondo seguía leyendo.
- **Cambio:** `vigilarTelemetria()` es un bucle `suspend` con `delay(1_000)`. Se lanza con `lifecycleScope.launch { repeatOnLifecycle(STARTED) { … } }`: arranca en `onStart` y se cancela en `onStop`.
- **Verificación** (contador de Banco):
  - 1 lectura por segundo, también después de rotar varias veces.
  - Con 10 segundos en el fondo suma 0 o 1, y al volver se retoma.

## 54 · Operar con guantes

- **Problema:**
  - DETENER medía 36 dp, estaba solo en Panel, junto a Abrir y Cerrar, y pedía confirmación.
  - La pinza cambiaba de estado al tocar, sin esperar al robot, y se ignoraba el rechazo.
  - Cada toque mandaba un comando.
  - La batería baja se indicaba solo con color.
  - «Borrar» era un `Text` con `.padding().clickable`, así que el área táctil era solo el texto.
  - `ACTION_SEND` sin `type` cerraba la app.
  - Los textos estaban escritos en el código.
- **Cambio:**
  - DETENER está en una barra fija, visible en todas las pestañas. Mide 88 dp, se toca una sola vez y no depende de otros envíos. Con la celda parada, la barra muestra «Celda detenida» y Reanudar.
  - Enviar ≠ ejecutar: el estado `enviando` muestra «Enviando…» y deshabilita los botones. Si ya hay un envío en curso, un toque más se ignora. La pinza cambia solo si el robot acepta; si rechaza, aparece un aviso y se conserva el estado anterior.
  - La batería baja se muestra con texto «⚠ BATERÍA BAJA», borde y letra blanca, además del color. Las alarmas del historial llevan el prefijo «⚠ ALARMA».
  - Los controles miden al menos 48 dp: `TextButton` para Borrar, `.clickable{}.padding()` en las filas y botones de pinza de 64 dp.
  - Compartir usa `type = "text/plain"` y `Intent.createChooser`.
  - Los textos de Panel y de la parada están en `strings.xml`.
- **Verificación:**
  - DETENER funciona desde las 3 pestañas, también durante «Enviando…».
  - Con «Demora larga», dos toques a Cerrar pinza suman 1 en «Comandos recibidos».
  - Con «Rechazar el próximo comando» aparece el aviso y la pinza no cambia.
  - Con «Batería baja» se ve el texto de alarma.
  - Compartir abre el selector.
