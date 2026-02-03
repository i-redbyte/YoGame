package yo.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

class GameViewModel(
    words: List<String>,
    cols: Int = 16,
    rows: Int = 22
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Main.immediate)
    private val engine = GameEngine(words = words, cols = cols, rows = rows)

    private var running = false
    private var lastNanos: Long = 0L

    private val _state = MutableStateFlow(engine.initialState())
    val state: StateFlow<GameUiState> = _state

    fun start() {
        if (running) return
        running = true
        lastNanos = 0L
    }

    fun stop() {
        running = false
        lastNanos = 0L
    }

    fun onFrame(nanos: Long) {
        if (!running) return
        val current = _state.value
        if (current.isReady || current.isPaused || current.isGameOver || current.showInfo) {
            lastNanos = nanos
            return
        }

        val prev = lastNanos
        lastNanos = nanos
        if (prev == 0L) return

        val dt = min(0.033f, (nanos - prev).toFloat() / 1_000_000_000f)
        when (val result = engine.nextFrame(dt)) {
            is FrameResult.Progress -> _state.value = result.state
            is FrameResult.GameOver -> _state.value = result.state
        }
    }

    fun togglePause() {
        _state.update { it.copy(isPaused = !it.isPaused, showInfo = false) }
    }

    fun setInfoVisible(visible: Boolean) {
        _state.update { it.copy(showInfo = visible) }
    }

    fun startGame() {
        scope.launch {
            withContext(Dispatchers.Main.immediate) {
                engine.reset()
                _state.value = engine
                    .currentSnapshot(isReady = false, isPaused = false, isGameOver = false, showInfo = false)
                start()
            }
        }
    }

    fun restart() {
        startGame()
    }

    fun moveLeft() {
        val s = _state.value
        if (s.isReady || s.isPaused || s.isGameOver || s.showInfo) return
        engine.moveWord(-1)
        _state.value = engine
            .currentSnapshot(isReady = false, isPaused = false, isGameOver = false, showInfo = false)
    }

    fun moveRight() {
        val s = _state.value
        if (s.isReady || s.isPaused || s.isGameOver || s.showInfo) return
        engine.moveWord(1)
        _state.value = engine
            .currentSnapshot(isReady = false, isPaused = false, isGameOver = false, showInfo = false)
    }

    fun clear() {
        stop()
        job.cancel()
    }
}
