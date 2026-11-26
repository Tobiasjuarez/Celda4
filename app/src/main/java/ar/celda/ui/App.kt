package ar.celda.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun App(model: PanelModel) {
    var seccion by rememberSaveable { mutableStateOf("Panel") }
    val context = LocalContext.current
    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF1F5A8A), secondary = Color(0xFF8A5A1F))) {
        Scaffold(bottomBar = {
            NavigationBar {
                listOf("Panel" to "◉", "Historial" to "☰", "Banco" to "⚙").forEach { (nombre, icono) ->
                    NavigationBarItem(
                        selected = seccion == nombre,
                        onClick = { seccion = nombre },
                        icon = { Text(icono) },
                        label = { Text(nombre) },
                    )
                }
            }
        }) { margen ->
            Column(
                Modifier.fillMaxSize().padding(margen).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Spacer(Modifier.height(8.dp))
                Text("Celda 4 · Panel de la pinza", style = MaterialTheme.typography.labelLarge)
                Text(seccion, style = MaterialTheme.typography.headlineLarge)
                when (seccion) {
                    "Panel" -> Panel(model)
                    "Historial" -> Historial(model) { compartirReporte(context, model.reporte()) }
                    else -> Banco()
                }
            }
        }
    }
}

private fun compartirReporte(context: Context, texto: String) {
    val intent = Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, texto)
    context.startActivity(intent)
}
