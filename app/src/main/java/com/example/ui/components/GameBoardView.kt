package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.BoardTile
import com.example.model.SwipeDirection
import kotlin.math.abs

@Composable
fun GameBoardView(
    board: Array<Array<BoardTile>>,
    selectedTile: Pair<Int, Int>?,
    hintMove: Pair<Pair<Int, Int>, Pair<Int, Int>>?,
    onSwipe: (Int, Int, SwipeDirection) -> Unit,
    onTileTap: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = board.size
    val cols = board[0].size

    // Idle hint pulse animation
    val hintPulse = remember { Animatable(1f) }
    LaunchedEffect(hintMove) {
        if (hintMove != null) {
            hintPulse.animateTo(
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            hintPulse.snapTo(1f)
        }
    }

    var dragStartPos by remember { mutableStateOf<Offset?>(null) }
    var dragStartTile by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var dragHandled by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .shadow(16.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .testTag("game_board_container")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(cols.toFloat() / rows.toFloat())
                .testTag("game_board_canvas")
                .pointerInput(board) {
                    detectTapGestures { tapOffset ->
                        val cellW = size.width / cols
                        val cellH = size.height / rows
                        val c = (tapOffset.x / cellW).toInt().coerceIn(0, cols - 1)
                        val r = (tapOffset.y / cellH).toInt().coerceIn(0, rows - 1)
                        onTileTap(r, c)
                    }
                }
                .pointerInput(board) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            val cellW = size.width / cols
                            val cellH = size.height / rows
                            val c = (startOffset.x / cellW).toInt().coerceIn(0, cols - 1)
                            val r = (startOffset.y / cellH).toInt().coerceIn(0, rows - 1)
                            dragStartPos = startOffset
                            dragStartTile = r to c
                            dragHandled = false
                        },
                        onDragEnd = {
                            dragStartPos = null
                            dragStartTile = null
                            dragHandled = false
                        },
                        onDragCancel = {
                            dragStartPos = null
                            dragStartTile = null
                            dragHandled = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (dragHandled) return@detectDragGestures

                            val start = dragStartPos ?: return@detectDragGestures
                            val tile = dragStartTile ?: return@detectDragGestures
                            val current = change.position
                            val dx = current.x - start.x
                            val dy = current.y - start.y

                            val threshold = 32f // swipe threshold in pixels
                            if (abs(dx) > threshold || abs(dy) > threshold) {
                                val direction = if (abs(dx) > abs(dy)) {
                                    if (dx > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                                } else {
                                    if (dy > 0) SwipeDirection.DOWN else SwipeDirection.UP
                                }
                                onSwipe(tile.first, tile.second, direction)
                                dragHandled = true
                            }
                        }
                    )
                }
        ) {
            val cellW = size.width / cols
            val cellH = size.height / rows

            // Draw board frame background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF230739), Color(0xFF380C56), Color(0xFF1F0633))
                )
            )

            // Draw checkered grid cells & jellies
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val x = c * cellW
                    val y = r * cellH
                    val isLight = (r + c) % 2 == 0

                    val cellColor = if (isLight) Color(0x28FFFFFF) else Color(0x14000000)
                    drawRoundRect(
                        color = cellColor,
                        topLeft = Offset(x + 2f, y + 2f),
                        size = Size(cellW - 4f, cellH - 4f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )

                    // Draw Jelly layer if present
                    if (board[r][c].hasJelly) {
                        // Frosted pink/purple jelly glow
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x99FF4081), Color(0x55E040FB), Color(0x227C4DFF)),
                                center = Offset(x + cellW / 2, y + cellH / 2),
                                radius = cellW * 0.7f
                            ),
                            topLeft = Offset(x + 3f, y + 3f),
                            size = Size(cellW - 6f, cellH - 6f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        // Jelly border glint
                        drawRoundRect(
                            color = Color(0x88FFFFFF),
                            topLeft = Offset(x + 3f, y + 3f),
                            size = Size(cellW - 6f, cellH - 6f),
                            cornerRadius = CornerRadius(10f, 10f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }
                }
            }

            // Draw candies
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val tile = board[r][c]
                    val candy = tile.candy ?: continue

                    val x = c * cellW
                    val y = r * cellH
                    val rect = Rect(Offset(x, y), Size(cellW, cellH))

                    val isSelected = selectedTile?.first == r && selectedTile.second == c
                    val isHinted = (hintMove?.first?.first == r && hintMove.first.second == c) ||
                            (hintMove?.second?.first == r && hintMove.second.second == c)

                    val scale = when {
                        isSelected -> 1.12f
                        isHinted -> hintPulse.value
                        else -> 1.0f
                    }

                    CandyPainter.drawCandy(
                        drawScope = this,
                        candy = candy,
                        rect = rect,
                        pulseScale = scale,
                        isSelected = isSelected
                    )
                }
            }
        }
    }
}
