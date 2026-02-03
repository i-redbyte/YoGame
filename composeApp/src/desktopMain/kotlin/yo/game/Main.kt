package yo.game

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.Modifier

fun main() = application {
    val vm = GameViewModel(words = Words.list)

    Window(
        onCloseRequest = {
            vm.clear()
            exitApplication()
        },
        title = "Ё-Игра"
    ) {
        DisposableEffect(vm) {
            onDispose { vm.clear() }
        }

        Box(
            modifier = Modifier.onKeyEvent { e ->
                when (e.key) {
                    Key.DirectionLeft -> {
                        vm.moveLeft()
                        true
                    }
                    Key.DirectionRight -> {
                        vm.moveRight()
                        true
                    }
                    Key.Spacebar -> {
                        vm.togglePause()
                        true
                    }
                    else -> false
                }
            }
        ) {
            App(vm = vm)
        }
    }
}
