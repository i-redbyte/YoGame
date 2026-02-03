package yo.game

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun App(vm: GameViewModel) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF63FF7A),
            secondary = Color(0xFF43C9FF),
            tertiary = Color(0xFFFED84D),
            error = Color(0xFFFF4E68),
            background = Color(0xFF05070B),
            surface = Color(0xFF070B10),
            onPrimary = Color(0xFF04120A),
            onSecondary = Color(0xFF031018),
            onTertiary = Color(0xFF121004),
            onError = Color(0xFF140306),
            onBackground = Color(0xFFE7FFF2),
            onSurface = Color(0xFFE7FFF2)
        )
    ) {
        GameScreen(vm)
    }
}
