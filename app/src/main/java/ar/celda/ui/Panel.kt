package ar.celda.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.celda.logica.Velocidad
import ar.celda.logica.formatoPorcentaje
import androidx.compose.ui.unit.sp

@Composable
fun Panel(model: PanelModel) {
    var velocidad by rememberSaveable { mutableStateOf("") }
    var resultado by remember { mutableStateOf<Velocidad?>(null) }
    var modoManual by rememberSaveable { mutableStateOf(false) }
    var confirmarParada by remember { mutableStateOf(false) }

    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TarjetaTelemetria(model)

        Text(
            "Pinza: " + (if (model.pinza == EstadoPinza.CERRADA) "Cerrada" else "Abierta"),
            style = MaterialTheme.typography.titleLarge,
        )
        if (model.detenido) {
            Text("Celda detenida", style = MaterialTheme.typography.titleMedium)
            Button(onClick = model::reanudar) { Text("Reanudar") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = model::abrirPinza, enabled = !model.detenido) { Text("Abrir pinza") }
            Button(onClick = model::cerrarPinza, enabled = !model.detenido) { Text("Cerrar pinza") }
            OutlinedButton(onClick = { confirmarParada = true }, modifier = Modifier.height(36.dp)) {
                Text("Detener", fontSize = 12.sp)
            }
        }

        Text("Velocidad de la pinza", style = MaterialTheme.typography.titleMedium)
        TextField(
            value = velocidad,
            onValueChange = { velocidad = it },
            label = { Text("0 a 120") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Button(onClick = { resultado = model.aplicarVelocidad(velocidad) }) { Text("Aplicar") }
        resultado?.let { Text(textoResultado(it)) }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Switch(checked = modoManual, onCheckedChange = { modoManual = it })
            Text("Modo manual")
        }
    }

    if (confirmarParada) {
        AlertDialog(
            onDismissRequest = { confirmarParada = false },
            title = { Text("Confirmar parada") },
            text = { Text("¿Seguro que querés detener la celda?") },
            confirmButton = {
                TextButton(onClick = { confirmarParada = false; model.detener() }) { Text("Detener") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarParada = false }) { Text("Cancelar") }
            },
        )
    }
}

private fun textoResultado(resultado: Velocidad): String = when (resultado) {
    is Velocidad.Valida -> "${formatoPorcentaje(resultado.porcentaje)} % · ${resultado.nivel.etiqueta}"
    Velocidad.FueraDeRango -> "Fuera de rango"
    Velocidad.NoEsNumero -> "Ingresá un número entre 0 y 120"
}

@Composable
private fun TarjetaTelemetria(model: PanelModel) {
    val t = model.telemetria
    val baja = t.bateria < 20
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (baja) Color(0xFFD32F2F) else MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Batería ${t.bateria} %")
            Text("Temperatura ${"%.1f".format(t.temperatura)} °C")
        }
    }
}
