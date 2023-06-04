package com.kola.webgame.bean

data class IconConfig(
    val icon: String = "",
    val hd_url: String = "https://s.oksp.in/v1/spin/tml?pid=10772&amp;appk=nZiTgPX3eXDXyIgBflNO49GO6gOTjxOF&amp;did={did}",
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