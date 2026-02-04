@file:OptIn(ExperimentalMaterial3Api::class)

package yo.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.geometry.*
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

@Composable
fun GameScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    val textMeasurer = rememberTextMeasurer()
    val cs = MaterialTheme.colorScheme
    val palette = remember(cs) {
        Palette(
            primary = cs.primary,
            secondary = cs.secondary,
            tertiary = cs.tertiary,
            error = cs.error,
            onSurface = cs.onSurface
        )
    }

    DisposableEffect(vm) {
        vm.start()
        onDispose { vm.stop() }
    }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { vm.onFrame(it) }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = cs.background) {
        Scaffold(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars),
            topBar = {
                TopAppBar(
                    title = { Text("Ё-Игра") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0x14000000),
                        titleContentColor = cs.onBackground
                    ),
                    actions = {
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 8.dp)) {
                            Text("Счёт: ${state.score}", color = cs.onBackground, fontSize = 12.sp)
                            Text(
                                "Скорость: ${state.speedLevel}",
                                color = cs.onBackground.copy(alpha = 0.65f),
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            "❤".repeat(max(0, state.lives)),
                            color = cs.error,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Spacer(Modifier.size(10.dp))
                    }
                )
            },
            bottomBar = {
                MobileControls(
                    isPaused = state.isPaused,
                    onLeft = vm::moveLeft,
                    onRight = vm::moveRight,
                    onPause = vm::togglePause,
                    onInfo = { vm.setInfoVisible(true) }
                )
            },
            containerColor = cs.background
        ) { contentPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF071018), Color(0xFF04070B)),
                            center = Offset.Zero,
                            radius = 1400f
                        )
                    )
            ) {
                val compact = maxWidth < 600.dp
                val side = if (compact) 12.dp else 24.dp
                val top = if (compact) 10.dp else 18.dp
                val bottom = if (compact) 12.dp else 18.dp

                val ratio = state.cols.toFloat() / state.rows.toFloat()
                val controlsH = 82.dp
                val availableH = (maxHeight - controlsH - top - bottom).coerceAtLeast(220.dp)
                val maxByWidth = (maxWidth / ratio)
                val boardH = availableH.coerceAtMost(maxByWidth).coerceAtLeast(240.dp)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = side, end = side, top = top, bottom = bottom),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GameBoard(
                        state = state,
                        palette = palette,
                        textMeasurer = textMeasurer,
                        onLeft = vm::moveLeft,
                        onRight = vm::moveRight,
                        onPause = vm::togglePause,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(boardH)
                            .clip(RoundedCornerShape(if (compact) 16.dp else 18.dp))
                            .background(Color(0x16000000))
                    )

                    Spacer(Modifier.weight(1f))

                    Overlay(
                        state = state,
                        onStart = vm::startGame,
                        onRestart = vm::restart,
                        onResume = vm::togglePause
                    )
                }
            }
        }
    }

    if (state.showInfo) {
        InfoDialog(onClose = { vm.setInfoVisible(false) })
    }
}

@Composable
private fun GameBoard(
    state: GameUiState,
    palette: Palette,
    textMeasurer: TextMeasurer,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var keyProcessed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                when (keyEvent.key) {
                    Key.DirectionLeft, Key.A -> {
                        if (keyEvent.type == KeyEventType.KeyDown && !keyProcessed) {
                            keyProcessed = true
                            onLeft()
                            true
                        } else if (keyEvent.type == KeyEventType.KeyUp) {
                            keyProcessed = false
                            true
                        } else false
                    }

                    Key.DirectionRight, Key.D -> {
                        if (keyEvent.type == KeyEventType.KeyDown && !keyProcessed) {
                            keyProcessed = true
                            onRight()
                            true
                        } else if (keyEvent.type == KeyEventType.KeyUp) {
                            keyProcessed = false
                            true
                        } else false
                    }

                    Key.Spacebar, Key.P -> {
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            onPause()
                            true
                        } else false
                    }

                    else -> false
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dx ->
                    val threshold = 18f
                    var acc = 0f
                    acc += dx
                    if (acc > threshold) {
                        onRight()
                    } else if (acc < -threshold) {
                        onLeft()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGame(state, palette, textMeasurer)
        }
    }
}


@Composable
private fun MobileControls(
    isPaused: Boolean,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onPause: () -> Unit,
    onInfo: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x26000000))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ControlButton("◀", cs.primary, onLeft)
        ControlButton(if (isPaused) "▶" else "⏸", cs.secondary, onPause)
        ControlButton("▶", cs.primary, onRight)
        ControlButton("i", cs.secondary, onInfo, size = 44.dp)
    }
}

