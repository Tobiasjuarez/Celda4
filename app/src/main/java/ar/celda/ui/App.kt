package ar.celda.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.celda.R

@Composable
fun App(model: PanelModel) {
    var seccion by rememberSaveable { mutableStateOf("Panel") }
    val context = LocalContext.current
    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF1F5A8A), secondary = Color(0xFF8A5A1F))) {
        Scaffold(bottomBar = {
            Column {
                // DETENER fuera de las pestañas: se ve y se toca desde cualquier sección.
                BarraParada(model)
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

/** Parada de emergencia: un solo toque, sin confirmación, y el control más grande de la pantalla. */
@Composable
private fun BarraParada(model: PanelModel) {
    Surface(tonalElevation = 3.dp) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (model.detenido) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.celda_detenida),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedButton(onClick = model::reanudar, modifier = Modifier.heightIn(min = 56.dp)) {
                        Text(stringResource(R.string.reanudar))
                    }
                }
            } else {
                Button(
                    onClick = model::detener,
                    modifier = Modifier.fillMaxWidth().height(88.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020), contentColor = Color.White),
                ) {
                    Text(stringResource(R.string.detener), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** ACTION_SEND necesita un type; el selector (createChooser) siempre existe, así que no hay crash. */
private fun compartirReporte(context: Context, texto: String) {
    val envio = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Reporte de turno · Celda 4")
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(envio, "Compartir reporte"))
}
