package com.abbaacarraahawwi

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import java.security.SecureRandom

class MainActivity : AppCompatActivity() {

    companion object {
        const val MIN = 1
        const val MAX = 2000
        const val SPIN_STEPS = 20
        const val SPIN_DELAY_MS = 60L
    }

    private val random = SecureRandom()
    private val handler = Handler(Looper.getMainLooper())
    private val history = mutableListOf<Int>()

    private lateinit var numberText: TextView
    private lateinit var historyText: TextView
    private lateinit var pickButton: MaterialButton
    private lateinit var noRepeatSwitch: SwitchMaterial
    private var spinning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        numberText = findViewById(R.id.numberText)
        historyText = findViewById(R.id.historyText)
        pickButton = findViewById(R.id.pickButton)
        noRepeatSwitch = findViewById(R.id.noRepeatSwitch)
        findViewById<Button>(R.id.clearButton).setOnClickListener {
            history.clear()
            renderHistory()
        }

        pickButton.setOnClickListener { startPick() }
    }

    private fun nextNumber(): Int? {
        if (!noRepeatSwitch.isChecked) return MIN + random.nextInt(MAX - MIN + 1)
        if (history.size >= MAX - MIN + 1) return null
        var n: Int
        do { n = MIN + random.nextInt(MAX - MIN + 1) } while (history.contains(n))
        return n
    }

    private fun startPick() {
        if (spinning) return
        val result = nextNumber()
        if (result == null) {
            Toast.makeText(this, R.string.all_picked, Toast.LENGTH_SHORT).show()
            return
        }
        spinning = true
        pickButton.isEnabled = false
        var step = 0
        val tick = object : Runnable {
            override fun run() {
                step++
                if (step < SPIN_STEPS) {
                    numberText.text = (MIN + random.nextInt(MAX - MIN + 1)).toString()
                    handler.postDelayed(this, SPIN_DELAY_MS + step * 6L)
                } else {
                    numberText.text = result.toString()
                    numberText.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).withEndAction {
                        numberText.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                    }.start()
                    history.add(0, result)
                    renderHistory()
                    spinning = false
                    pickButton.isEnabled = true
                }
            }
        }
        handler.post(tick)
    }

    private fun renderHistory() {
        historyText.text = if (history.isEmpty()) "—" else history.take(30).joinToString("  •  ")
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
