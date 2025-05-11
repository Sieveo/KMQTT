import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun main() {
    application {
        //I set the window size specifically to this resolution so that it has enough space to display the hot reload
        //menu next to the window on an HD monitor
        val windowState = rememberWindowState(size = DpSize(1880.dp, 1080.dp))
        Window(title = "Sample MQTT Client", state = windowState, onCloseRequest = ::exitApplication) {
            App()
        }
    }
}
