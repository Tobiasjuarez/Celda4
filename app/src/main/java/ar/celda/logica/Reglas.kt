package ar.celda.logica

const val VELOCIDAD_MAXIMA = 120

enum class TipoEvento { VELOCIDAD, PINZA, PARADA, ALARMA }

data class Evento(
    val id: Long,
    val tipo: TipoEvento,
    val texto: String,
    val hora: String,
    val detalle: String,
)

/** Convierte lo que escribió el operador. */
fun leerVelocidad(texto: String): Int = texto.toInt()

/** Porcentaje respecto de la velocidad máxima. */
fun porcentaje(velocidad: Int): Int = velocidad / VELOCIDAD_MAXIMA * 100

fun nivel(velocidad: Int): String = when (velocidad) {
    in 0..VELOCIDAD_MAXIMA -> "Normal"
    in 80..VELOCIDAD_MAXIMA -> "Alta"
    else -> "Fuera de rango"
}

/** Búsqueda del historial por tipo o por texto. */
fun coincide(evento: Evento, busqueda: String): Boolean =
    evento.tipo.name.contains(busqueda) && evento.texto.contains(busqueda)
