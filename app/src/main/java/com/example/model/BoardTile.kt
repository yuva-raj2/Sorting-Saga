package com.example.model

data class BoardTile(
    val row: Int,
    val col: Int,
    var candy: Candy? = null,
    var hasJelly: Boolean = false,
    var isSelected: Boolean = false,
    var isHinted: Boolean = false,
    var isPopping: Boolean = false
) {
    val isEmpty: Boolean get() = candy == null
}

enum class SwipeDirection {
    UP, DOWN, LEFT, RIGHT
}
