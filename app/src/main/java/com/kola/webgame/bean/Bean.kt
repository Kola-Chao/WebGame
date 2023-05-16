package com.kola.webgame.bean

data class IconConfig(
    val icon: String = "",
    val open: Int = 1,
    val verticalBias: Float = 0.2f,
    val horizontalBias: Float = 0.99f,
    val showType: Int = 2,
    val showTimeX: Int = 50,
    val showTimeY: Int = 10,
    val showTimeZ: Int = 999,
    val isLock: Int = 1,
) {
    fun isOpen(): Boolean {
        return open == 1
    }

    fun alwaysShow(): Boolean {
        return showType == 1
    }
}