package ar.celda.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ar.celda.logica.Evento
import ar.celda.logica.TipoEvento
import ar.celda.logica.coincide

private val TIPOS = listOf("Todos", "Velocidad", "Pinza", "Parada", "Alarma")

@Composable
fun Historial(model: PanelModel, compartir: () -> Unit) {
    var tipo by rememberSaveable { mutableStateOf("Todos") }
    var busqueda by rememberSaveable { mutableStateOf("") }
    val filtrados = model.eventos.filter {
        (tipo == "Todos" || it.tipo.name.equals(tipo, ignoreCase = true)) && coincide(it, busqueda)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TIPOS.forEach { opcion ->
                FilterChip(selected = tipo == opcion, onClick = { tipo = opcion }, label = { Text(opcion) })
            }
        }
        TextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = { Text("Buscar") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${model.eventos.size} eventos", Modifier.weight(1f))
            TextButton(onClick = compartir) { Text("Compartir reporte") }
        }
        if (filtrados.isEmpty()) Text("No hay eventos.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
            // key = id: el estado de cada fila (expandido) queda atado al evento, no a la posición.
            items(filtrados, key = { it.id }) { evento ->
                FilaEvento(evento, onBorrar = { model.borrar(evento.id) })
            }
        }
    }
}

@Composable
private fun FilaEvento(evento: Evento, onBorrar: () -> Unit) {
    var expandido by rememberSaveable { mutableStateOf(false) }
    val fondo = if (evento.tipo == TipoEvento.ALARMA) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.surfaceVariant
    Card(colors = CardDefaults.cardColors(containerColor = fondo), modifier = Modifier.fillMaxWidth()) {
        // clickable antes que padding: toda la tarjeta responde al toque, no solo el contenido.
        Column(Modifier.clickable { expandido = !expandido }.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val alarma = if (evento.tipo == TipoEvento.ALARMA) "⚠ ALARMA · " else ""
                Text("$alarma${evento.hora} · ${evento.texto}", Modifier.weight(1f))
                // TextButton: área táctil mínima de 48 dp (el Text con clickable medía solo el texto).
                TextButton(onClick = onBorrar, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            }
            if (expandido) Text(evento.detalle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
