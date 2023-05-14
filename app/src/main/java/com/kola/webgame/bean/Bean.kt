package com.kola.webgame.bean

data class IconConfig(
    val icon: String = "",
    var url: String = "",
    val open: Int = 0,
    val verticalBias: Float = 0.85f,
    val horizontalBias: Float = 0.75f,
    val showType: Int = 1,
    val showTimeX: Int = 20,
    val showTimeY: Int = 120,
    val showTimeZ: Int = 10
) {
    fun isOpen(): Boolean {
        return open == 1
    }

    fun alwaysShow(): Boolean {
        return showType == 1
    }
}