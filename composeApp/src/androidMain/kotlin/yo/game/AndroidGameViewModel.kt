package yo.game

import androidx.lifecycle.ViewModel

class AndroidGameViewModel : ViewModel() {
    val vm = GameViewModel(words = Words.list)

    override fun onCleared() {
        vm.stop()
        super.onCleared()
    }
}
