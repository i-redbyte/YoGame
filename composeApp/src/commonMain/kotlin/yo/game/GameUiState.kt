package yo.game

import kotlin.math.max
import kotlin.math.min

data class GameUiState(
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val isReady: Boolean = true,
    val showInfo: Boolean = false,

    val score: Int = 0,
    val speedLevel: Int = 1,
    val lives: Int = 3,

    val cols: Int = 16,
    val rows: Int = 22,

    val word: String = "",
    val wordCells: List<Char> = emptyList(),
    val missingIndex: Int = 0,
    val wordX: Int = 0,

    val dropCol: Int = 0,
    val dropY: Float = -1.2f,

    val effect: WordEffect = WordEffect.None,
    val effectT: Float = 0f,

    val particles: List<Particle> = emptyList()
) {
    val targetCol: Int get() = wordX + missingIndex
}

sealed interface WordEffect {
    data object None : WordEffect
    data object SuccessWave : WordEffect
    data object FailBurn : WordEffect
}

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val t: Float,
    val life: Float,
    val kind: ParticleKind
)

enum class ParticleKind { Good, Bad }

internal fun clampInt(v: Int, a: Int, b: Int): Int = max(a, min(b, v))