@Composable
private fun ControlButton(label: String, color: Color, onClick: () -> Unit, size: Dp = 56.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1C000000))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, _ -> }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = color,
            fontSize = if (label == "i") 16.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.pointerInput(Unit) {
                detectHorizontalDragGestures { _, _ -> }
            }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.any { it.pressed }) onClick()
                        }
                    }
                }
        )
    }
}

@Composable
private fun Overlay(state: GameUiState, onStart: () -> Unit, onRestart: () -> Unit, onResume: () -> Unit) {
    when {
        state.isReady -> CenterDialog(
            "Ё-Игра",
            "Лови Ё ровно в пропуск",
            "Старт",
            onStart,
            null
        ) {}

        state.isPaused -> CenterDialog(
            "Пауза",
            "Нажми центральную кнопку для продолжения",
            "Продолжить",
            onResume,
            "Заново",
            onRestart
        )

        state.isGameOver -> CenterDialog(
            "Game Over",
            "Счёт: ${state.score}",
            "Заново",
            onRestart,
            null
        ) {}
    }
}

@Composable
private fun CenterDialog(
    title: String,
    text: String,
    primary: String,
    onPrimary: () -> Unit,
    secondary: String?,
    onSecondary: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { Button(onClick = onPrimary) { Text(primary) } },
        dismissButton = { if (secondary != null) OutlinedButton(onClick = onSecondary) { Text(secondary) } }
    )
}

@Composable
private fun InfoDialog(onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Управление") },
        text = { Text("Свайп влево/вправо по полю или кнопки внизу.\nЦентральная кнопка - пауза.\nНужно поймать Ё в пустую клетку.") },
        confirmButton = { Button(onClick = onClose) { Text("Ок") } }
    )
}

