package ar.celda.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.celda.lab.Comando
import ar.celda.lab.RobotSimulado
import ar.celda.lab.Telemetria
import ar.celda.logica.Evento
import ar.celda.logica.TipoEvento
import ar.celda.logica.leerVelocidad
import ar.celda.logica.nivel
import ar.celda.logica.porcentaje
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class EstadoPinza { ABIERTA, CERRADA }

class PanelModel : ViewModel() {
    val eventos = mutableListOf<Evento>()

    var pinza by mutableStateOf(EstadoPinza.ABIERTA)
        private set
    var detenido by mutableStateOf(false)
        private set
    var telemetria by mutableStateOf(Telemetria(bateria = 82, temperatura = 36.0))
        private set

    private var proximoId = 1L
    private var alarmaBateria = false

    fun aplicarVelocidad(texto: String): String {
        val v = leerVelocidad(texto)
        val resumen = "${porcentaje(v)} % · ${nivel(v)}"
        registrar(TipoEvento.VELOCIDAD, "Velocidad $v", resumen)
        return resumen
    }

    fun abrirPinza() = mover(Comando.ABRIR, EstadoPinza.ABIERTA, "Abrir pinza")

    fun cerrarPinza() = mover(Comando.CERRAR, EstadoPinza.CERRADA, "Cerrar pinza")

    private fun mover(comando: Comando, destino: EstadoPinza, texto: String) {
        pinza = destino
        registrar(TipoEvento.PINZA, texto, "Orden enviada al robot")
        viewModelScope.launch { RobotSimulado.enviar(comando) }
    }

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

    fun actualizarTelemetria() {
        val lectura = RobotSimulado.leerTelemetria()
        telemetria = lectura
        if (lectura.bateria < 20 && !alarmaBateria) {
            alarmaBateria = true
            registrar(TipoEvento.ALARMA, "Batería baja", "Batería al ${lectura.bateria} %")
        } else if (lectura.bateria >= 20) {
            alarmaBateria = false
        }
    }

    fun borrar(indice: Int) {
        eventos.removeAt(indice)
    }

    fun reporte(): String = buildString {
        appendLine("Celda 4 · Reporte de turno")
        appendLine("Pinza: ${pinza.name.lowercase()} · Batería: ${telemetria.bateria} %")
        appendLine("Eventos: ${eventos.size}")
        eventos.forEach { appendLine("${it.hora} ${it.texto}") }
    }

    private fun registrar(tipo: TipoEvento, texto: String, detalle: String) {
        eventos.add(Evento(proximoId++, tipo, texto, LocalTime.now().format(HORA), detalle))
    }

    private companion object {
        val HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
