package com.example.model

import androidx.compose.ui.graphics.Color

enum class ParticleType {
    SHARD,
    STAR,
    RING_SHOCKWAVE,
    LASER_BEAM_H,
    LASER_BEAM_V,
    RAINBOW_SPARKLE,
    FLOATING_TEXT,
    CONFETTI
}

data class Particle(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var size: Float,
    var alpha: Float = 1f,
    var life: Float = 1f,
    val maxLife: Float = 1f,
    val type: ParticleType = ParticleType.SHARD,
    var rotation: Float = 0f,
    var vRot: Float = 0f,
    var length: Float = 0f,
    val text: String = "",
    val gravity: Float = 600f
) {
    fun update(dt: Float): Boolean {
        life -= dt
        if (life <= 0f) return false

        alpha = (life / maxLife).coerceIn(0f, 1f)
        x += vx * dt
        y += vy * dt

        when (type) {
            ParticleType.SHARD, ParticleType.CONFETTI -> {
                vy += gravity * dt
                rotation += vRot * dt
            }
            ParticleType.STAR -> {
                rotation += vRot * dt
                size *= (1f + 0.3f * dt)
            }
            ParticleType.RING_SHOCKWAVE -> {
                size += 500f * dt
            }
            ParticleType.LASER_BEAM_H, ParticleType.LASER_BEAM_V -> {
                length += 1800f * dt
            }
            ParticleType.FLOATING_TEXT -> {
                vy -= 50f * dt // drift upwards
            }
            ParticleType.RAINBOW_SPARKLE -> {
                rotation += vRot * dt
            }
        }
        return true
    }
}
