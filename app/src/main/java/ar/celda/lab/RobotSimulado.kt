package ar.celda.lab

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

enum class Comando { ABRIR, CERRAR, DETENER, REANUDAR }

data class Telemetria(val bateria: Int, val temperatura: Double)

/**
 * Robot de prueba para el banco de la celda. No forma parte de los pedidos.
 * Los contadores vuelven a cero cuando muere el proceso.
 */
object RobotSimulado {
    private val _consultas = MutableStateFlow(0)
    /** Lecturas de telemetría desde que arrancó el proceso. */
    val consultas: StateFlow<Int> = _consultas.asStateFlow()

    private val _comandos = MutableStateFlow(0)
    /** Comandos recibidos desde que arrancó el proceso (incluye los rechazados). */
    val comandos: StateFlow<Int> = _comandos.asStateFlow()

    val demoraLarga = MutableStateFlow(false)
    val rechazarProximo = MutableStateFlow(false)
    val bateriaBaja = MutableStateFlow(false)

    fun leerTelemetria(): Telemetria {
        val n = _consultas.updateAndGet { it + 1 }
        val bateria = if (bateriaBaja.value) 15 else 82
        val temperatura = 36.0 + (n % 10) / 10.0
        return Telemetria(bateria, temperatura)
    }

    /** Devuelve true si el robot acepta y ejecuta el comando. La parada nunca se rechaza. */
    suspend fun enviar(comando: Comando): Boolean {
        _comandos.update { it + 1 }
        val rechazar = rechazarProximo.value && comando != Comando.DETENER
        if (rechazar) rechazarProximo.value = false
        delay(if (demoraLarga.value) 6_000L else 1_500L)
        return !rechazar
    }
}
