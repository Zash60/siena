package com.example.siena
object SienaNative {
    init { System.loadLibrary("siena") }
    const val BTN_A=0; const val BTN_B=1; const val BTN_X=2; const val BTN_Y=3;
    const val BTN_START=4; const val BTN_SELECT=5; const val BTN_UP=6; const val BTN_DOWN=7;
    const val BTN_LEFT=8; const val BTN_RIGHT=9; const val BTN_L=10; const val BTN_R=11;
    external fun init(r: ByteArray, i: ByteArray): Boolean
    external fun tickFrame()
    external fun getPixels(b: IntArray)
    external fun sendInput(id: Int, p: Boolean)
}