private fun DrawScope.drawGame(
    state: GameUiState,
    palette: Palette,
    textMeasurer: TextMeasurer
) {
    val cols = state.cols
    val rows = state.rows

    val pad = size.minDimension * 0.05f
    val usableW = size.width - pad * 2f
    val usableH = size.height - pad * 2f
    val cell = floor(min(usableW / cols, usableH / rows)).coerceAtLeast(6f)

    val gridW = cell * cols
    val gridH = cell * rows
    val ox = (size.width - gridW) * 0.5f
    val oy = (size.height - gridH) * 0.5f

    translate(ox, oy) {
        val gridA = palette.secondary.copy(alpha = 0.16f)
        val gridB = palette.secondary.copy(alpha = 0.10f)

        for (r in 0..rows) {
            val y = r * cell
            drawLine(
                if (r % 2 == 0) gridA else gridB,
                start = Offset(0f, y),
                end = Offset(gridW, y),
                strokeWidth = 1f
            )
        }
        for (c in 0..cols) {
            val x = c * cell
            drawLine(
                if (c % 2 == 0) gridA else gridB,
                start = Offset(x, 0f),
                end = Offset(x, gridH),
                strokeWidth = 1f
            )
        }

        val bottom = rows - 1
        val wordLen = state.wordCells.size

        val effect = state.effect
        val t = state.effectT
        val successP = if (effect == WordEffect.SuccessWave) (t / 0.22f).coerceIn(0f, 1f) else 0f
        val failP = if (effect == WordEffect.FailBurn) (t / 0.32f).coerceIn(0f, 1f) else 0f

        val wordCenterCol = state.wordX + (wordLen - 1) * 0.5f
        val wordCenterX = (wordCenterCol + 0.5f) * cell
        val wordCenterY = (bottom + 0.5f) * cell

        for (i in 0 until wordLen) {
            val col = state.wordX + i
            if (col !in 0..<cols) continue

            val ch = state.wordCells[i]
            val isHole = ch == '_'
            val baseBg = if (isHole) Color(0xAA0A1018) else palette.tertiary
            val baseFg = if (isHole) palette.onSurface.copy(alpha = 0f) else Color(0xFF060A0E)

            if (effect == WordEffect.FailBurn) {
                val cx = (col + 0.5f) * cell
                val cy = (bottom + 0.5f) * cell
                val k = (failP * failP).coerceIn(0f, 1f)
                val nx = cx + (wordCenterX - cx) * k
                val ny = cy + (wordCenterY - cy) * k
                val s = (1f - 0.75f * k).coerceAtLeast(0.15f)
                val tint = palette.error.copy(alpha = 0.65f * (1f - k))

                withTransform({
                    translate(left = nx - cx, top = ny - cy)
                    scale(s, s, pivot = Offset(cx, cy))
                }) {
                    drawTile(
                        cell, col,
                        bottom.toFloat(),
                        baseBg,
                        baseFg,
                        if (isHole) " " else ch.toString(),
                        textMeasurer
                    )
                    drawRoundRect(
                        color = tint,
                        topLeft = Offset(col * cell + cell * 0.12f, bottom * cell + cell * 0.12f),
                        size = Size(cell * 0.76f, cell * 0.76f),
                        cornerRadius = CornerRadius(cell * 0.16f, cell * 0.16f)
                    )
                }
            } else {
                drawTile(
                    cell,
                    col,
                    bottom.toFloat(),
                    baseBg,
                    baseFg,
                    if (isHole) " " else ch.toString(),
                    textMeasurer
                )
            }
        }

        val targetCol = state.targetCol
        if (targetCol in 0 until cols) {
            val x = targetCol * cell
            val y = bottom * cell
            drawRoundRect(
                color = palette.primary.copy(alpha = 0.55f),
                topLeft = Offset(x + cell * 0.10f, y + cell * 0.10f),
                size = Size(cell * 0.80f, cell * 0.80f),
                cornerRadius = CornerRadius(cell * 0.18f, cell * 0.18f),
                style = Stroke(width = max(2f, cell * 0.06f))
            )
        }

        if (effect == WordEffect.SuccessWave) {
            val waveX = (state.wordX + successP * (wordLen + 2) - 1f) * cell
            val waveW = cell * 1.2f
            val y = bottom * cell
            for (i in 0 until wordLen) {
                val col = state.wordX + i
                if (col !in 0..<cols) continue
                val cx = col * cell
                val d = abs((cx + cell * 0.5f) - (waveX + waveW * 0.5f))
                val a = 1f - (d / (cell * 1.2f)).coerceIn(0f, 1f)
                if (a <= 0f) continue
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            palette.primary.copy(alpha = 0f),
                            palette.primary.copy(alpha = 0.55f * a),
                            palette.secondary.copy(alpha = 0.35f * a),
                            palette.primary.copy(alpha = 0f)
                        ),
                        start = Offset(waveX, y),
                        end = Offset(waveX + waveW, y)
                    ),
                    topLeft = Offset(cx + cell * 0.08f, y + cell * 0.08f),
                    size = Size(cell * 0.84f, cell * 0.84f),
                    cornerRadius = CornerRadius(cell * 0.18f, cell * 0.18f)
                )
            }
        }

        if (state.dropCol in 0 until cols && state.dropY <= rows) {
            drawTile(
                cell,
                state.dropCol,
                state.dropY,
                palette.onSurface,
                Color(0xFF060A0E),
                "Ё",
                textMeasurer
            )
        }

        state.particles.forEach { p ->
            val a = 1f - (p.t / p.life).coerceIn(0f, 1f)
            val color = if (p.kind == ParticleKind.Good) palette.primary else palette.error
            drawCircle(
                color = color.copy(alpha = a * 0.85f),
                radius = (cell * 0.14f) * (0.35f + 0.65f * a),
                center = Offset(p.x * cell, p.y * cell)
            )
        }
    }
}

private fun DrawScope.drawTile(
    cell: Float,
    col: Int,
    rowY: Float,
    bg: Color,
    fg: Color,
    text: String,
    textMeasurer: TextMeasurer
) {
    val pad = max(2f, cell * 0.10f)
    val x = col * cell + pad
    val y = rowY * cell + pad
    val w = cell - pad * 2f
    val h = cell - pad * 2f
    val cr = max(8f, cell * 0.18f)

    drawRoundRect(
        color = bg,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(cr, cr)
    )

    drawRoundRect(
        color = Color(0x55000000),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(cr, cr),
        style = Stroke(width = max(1.5f, cell * 0.05f))
    )

    if (text.isBlank()) return

    val baseSize = cell * 0.58f
    val layout = textMeasurer.measure(
        text = text,
        style = TextStyle(
            fontSize = baseSize.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = fg
        )
    )

    val maxTextW = w * 0.82f
    val maxTextH = h * 0.82f
    val sx = (maxTextW / layout.size.width.toFloat()).coerceAtMost(1f)
    val sy = (maxTextH / layout.size.height.toFloat()).coerceAtMost(1f)
    val s = min(sx, sy).coerceAtLeast(0.01f)

    val cx = x + w * 0.5f
    val cy = y + h * 0.5f

    withTransform({
        scale(s, s, pivot = Offset(cx, cy))
    }) {
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                cx - layout.size.width * 0.5f,
                cy - layout.size.height * 0.5f
            )
        )
    }
}


private data class Palette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val error: Color,
    val onSurface: Color
)
