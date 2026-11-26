package ar.celda.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.celda.lab.Comando
import ar.celda.lab.RobotSimulado
import ar.celda.lab.Telemetria
import ar.celda.logica.Evento
import ar.celda.logica.TipoEvento
import ar.celda.logica.Velocidad
import ar.celda.logica.evaluarVelocidad
import ar.celda.logica.formatoDecimal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class EstadoPinza { ABIERTA, CERRADA }

class PanelModel : ViewModel() {
    // Lista observable: agregar o borrar recompone el historial al instante.
    private val _eventos = mutableStateListOf<Evento>()
    val eventos: List<Evento> get() = _eventos

    var pinza by mutableStateOf(EstadoPinza.ABIERTA)
        private set
    var detenido by mutableStateOf(false)
        private set
    /** true desde que se manda una orden de pinza hasta que el robot responde. */
    var enviando by mutableStateOf(false)
        private set
    /** true si el robot rechazó la última orden de pinza. */
    var rechazada by mutableStateOf(false)
        private set
    var telemetria by mutableStateOf(Telemetria(bateria = 82, temperatura = 36.0))
        private set

    private var proximoId = 1L
    private var alarmaBateria = false

    /** Solo registra la velocidad si es un número dentro de 0..120. */
    fun aplicarVelocidad(texto: String): Velocidad {
        val resultado = evaluarVelocidad(texto)
        if (resultado is Velocidad.Valida) {
            registrar(
                TipoEvento.VELOCIDAD,
                "Velocidad ${resultado.valor}",
                "${formatoDecimal(resultado.porcentaje)} % · ${resultado.nivel.etiqueta}",
            )
        }
        return resultado
    }

    fun abrirPinza() = mover(Comando.ABRIR, EstadoPinza.ABIERTA, "Abrir pinza")

    fun cerrarPinza() = mover(Comando.CERRAR, EstadoPinza.CERRADA, "Cerrar pinza")

    /**
     * Enviar ≠ ejecutar: la pinza cambia de estado solo cuando el robot confirma.
     * Si ya hay un envío en curso, el toque se ignora (dos toques → un comando).
     */
    private fun mover(comando: Comando, destino: EstadoPinza, texto: String) {
        if (enviando) return
        enviando = true
        rechazada = false
        registrar(TipoEvento.PINZA, texto, "Orden enviada al robot")
        viewModelScope.launch {
            val aceptado = try {
                RobotSimulado.enviar(comando)
            } finally {
                enviando = false
            }
            if (aceptado) {
                pinza = destino
                registrar(TipoEvento.PINZA, "$texto: confirmado", "El robot ejecutó la orden")
            } else {
                rechazada = true
                registrar(TipoEvento.PINZA, "$texto: rechazado", "El robot rechazó la orden; la pinza conserva su estado")
            }
        }
    }

    /** La parada no depende de `enviando`: funciona aunque haya otra orden en curso. */
    fun detener() {
        detenido = true
        registrar(TipoEvento.PARADA, "Parada", "Parada solicitada por el operador")
        viewModelScope.launch { RobotSimulado.enviar(Comando.DETENER) }
    }

    fun reanudar() {
        detenido = false
        registrar(TipoEvento.PARADA, "Reanudar", "El operador reanudó la celda")
        viewModelScope.launch { RobotSimulado.enviar(Comando.REANUDAR) }
    }

    /** Lee la telemetría una vez por segundo hasta que se cancele la corrutina que la llama. */
    suspend fun vigilarTelemetria() {
        while (true) {
            actualizarTelemetria()
            delay(1_000L)   // delay es un punto de cancelación: al cancelar, el bucle termina acá
        }
    }

    private fun actualizarTelemetria() {
        val lectura = RobotSimulado.leerTelemetria()
        telemetria = lectura
        if (lectura.bateria < 20 && !alarmaBateria) {
            alarmaBateria = true
            registrar(TipoEvento.ALARMA, "Batería baja", "Batería al ${lectura.bateria} %")
        } else if (lectura.bateria >= 20) {
            alarmaBateria = false
        }
    }

    /** Se borra por id: el índice de la lista filtrada no coincide con el de la lista completa. */
    fun borrar(id: Long) {
        _eventos.removeAll { it.id == id }
    }

    fun reporte(): String = buildString {
        appendLine("Celda 4 · Reporte de turno")
        appendLine("Pinza: ${pinza.name.lowercase()} · Batería: ${telemetria.bateria} %")
        appendLine("Eventos: ${eventos.size}")
        eventos.forEach { appendLine("${it.hora} ${it.texto}") }
    }

    private fun registrar(tipo: TipoEvento, texto: String, detalle: String) {
        _eventos.add(Evento(proximoId++, tipo, texto, LocalTime.now().format(HORA), detalle))
    }

    private companion object {
        val HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
