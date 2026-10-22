package com.abbaacarraahawwi

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.security.SecureRandom

class MainActivity : AppCompatActivity() {

    companion object {
        const val MIN = 1
        const val MAX = 2000
        const val PRIZES = 3
        const val SPIN_STEPS = 30         // number of rolling ticks
        const val REVEAL_HOLD_MS = 3200L  // pause on the winning number (applause time)
    }

    private val random = SecureRandom()
    private val handler = Handler(Looper.getMainLooper())
    private val winners = mutableListOf<Int>()

    private lateinit var numberText: TextView
    private lateinit var prizeLabel: TextView
    private lateinit var congratsText: TextView
    private lateinit var drawButton: MaterialButton
    private lateinit var confetti: ConfettiView
    private lateinit var valueViews: List<TextView>
    private var player: MediaPlayer? = null
    private var spinning = false

    private val red = Color.parseColor("#DA121A")
    private val green = Color.parseColor("#078930")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        numberText = findViewById(R.id.numberText)
        prizeLabel = findViewById(R.id.prizeLabel)
        congratsText = findViewById(R.id.congratsText)
        drawButton = findViewById(R.id.drawButton)
        confetti = findViewById(R.id.confetti)
        valueViews = listOf(R.id.value1, R.id.value2, R.id.value3).map { findViewById<TextView>(it) }

        listOf(R.id.label1, R.id.label2, R.id.label3).forEachIndexed { i, id ->
            findViewById<TextView>(id).text = getString(R.string.prize_both, i + 1)
        }

        drawButton.setOnClickListener { onDrawClicked() }
        refresh()
    }

    private fun refresh() {
        for (i in 0 until PRIZES) {
            valueViews[i].text = if (i < winners.size) winners[i].toString() else "—"
        }
        if (winners.size < PRIZES) {
            prizeLabel.text = getString(R.string.prize_both, winners.size + 1)
            drawButton.text = getString(R.string.draw_next, winners.size + 1)
        } else {
            prizeLabel.text = getString(R.string.done)
            drawButton.text = getString(R.string.restart)
        }
    }

    private fun onDrawClicked() {
        if (spinning) return
        if (winners.size >= PRIZES) {
            winners.clear()
            numberText.text = "?"
            numberText.setTextColor(red)
            congratsText.animate().alpha(0f).setDuration(200).start()
            refresh()
            return
        }
        startDraw()
    }

    private fun nextWinner(): Int {
        var n: Int
        do { n = MIN + random.nextInt(MAX - MIN + 1) } while (winners.contains(n))
        return n
    }

    private fun startDraw() {
        val result = nextWinner()
        spinning = true
        drawButton.isEnabled = false
        drawButton.text = getString(R.string.drawing)
        numberText.setTextColor(red)
        numberText.scaleX = 1f
        numberText.scaleY = 1f
        congratsText.animate().alpha(0f).setDuration(200).start()

        var step = 0
        val tick = object : Runnable {
            override fun run() {
                if (step < SPIN_STEPS) {
                    numberText.text = (MIN + random.nextInt(MAX - MIN + 1)).toString()
                    step++
                    // starts fast (~40 ms) and slows down (~380 ms) for suspense
                    val delay = (40 + 0.4 * step * step).toLong()
                    handler.postDelayed(this, delay)
                } else {
                    reveal(result)
                }
            }
        }
        handler.post(tick)
    }

    private fun reveal(result: Int) {
        numberText.text = result.toString()
        numberText.setTextColor(green)
        numberText.animate().scaleX(1.25f).scaleY(1.25f).setDuration(300).start()

        congratsText.animate().alpha(1f).setDuration(400).start()
        confetti.burst()
        playApplause()

        // hold the winning number on screen, then record it in the list
        handler.postDelayed({
            numberText.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
            winners.add(result)
            spinning = false
            drawButton.isEnabled = true
            refresh()
        }, REVEAL_HOLD_MS)
    }

    private fun playApplause() {
        try {
            player?.release()
            val mp = MediaPlayer.create(this, R.raw.applause)
            mp?.setOnCompletionListener {
                it.release()
                if (player === it) player = null
            }
            player = mp
            mp?.start()
        } catch (e: Exception) {
            // sound is optional; ignore any playback problem
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        player?.release()
        player = null
        super.onDestroy()
    }
}
