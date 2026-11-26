package ar.celda.logica

import java.util.Locale

const val VELOCIDAD_MAXIMA = 120
const val UMBRAL_ALTA = 80

/** Los porcentajes se muestran siempre con coma decimal, sin depender del idioma del dispositivo. */
private val LOCALE_AR: Locale = Locale.forLanguageTag("es-AR")

enum class TipoEvento { VELOCIDAD, PINZA, PARADA, ALARMA }

data class Evento(
    val id: Long,
    val tipo: TipoEvento,
    val texto: String,
    val hora: String,
    val detalle: String,
)

enum class Nivel(val etiqueta: String) { NORMAL("Normal"), ALTA("Alta") }

/** Resultado de evaluar lo que escribió el operador. */
sealed interface Velocidad {
    data class Valida(val valor: Int, val porcentaje: Double, val nivel: Nivel) : Velocidad
    data object FueraDeRango : Velocidad
    data object NoEsNumero : Velocidad
}

/** null si el texto no es un número entero (por ejemplo «8O», vacío o «abc»). */
fun leerVelocidad(texto: String): Int? = texto.trim().toIntOrNull()

/** Porcentaje respecto de la velocidad máxima. Se pasa a Double antes de dividir. */
fun porcentaje(velocidad: Int): Double = velocidad * 100.0 / VELOCIDAD_MAXIMA

/** null si la velocidad está fuera de 0..120. Los rangos no se superponen. */
fun nivel(velocidad: Int): Nivel? = when (velocidad) {
    in 0 until UMBRAL_ALTA -> Nivel.NORMAL
    in UMBRAL_ALTA..VELOCIDAD_MAXIMA -> Nivel.ALTA
    else -> null
}

fun evaluarVelocidad(texto: String): Velocidad {
    val valor = leerVelocidad(texto) ?: return Velocidad.NoEsNumero
    val nivel = nivel(valor) ?: return Velocidad.FueraDeRango
    return Velocidad.Valida(valor, porcentaje(valor), nivel)
}

fun formatoDecimal(valor: Double): String = String.format(LOCALE_AR, "%.1f", valor)

/** Búsqueda del historial por tipo o por texto, sin importar mayúsculas. */
fun coincide(evento: Evento, busqueda: String): Boolean {
    val buscado = busqueda.trim()
    return buscado.isEmpty() ||
        evento.tipo.name.contains(buscado, ignoreCase = true) ||
        evento.texto.contains(buscado, ignoreCase = true)
}
