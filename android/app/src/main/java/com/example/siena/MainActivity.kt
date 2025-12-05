package com.example.siena
import android.net.Uri; import android.os.Bundle; import android.view.MotionEvent; import android.view.View; import android.widget.Button; import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts; import androidx.appcompat.app.AppCompatActivity
import com.example.siena.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream; import java.util.concurrent.Executors; import java.util.concurrent.ScheduledFuture; import java.util.concurrent.TimeUnit
class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val exec = Executors.newSingleThreadScheduledExecutor()
    private var task: ScheduledFuture<*>? = null
    private var run = false
    private var bios: ByteArray? = null; private var rom: ByteArray? = null
    private val getBios = registerForActivityResult(ActivityResultContracts.GetContent()) { u -> u?.let { bios = read(it); Toast.makeText(this, "BIOS OK", 0).show(); tryStart() } }
    private val getRom = registerForActivityResult(ActivityResultContracts.GetContent()) { u -> u?.let { rom = read(it); Toast.makeText(this, "ROM OK", 0).show(); tryStart() } }
    override fun onCreate(s: Bundle?) { super.onCreate(s); b = ActivityMainBinding.inflate(layoutInflater); setContentView(b.root); ui() }
    private fun ui() {
        b.btnLoadBios.setOnClickListener { getBios.launch("*/*") }
        b.btnLoadRom.setOnClickListener { getRom.launch("*/*") }
        listOf(b.btnA to 0, b.btnB to 1, b.btnX to 2, b.btnY to 3, b.btnStart to 4, b.btnSelect to 5, b.btnUp to 6, b.btnDown to 7, b.btnLeft to 8, b.btnRight to 9, b.btnL to 10, b.btnR to 11).forEach { (btn, id) -> ctl(btn, id) }
    }
    private fun ctl(btn: Button, id: Int) { btn.setOnTouchListener { _, e -> when(e.action) { MotionEvent.ACTION_DOWN -> { SienaNative.sendInput(id, true); true } MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { SienaNative.sendInput(id, false); true } else -> false } } }
    private fun read(u: Uri): ByteArray {
        val s = contentResolver.openInputStream(u); val o = ByteArrayOutputStream(); val buf = ByteArray(1024); var l: Int
        s?.use { while (it.read(buf).also { l = it } != -1) o.write(buf, 0, l) }
        return o.toByteArray()
    }
    private fun tryStart() {
        if (bios != null && rom != null && !run) {
            if (SienaNative.init(rom!!, bios!!)) {
                b.layoutLoaders.visibility = View.GONE; b.emulatorView.visibility = View.VISIBLE; b.layoutControls.visibility = View.VISIBLE; start()
            } else Toast.makeText(this, "Erro Init", 1).show()
        }
    }
    private fun start() { run = true; task = exec.scheduleAtFixedRate({ if(run) { SienaNative.tickFrame(); runOnUiThread { b.emulatorView.updateFrame() } } }, 0, 16, TimeUnit.MILLISECONDS) }
    override fun onDestroy() { super.onDestroy(); run = false; task?.cancel(true); exec.shutdown() }
}
