package yo.game

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameEngine(
    private val words: List<String>,
    private val cols: Int,
    private val rows: Int
) {
    private val rng = Random.Default

    private var word: String = ""
    private var wordCells: List<Char> = emptyList()
    private var missingIndex: Int = 0
    private var wordX: Int = 0

    private var score: Int = 0
    private var speedLevel: Int = 1
    private var lives: Int = 3

    private var dropCol: Int = 0
    private var dropY: Float = -1.2f
    private var dropVy: Float = 0f

    private var effect: WordEffect = WordEffect.None
    private var effectT: Float = 0f
    private var pendingOutcome: PendingOutcome = PendingOutcome.None

    private var particles: List<Particle> = emptyList()

    fun initialState(): GameUiState {
        reset()
        return snapshot(isReady = true, isPaused = false, isGameOver = false, showInfo = false)
    }

    fun reset() {
        score = 0
        speedLevel = 1
        lives = 3
        particles = emptyList()
        effect = WordEffect.None
        effectT = 0f
        pendingOutcome = PendingOutcome.None
        pickWord()
        spawnDrop()
    }

    fun moveWord(delta: Int) {
        if (pendingOutcome != PendingOutcome.None) return
        val minX = -missingIndex
        val maxX = (cols - 1) - missingIndex
        wordX = clampInt(wordX + delta, minX, maxX)
    }

    fun nextFrame(dtSec: Float): FrameResult {
        particles = particles.mapNotNull { p ->
            val nt = p.t + dtSec
            if (nt >= p.life) null
            else {
                val nvy = p.vy + 520f * dtSec
                Particle(
                    x = p.x + p.vx * dtSec,
                    y = p.y + nvy * dtSec,
                    vx = p.vx,
                    vy = nvy,
                    t = nt,
                    life = p.life,
                    kind = p.kind
                )
            }
        }

        if (pendingOutcome != PendingOutcome.None) {
            val dur = if (pendingOutcome == PendingOutcome.Success) 0.22f else 0.32f
            effectT = (effectT + dtSec).coerceAtMost(dur)
            if (effectT >= dur) {
                applyOutcome()
                effect = WordEffect.None
                effectT = 0f
                pendingOutcome = PendingOutcome.None
            }
            return FrameResult
                .Progress(snapshot(isReady = false, isPaused = false, isGameOver = lives <= 0, showInfo = false))
        }

        dropY += dropVy * dtSec

        val hitLine = (rows - 1) - 0.10f
        if (dropY >= hitLine) {
            val hit = dropCol == (wordX + missingIndex)
            if (hit) {
                effect = WordEffect.SuccessWave
                effectT = 0f
                pendingOutcome = PendingOutcome.Success
                addBurst(kind = ParticleKind.Good, col = dropCol, row = rows - 1)
                return FrameResult
                    .Progress(snapshot(isReady = false, isPaused = false, isGameOver = false, showInfo = false))
            } else {
                effect = WordEffect.FailBurn
                effectT = 0f
                pendingOutcome = PendingOutcome.Fail
                addBurst(kind = ParticleKind.Bad, col = dropCol, row = rows - 1)
                return FrameResult
                    .Progress(snapshot(isReady = false, isPaused = false, isGameOver = false, showInfo = false))
            }
        }

        return FrameResult
            .Progress(snapshot(isReady = false, isPaused = false, isGameOver = false, showInfo = false))
    }

    fun currentSnapshot(isReady: Boolean, isPaused: Boolean, isGameOver: Boolean, showInfo: Boolean): GameUiState {
        return snapshot(isReady = isReady, isPaused = isPaused, isGameOver = isGameOver, showInfo = showInfo)
    }

    private fun applyOutcome() {
        when (pendingOutcome) {
            PendingOutcome.Success -> {
                score += 1
                speedLevel = speedFromScore(score)
                pickWord()
                spawnDrop()
            }
            PendingOutcome.Fail -> {
                lives -= 1
                if (lives > 0) {
                    pickWord()
                    spawnDrop()
                }
            }
            PendingOutcome.None -> Unit
        }
    }

    private fun pickWord() {
        val w = words[rng.nextInt(words.size)]
        word = w
        val idx = w.indexOf('ё')
        missingIndex = idx
        wordCells = w.mapIndexed { i, ch -> if (i == idx) '_' else ch }

        val targetCenter = (cols - 1) / 2
        val desiredX = targetCenter - missingIndex
        val minX = -missingIndex
        val maxX = (cols - 1) - missingIndex
        wordX = clampInt(desiredX, minX, maxX)
    }

    private fun spawnDrop() {
        dropCol = rng.nextInt(cols)
        dropY = -1.2f
        dropVy = 4.2f + speedLevel * 1.15f
    }

    private fun speedFromScore(score: Int): Int {
        val v = 1 + (score / 4)
        return clampInt(v, 1, 7)
    }

    private fun addBurst(kind: ParticleKind, col: Int, row: Int) {
        val count = if (kind == ParticleKind.Good) 22 else 14
        val centerX = col + 0.5f
        val centerY = row + 0.5f
        val list = ArrayList<Particle>(particles.size + count)
        list.addAll(particles)

        repeat(count) {
            val a = rng.nextFloat() * (2f * PI.toFloat())
            val s = if (kind == ParticleKind.Good){
                lerp(70f, 220f, rng.nextFloat())
            } else {
                lerp(60f, 180f, rng.nextFloat())
            }
            list.add(
                Particle(
                    x = centerX,
                    y = centerY,
                    vx = cos(a) * s,
                    vy = sin(a) * s,
                    t = 0f,
                    life = if (kind == ParticleKind.Good) {
                        lerp(0.25f, 0.55f, rng.nextFloat())
                    } else {
                        lerp(0.18f, 0.40f, rng.nextFloat())
                    },
                    kind = kind
                )
            )
        }
        particles = list
    }

    private fun snapshot(isReady: Boolean, isPaused: Boolean, isGameOver: Boolean, showInfo: Boolean): GameUiState {
        return GameUiState(
            isReady = isReady,
            isPaused = isPaused,
            isGameOver = isGameOver,
            showInfo = showInfo,
            score = score,
            speedLevel = speedLevel,
            lives = lives,
            cols = cols,
            rows = rows,
            word = word,
            wordCells = wordCells,
            missingIndex = missingIndex,
            wordX = wordX,
            dropCol = dropCol,
            dropY = dropY,
            effect = effect,
            effectT = effectT,
            particles = particles
        )
    }
}

sealed interface FrameResult {
    data class Progress(val state: GameUiState) : FrameResult
    data class GameOver(val state: GameUiState) : FrameResult
}

private enum class PendingOutcome { None, Success, Fail }

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
