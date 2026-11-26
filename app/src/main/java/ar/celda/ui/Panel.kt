package ar.celda.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.celda.R
import ar.celda.logica.Nivel
import ar.celda.logica.Velocidad
import ar.celda.logica.formatoDecimal

@Composable
fun Panel(model: PanelModel) {
    var velocidad by rememberSaveable { mutableStateOf("") }
    var resultado by remember { mutableStateOf<Velocidad?>(null) }
    var modoManual by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TarjetaTelemetria(model)

        // Enviar ≠ ejecutar: mientras el robot no responde, el estado es «Enviando…».
        val estado = when {
            model.enviando -> stringResource(R.string.pinza_enviando)
            model.pinza == EstadoPinza.CERRADA -> stringResource(R.string.pinza_cerrada)
            else -> stringResource(R.string.pinza_abierta)
        }
        Text(stringResource(R.string.pinza_estado, estado), style = MaterialTheme.typography.titleLarge)
        if (model.rechazada) {
            Text(stringResource(R.string.orden_rechazada), color = MaterialTheme.colorScheme.error)
        }

        // Deshabilitados durante un envío: el segundo toque no manda otro comando.
        val habilitados = !model.detenido && !model.enviando
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = model::abrirPinza,
                enabled = habilitados,
                modifier = Modifier.weight(1f).heightIn(min = 64.dp),
            ) { Text(stringResource(R.string.abrir_pinza)) }
            Button(
                onClick = model::cerrarPinza,
                enabled = habilitados,
                modifier = Modifier.weight(1f).heightIn(min = 64.dp),
            ) { Text(stringResource(R.string.cerrar_pinza)) }
        }

        Text(stringResource(R.string.velocidad_titulo), style = MaterialTheme.typography.titleMedium)
        TextField(
            value = velocidad,
            onValueChange = { velocidad = it },
            label = { Text(stringResource(R.string.velocidad_rango)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Button(
            onClick = { resultado = model.aplicarVelocidad(velocidad) },
            modifier = Modifier.heightIn(min = 56.dp),
        ) { Text(stringResource(R.string.aplicar)) }
        resultado?.let { Text(textoResultado(it)) }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Switch(checked = modoManual, onCheckedChange = { modoManual = it })
            Text(stringResource(R.string.modo_manual))
        }
    }
}

@Composable
private fun textoResultado(resultado: Velocidad): String = when (resultado) {
    is Velocidad.Valida -> stringResource(
        R.string.resultado_velocidad,
        formatoDecimal(resultado.porcentaje),
        stringResource(if (resultado.nivel == Nivel.ALTA) R.string.nivel_alta else R.string.nivel_normal),
    )
    Velocidad.FueraDeRango -> stringResource(R.string.fuera_de_rango)
    Velocidad.NoEsNumero -> stringResource(R.string.no_es_numero)
}

/** La batería baja se distingue por texto, ícono y borde, no solo por el color. */
@Composable
private fun TarjetaTelemetria(model: PanelModel) {
    val t = model.telemetria
    val baja = t.bateria < 20
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (baja) Color(0xFFB00020) else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (baja) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = if (baja) BorderStroke(3.dp, Color.Black) else null,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (baja) {
                Text(
                    stringResource(R.string.bateria_baja, t.bateria),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.bateria, t.bateria))
                Text(stringResource(R.string.temperatura, formatoDecimal(t.temperatura)))
            }
        }
    }
}
