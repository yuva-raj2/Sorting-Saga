package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.Candy
import com.example.model.CandyColor
import com.example.model.SpecialType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object CandyPainter {

    fun drawCandy(
        drawScope: DrawScope,
        candy: Candy,
        rect: Rect,
        pulseScale: Float = 1f,
        isSelected: Boolean = false
    ) {
        val cx = rect.center.x
        val cy = rect.center.y
        val baseRadius = min(rect.width, rect.height) * 0.44f * pulseScale

        // Draw shadow
        drawScope.drawOval(
            color = Color(0x35000000),
            topLeft = Offset(cx - baseRadius * 0.85f, cy + baseRadius * 0.65f),
            size = Size(baseRadius * 1.7f, baseRadius * 0.4f)
        )

        // Draw selection halo if selected
        if (isSelected) {
            drawScope.drawCircle(
                color = Color(0xFFFFFFFF),
                radius = baseRadius * 1.25f,
                center = Offset(cx, cy),
                style = Stroke(width = 4.5f)
            )
            drawScope.drawCircle(
                color = candy.color.primaryColor.copy(alpha = 0.4f),
                radius = baseRadius * 1.38f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.5f)
            )
        }

        // Color Bomb has unique chocolate disco-ball rendering
        if (candy.special == SpecialType.COLOR_BOMB) {
            drawColorBomb(drawScope, cx, cy, baseRadius)
            return
        }

        // Draw base candy shape based on color
        when (candy.color) {
            CandyColor.RED -> drawHeartCandy(drawScope, cx, cy, baseRadius, candy.color)
            CandyColor.ORANGE -> drawLozengeCandy(drawScope, cx, cy, baseRadius, candy.color)
            CandyColor.YELLOW -> drawStarCandy(drawScope, cx, cy, baseRadius, candy.color)
            CandyColor.GREEN -> drawGumdropCandy(drawScope, cx, cy, baseRadius, candy.color)
            CandyColor.BLUE -> drawSphereCandy(drawScope, cx, cy, baseRadius, candy.color)
            CandyColor.PURPLE -> drawJewelCandy(drawScope, cx, cy, baseRadius, candy.color)
        }

        // Draw special overlay enhancements
        when (candy.special) {
            SpecialType.STRIPED_HORIZONTAL -> drawHorizontalStripes(drawScope, cx, cy, baseRadius)
            SpecialType.STRIPED_VERTICAL -> drawVerticalStripes(drawScope, cx, cy, baseRadius)
            SpecialType.WRAPPED -> drawWrappedPackage(drawScope, cx, cy, baseRadius)
            SpecialType.COLOR_BOMB, SpecialType.NONE -> {}
        }
    }

    private fun drawHeartCandy(drawScope: DrawScope, cx: Float, cy: Float, r: Float, candyColor: CandyColor) {
        val path = Path().apply {
            val topY = cy - r * 0.7f
            moveTo(cx, topY + r * 0.4f)
            cubicTo(
                cx - r * 0.85f, topY - r * 0.35f,
                cx - r * 1.15f, topY + r * 0.75f,
                cx, cy + r * 1.0f
            )
            cubicTo(
                cx + r * 1.15f, topY + r * 0.75f,
                cx + r * 0.85f, topY - r * 0.35f,
                cx, topY + r * 0.4f
            )
            close()
        }

        val brush = Brush.radialGradient(
            colors = listOf(candyColor.lightColor, candyColor.primaryColor, candyColor.darkColor),
            center = Offset(cx - r * 0.25f, cy - r * 0.3f),
            radius = r * 1.2f
        )
        drawScope.drawPath(path, brush = brush)

        // Gloss highlight
        drawScope.drawOval(
            color = Color(0x80FFFFFF),
            topLeft = Offset(cx - r * 0.55f, cy - r * 0.5f),
            size = Size(r * 0.45f, r * 0.25f)
        )
    }

    private fun drawLozengeCandy(drawScope: DrawScope, cx: Float, cy: Float, r: Float, candyColor: CandyColor) {
        val brush = Brush.radialGradient(
            colors = listOf(candyColor.lightColor, candyColor.primaryColor, candyColor.darkColor),
            center = Offset(cx - r * 0.3f, cy - r * 0.3f),
            radius = r * 1.2f
        )
        // Oval Lozenge
        drawScope.drawOval(
            brush = brush,
            topLeft = Offset(cx - r * 0.95f, cy - r * 0.7f),
            size = Size(r * 1.9f, r * 1.4f)
        )
        // Gloss highlight arc
        drawScope.drawOval(
            color = Color(0x88FFFFFF),
            topLeft = Offset(cx - r * 0.65f, cy - r * 0.55f),
            size = Size(r * 0.75f, r * 0.32f)
        )
    }

    private fun drawStarCandy(drawScope: DrawScope, cx: Float, cy: Float, r: Float, candyColor: CandyColor) {
        val path = Path()
        val numPoints = 5
        val outerRadius = r * 1.05f
        val innerRadius = r * 0.48f

        for (i in 0 until numPoints * 2) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = i * PI / numPoints - PI / 2
            val x = cx + (radius * cos(angle)).toFloat()
            val y = cy + (radius * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        val brush = Brush.radialGradient(
            colors = listOf(candyColor.lightColor, candyColor.primaryColor, candyColor.darkColor),
            center = Offset(cx, cy - r * 0.2f),
            radius = r * 1.1f
        )
        drawScope.drawPath(path, brush = brush)

        // Center sweet star shine
        drawScope.drawCircle(
            color = Color(0x99FFFFFF),
            radius = r * 0.28f,
            center = Offset(cx - r * 0.15f, cy - r * 0.2f)
        )
    }

    private fun drawGumdropCandy(drawScope: DrawScope, cx: Float, cy: Float, r: Float, candyColor: CandyColor) {
        val path = Path().apply {
            val left = cx - r * 0.85f
            val right = cx + r * 0.85f
            val top = cy - r * 0.8f
            val bottom = cy + r * 0.85f

            moveTo(left + r * 0.3f, top)
            lineTo(right - r * 0.3f, top)
            cubicTo(right, top, right + r * 0.1f, bottom - r * 0.4f, right, bottom)
            lineTo(left, bottom)
            cubicTo(left - r * 0.1f, bottom - r * 0.4f, left, top, left + r * 0.3f, top)
            close()
        }

        val brush = Brush.radialGradient(
            colors = listOf(candyColor.lightColor, candyColor.primaryColor, candyColor.darkColor),
            center = Offset(cx - r * 0.25f, cy - r * 0.3f),
            radius = r * 1.1f
        )
        drawScope.drawPath(path, brush = brush)

        // Sugar crystal sprinkles
        drawScope.drawCircle(Color(0xA0FFFFFF), r * 0.08f, Offset(cx - r * 0.35f, cy - r * 0.2f))
        drawScope.drawCircle(Color(0xA0FFFFFF), r * 0.07f, Offset(cx + r * 0.3f, cy - r * 0.3f))
        drawScope.drawCircle(Color(0xA0FFFFFF), r * 0.09f, Offset(cx - r * 0.1f, cy + r * 0.25f))
        drawScope.drawCircle(Color(0xA0FFFFFF), r * 0.06f, Offset(cx + r * 0.4f, cy + r * 0.15f))

        // Gloss
        drawScope.drawOval(
            color = Color(0x70FFFFFF),
            topLeft = Offset(cx - r * 0.6f, cy - r * 0.65f),
            size = Size(r * 0.6f, r * 0.28f)
        )
    }

    private fun drawSphereCandy(drawScope: DrawScope, cx: Float, cy: Float, r: Float, candyColor: CandyColor) {
        val brush = Brush.radialGradient(
            colors = listOf(candyColor.lightColor, candyColor.primaryColor, candyColor.darkColor),
            center = Offset(cx - r * 0.3f, cy - r * 0.35f),
            radius = r * 1.1f
        )
        drawScope.drawCircle(
            brush = brush,
            radius = r * 0.95f,
            center = Offset(cx, cy)
        )
        // 3D specular shine
        drawScope.drawCircle(
            color = Color(0xB0FFFFFF),
            radius = r * 0.26f,
            center = Offset(cx - r * 0.32f, cy - r * 0.35f)
        )
    }

    private fun drawJewelCandy(drawScope: DrawScope, cx: Float, cy: Float, r: Float, candyColor: CandyColor) {
        val path = Path()
        val numSides = 6
        for (i in 0 until numSides) {
            val angle = i * 2 * PI / numSides - PI / 6
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy + (r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        val brush = Brush.radialGradient(
            colors = listOf(candyColor.lightColor, candyColor.primaryColor, candyColor.darkColor),
            center = Offset(cx - r * 0.25f, cy - r * 0.3f),
            radius = r * 1.15f
        )
        drawScope.drawPath(path, brush = brush)

        // Jewel facet lines
        for (i in 0 until numSides) {
            val angle = i * 2 * PI / numSides - PI / 6
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy + (r * sin(angle)).toFloat()
            drawScope.drawLine(
                color = Color(0x60FFFFFF),
                start = Offset(cx, cy),
                end = Offset(x, y),
                strokeWidth = 2f
            )
        }

        // Sparkle glint
        drawScope.drawCircle(
            color = Color(0xD0FFFFFF),
            radius = r * 0.2f,
            center = Offset(cx - r * 0.25f, cy - r * 0.3f)
        )
    }

    private fun drawHorizontalStripes(drawScope: DrawScope, cx: Float, cy: Float, r: Float) {
        val stripeColor = Color(0xCCFFFFFF)
        val strokeW = r * 0.2f
        drawScope.drawLine(
            color = stripeColor,
            start = Offset(cx - r * 0.85f, cy - r * 0.32f),
            end = Offset(cx + r * 0.85f, cy - r * 0.32f),
            strokeWidth = strokeW
        )
        drawScope.drawLine(
            color = stripeColor,
            start = Offset(cx - r * 0.85f, cy + r * 0.32f),
            end = Offset(cx + r * 0.85f, cy + r * 0.32f),
            strokeWidth = strokeW
        )
    }

    private fun drawVerticalStripes(drawScope: DrawScope, cx: Float, cy: Float, r: Float) {
        val stripeColor = Color(0xCCFFFFFF)
        val strokeW = r * 0.2f
        drawScope.drawLine(
            color = stripeColor,
            start = Offset(cx - r * 0.32f, cy - r * 0.85f),
            end = Offset(cx - r * 0.32f, cy + r * 0.85f),
            strokeWidth = strokeW
        )
        drawScope.drawLine(
            color = stripeColor,
            start = Offset(cx + r * 0.32f, cy - r * 0.85f),
            end = Offset(cx + r * 0.32f, cy + r * 0.85f),
            strokeWidth = strokeW
        )
    }

    private fun drawWrappedPackage(drawScope: DrawScope, cx: Float, cy: Float, r: Float) {
        // Wrapper bow twists on left and right
        val leftBow = Path().apply {
            moveTo(cx - r * 0.7f, cy)
            lineTo(cx - r * 1.25f, cy - r * 0.4f)
            lineTo(cx - r * 1.15f, cy)
            lineTo(cx - r * 1.3f, cy + r * 0.45f)
            close()
        }
        val rightBow = Path().apply {
            moveTo(cx + r * 0.7f, cy)
            lineTo(cx + r * 1.25f, cy - r * 0.4f)
            lineTo(cx + r * 1.15f, cy)
            lineTo(cx + r * 1.3f, cy + r * 0.45f)
            close()
        }

        drawScope.drawPath(leftBow, color = Color(0xEEFFFFFF))
        drawScope.drawPath(rightBow, color = Color(0xEEFFFFFF))

        // Cellophane shiny wrapper cross
        drawScope.drawCircle(
            color = Color(0x55FFFFFF),
            radius = r * 0.95f,
            center = Offset(cx, cy),
            style = Stroke(width = 4f)
        )
        drawScope.drawCircle(
            color = Color(0xAAFFFFFF),
            radius = r * 0.35f,
            center = Offset(cx, cy),
            style = Fill
        )
    }

    private fun drawColorBomb(drawScope: DrawScope, cx: Float, cy: Float, r: Float) {
        // Chocolate base sphere
        val chocoBrush = Brush.radialGradient(
            colors = listOf(Color(0xFF5D4037), Color(0xFF3E2723), Color(0xFF1B0000)),
            center = Offset(cx - r * 0.3f, cy - r * 0.35f),
            radius = r * 1.1f
        )
        drawScope.drawCircle(
            brush = chocoBrush,
            radius = r * 0.95f,
            center = Offset(cx, cy)
        )

        // Rainbow sprinkle dots scattered across the chocolate ball
        val sprinkleColors = listOf(
            Color(0xFFFF1744), Color(0xFFFFD600), Color(0xFF00E676),
            Color(0xFF00B0FF), Color(0xFFAA00FF), Color(0xFFFF6D00),
            Color(0xFFFFFFFF), Color(0xFFFF4081)
        )

        val dotOffsets = listOf(
            Offset(-0.45f, -0.45f), Offset(0.0f, -0.6f), Offset(0.45f, -0.4f),
            Offset(-0.6f, 0.05f), Offset(-0.2f, -0.1f), Offset(0.3f, -0.05f),
            Offset(0.6f, 0.1f), Offset(-0.4f, 0.45f), Offset(0.05f, 0.5f),
            Offset(0.45f, 0.4f), Offset(0.0f, 0.1f)
        )

        dotOffsets.forEachIndexed { i, off ->
            val color = sprinkleColors[i % sprinkleColors.size]
            drawScope.drawCircle(
                color = color,
                radius = r * 0.16f,
                center = Offset(cx + off.x * r, cy + off.y * r)
            )
            // Tiny white glint on sprinkle
            drawScope.drawCircle(
                color = Color(0xCCFFFFFF),
                radius = r * 0.06f,
                center = Offset(cx + off.x * r - r * 0.04f, cy + off.y * r - r * 0.04f)
            )
        }

        // Specular glow highlight
        drawScope.drawCircle(
            color = Color(0x60FFFFFF),
            radius = r * 0.28f,
            center = Offset(cx - r * 0.3f, cy - r * 0.35f)
        )

        // Subtle pulsing rainbow aura ring
        drawScope.drawCircle(
            color = Color(0x66FFD700),
            radius = r * 1.15f,
            center = Offset(cx, cy),
            style = Stroke(width = 3f)
        )
    }
}
