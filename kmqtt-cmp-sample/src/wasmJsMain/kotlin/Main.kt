import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalComposeUiApi::class, ExperimentalUnsignedTypes::class, ExperimentalTime::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "canvas") {
        App()
    }
}
