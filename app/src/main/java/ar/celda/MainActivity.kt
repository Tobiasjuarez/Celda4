package ar.celda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ar.celda.ui.App
import ar.celda.ui.PanelModel
import java.util.Timer
import java.util.TimerTask

class MainActivity : ComponentActivity() {
    private val model = PanelModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Telemetría: batería y temperatura una vez por segundo.
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread { model.actualizarTelemetria() }
            }
        }, 0L, 1_000L)
        setContent { App(model) }
    }
}
