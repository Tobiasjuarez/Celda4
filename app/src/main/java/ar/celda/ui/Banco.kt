package ar.celda.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.celda.lab.RobotSimulado
import kotlinx.coroutines.flow.MutableStateFlow

/** Controles del robot de prueba. No forma parte de los pedidos. */
@Composable
fun Banco() {
    val consultas by RobotSimulado.consultas.collectAsStateWithLifecycle()
    val comandos by RobotSimulado.comandos.collectAsStateWithLifecycle()
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Robot de prueba", style = MaterialTheme.typography.titleMedium)
        Text("Las opciones se aplican al próximo comando o a la próxima lectura.")
        Text("Consultas de telemetría en esta sesión: $consultas")
        Text("Comandos recibidos en esta sesión: $comandos")
        Opcion("Demora larga (6 segundos)", RobotSimulado.demoraLarga)
        Opcion("Rechazar el próximo comando", RobotSimulado.rechazarProximo)
        Opcion("Batería baja (15 %)", RobotSimulado.bateriaBaja)
    }
}

@Composable
private fun Opcion(texto: String, valor: MutableStateFlow<Boolean>) {
    val activo by valor.collectAsStateWithLifecycle()
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Switch(checked = activo, onCheckedChange = { valor.value = it })
        Text(texto)
    }
}
