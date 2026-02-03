package yo.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    val vm = rememberGameViewModel()
    App(vm = vm)
}

@Composable
private fun rememberGameViewModel(): GameViewModel {
    val vm = remember { GameViewModel(words = Words.list) }
    DisposableEffect(vm) {
        onDispose { vm.clear() }
    }
    return vm
}
