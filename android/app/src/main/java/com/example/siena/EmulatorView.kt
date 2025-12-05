package com.example.siena
import android.content.Context; import android.graphics.*; import android.util.AttributeSet; import android.view.View
class EmulatorView @JvmOverloads constructor(c: Context, a: AttributeSet? = null) : View(c, a) {
    private val bm: Bitmap = Bitmap.createBitmap(512, 448, Bitmap.Config.ARGB_8888)
    private val pb = IntArray(512 * 448)
    private val dst = Rect(); private val pt = Paint(Paint.FILTER_BITMAP_FLAG)
    init { bm.eraseColor(Color.BLACK) }
    fun updateFrame() { SienaNative.getPixels(pb); bm.setPixels(pb, 0, 512, 0, 0, 512, 448); invalidate() }
    override fun onDraw(c: Canvas) { super.onDraw(c); dst.set(0, 0, width, height); c.drawBitmap(bm, null, dst, pt) }
}
