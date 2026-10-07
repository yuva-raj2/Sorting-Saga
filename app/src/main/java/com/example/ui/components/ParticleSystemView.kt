package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.model.Particle
import com.example.model.ParticleType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ParticleSystemView(
    particles: List<Particle>,
    modifier: Modifier = Modifier
) {
    if (particles.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val nativeCanvas = drawContext.canvas.nativeCanvas

        particles.forEach { p ->
            if (p.alpha <= 0.01f) return@forEach

            val drawColor = p.color.copy(alpha = p.alpha)

            when (p.type) {
                ParticleType.SHARD -> {
                    rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                        val path = Path().apply {
                            moveTo(p.x, p.y - p.size)
                            lineTo(p.x + p.size * 0.8f, p.y + p.size * 0.7f)
                            lineTo(p.x - p.size * 0.6f, p.y + p.size * 0.9f)
                            close()
                        }
                        drawPath(path, color = drawColor)
                    }
                }
                ParticleType.STAR, ParticleType.RAINBOW_SPARKLE -> {
                    rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                        val path = Path()
                        val numPoints = 4
                        val outer = p.size
                        val inner = p.size * 0.35f
                        for (i in 0 until numPoints * 2) {
                            val r = if (i % 2 == 0) outer else inner
                            val angle = i * PI / numPoints
                            val px = p.x + (r * cos(angle)).toFloat()
                            val py = p.y + (r * sin(angle)).toFloat()
                            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        path.close()
                        drawPath(path, color = drawColor)
                    }
                }
                ParticleType.RING_SHOCKWAVE -> {
                    drawCircle(
                        color = drawColor,
                        radius = p.size,
                        center = Offset(p.x, p.y),
                        style = Stroke(width = 6f * p.alpha)
                    )
                }
                ParticleType.LASER_BEAM_H -> {
                    drawLine(
                        color = drawColor,
                        start = Offset(p.x - p.length / 2, p.y),
                        end = Offset(p.x + p.length / 2, p.y),
                        strokeWidth = p.size * p.alpha
                    )
                    // Core white beam
                    drawLine(
                        color = Color.White.copy(alpha = p.alpha),
                        start = Offset(p.x - p.length / 2, p.y),
                        end = Offset(p.x + p.length / 2, p.y),
                        strokeWidth = (p.size * 0.4f) * p.alpha
                    )
                }
                ParticleType.LASER_BEAM_V -> {
                    drawLine(
                        color = drawColor,
                        start = Offset(p.x, p.y - p.length / 2),
                        end = Offset(p.x, p.y + p.length / 2),
                        strokeWidth = p.size * p.alpha
                    )
                    drawLine(
                        color = Color.White.copy(alpha = p.alpha),
                        start = Offset(p.x, p.y - p.length / 2),
                        end = Offset(p.x, p.y + p.length / 2),
                        strokeWidth = (p.size * 0.4f) * p.alpha
                    )
                }
                ParticleType.CONFETTI -> {
                    rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                        drawRect(
                            color = drawColor,
                            topLeft = Offset(p.x - p.size, p.y - p.size * 0.5f),
                            size = Size(p.size * 2f, p.size)
                        )
                    }
                }
                ParticleType.FLOATING_TEXT -> {
                    drawIntoCanvas {
                        val paint = Paint().apply {
                            color = p.color.copy(alpha = p.alpha).toArgb()
                            textSize = p.size
                            isFakeBoldText = true
                            textAlign = Paint.Align.CENTER
                            setShadowLayer(8f, 2f, 2f, android.graphics.Color.BLACK)
                        }
                        nativeCanvas.drawText(p.text, p.x, p.y, paint)
                    }
                }
            }
        }
    }
}
