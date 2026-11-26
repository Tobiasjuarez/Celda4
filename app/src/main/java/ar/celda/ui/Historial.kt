package ar.celda.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var tipo by remember { mutableStateOf("Todos") }
    var busqueda by remember { mutableStateOf("") }
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
            itemsIndexed(filtrados) { indice, evento ->
                FilaEvento(evento, onBorrar = { model.borrar(indice) })
            }
        }
    }
}

@Composable
private fun FilaEvento(evento: Evento, onBorrar: () -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    val fondo = if (evento.tipo == TipoEvento.ALARMA) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.surfaceVariant
    Card(colors = CardDefaults.cardColors(containerColor = fondo), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp).clickable { expandido = !expandido }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${evento.hora} · ${evento.texto}", Modifier.weight(1f))
                Text(
                    "Borrar",
                    modifier = Modifier.padding(8.dp).clickable { onBorrar() },
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (expandido) Text(evento.detalle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
