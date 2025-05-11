import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun <T> T.outlineTextFieldValidation(
    title: String,
    modifier: Modifier = Modifier,
    validate: String.() -> T,
    update: (T) -> Unit
) {
    var value by remember { mutableStateOf(this.toString()) }
    var error by remember { mutableStateOf("") }

    fun validate() {
        error = ""

        runCatching { value.validate() }
            .onSuccess { update(it) }
            .onFailure { error = it.message.toString() }
    }

    LaunchedEffect(value) {
        delay(300.milliseconds)
        validate()
    }

    OutlinedTextField(
        value = value,
        modifier = modifier,
        label = { Text(title) },
        supportingText = { Text(error) },
        isError = error.isNotEmpty(),
        singleLine = true,
        onValueChange = { value = it }
    )
}