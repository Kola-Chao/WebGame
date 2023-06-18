package com.kola.webgame.bean

data class IconConfig(
    val icon: String = "https://firebasestorage.googleapis.com/v0/b/pop-stone.appspot.com/o/icon.gif?alt=media&token=18b138d5-11b9-4aea-a6ca-7088b3db17cd",
    val hd_url: String = "https://s.oksp.in/v1/spin/tml?pid=10772&appk=nZiTgPX3eXDXyIgBflNO49GO6gOTjxOF&did={did}",
    var open: Int = 0,
    val verticalBias: Float = 0.2f,
    val horizontalBias: Float = 0.99f,
    val showType: Int = 2,
    val showTimeX: Int = 30,
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