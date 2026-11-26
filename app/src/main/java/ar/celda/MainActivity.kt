package ar.celda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ar.celda.ui.App
import ar.celda.ui.PanelModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // viewModels(): el mismo PanelModel sobrevive a la rotación (no se crea uno nuevo por Activity).
    private val model: PanelModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Telemetría solo mientras la app está visible: repeatOnLifecycle(STARTED) lanza el bucle
        // en onStart y lo cancela en onStop. Al rotar, la Activity vieja cancela el suyo al
        // destruirse, así que nunca hay dos bucles a la vez.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.vigilarTelemetria()
            }
        }
        setContent { App(model) }
    }
}
