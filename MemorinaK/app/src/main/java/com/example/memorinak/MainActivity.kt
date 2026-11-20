package com.example.memorinak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var firstCard: ImageView? = null
    private var secondCard: ImageView? = null
    private var isBusy = false
    private var matchedPairs = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(applicationContext)
        layout.orientation = LinearLayout.VERTICAL

        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.weight = 1.toFloat()

        val images = arrayOf(
            R.drawable.squarecat,
            R.drawable.cat_2,
            R.drawable.cat_3,
            R.drawable.cat_4,
            R.drawable.cat_5,
            R.drawable.cat_6,
            R.drawable.cat_7,
            R.drawable.cat_8
        )

        val cards = ArrayList<Int>()
        for (img in images) {
            cards.add(img)
            cards.add(img)
        }
        cards.shuffle()

        val catViews = ArrayList<ImageView>()
        for (i in 0 until 16) {
            catViews.add(
                ImageView(applicationContext).apply {
                    setImageResource(R.drawable.card_back)
                    layoutParams = params
                    tag = cards[i]
                    setOnClickListener(colorListener)
                }
            )
        }

        val rows = Array(4, { LinearLayout(applicationContext) })

        var count = 0
        for (view in catViews) {
            val row: Int = count / 4
            rows[row].addView(view)
            count++
        }
        for (row in rows) {
            layout.addView(row)
        }
        setContentView(layout)
    }

    suspend fun openCards(card: ImageView) {
        if (isBusy || card == firstCard || card.visibility != View.VISIBLE) {
            return
        }

        card.setImageResource(card.tag as Int)

        if (firstCard == null) {
            firstCard = card
        } else {
            secondCard = card
            isBusy = true

            delay(1000)

            if (firstCard?.tag == secondCard?.tag) {
                firstCard?.visibility = View.INVISIBLE
                secondCard?.visibility = View.INVISIBLE
                firstCard?.isClickable = false
                secondCard?.isClickable = false
                matchedPairs++

                if (matchedPairs == 8) {
                    Toast.makeText(applicationContext, "Победа! Все карты открыты!", Toast.LENGTH_LONG).show()
                }
            } else {
                firstCard?.setImageResource(R.drawable.card_back)
                secondCard?.setImageResource(R.drawable.card_back)
            }

            firstCard = null
            secondCard = null
            isBusy = false
        }
    }

    val colorListener = View.OnClickListener {
        GlobalScope.launch(Dispatchers.Main) {
            openCards(it as ImageView)
        }
    }
}
