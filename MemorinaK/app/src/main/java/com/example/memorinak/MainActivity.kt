package com.example.memorinak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(applicationContext)
        layout.orientation = LinearLayout.VERTICAL

        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.weight = 1.toFloat()

        val images = arrayOf(
            R.drawable.squarecat,
            R.drawable.squarecat,
            R.drawable.squarecat,
            R.drawable.squarecat,
            R.drawable.squarecat,
            R.drawable.squarecat,
            R.drawable.squarecat,
            R.drawable.squarecat
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

    suspend fun setBackgroundWithDelay(v: View) {
        val imgView = v as ImageView
        val frontImage = v.tag as Int

        imgView.setImageResource(frontImage)
        delay(1000)
        imgView.setImageResource(R.drawable.card_back)
    }

    val colorListener = View.OnClickListener {
        GlobalScope.launch(Dispatchers.Main) {
            setBackgroundWithDelay(it)
        }
    }
}
