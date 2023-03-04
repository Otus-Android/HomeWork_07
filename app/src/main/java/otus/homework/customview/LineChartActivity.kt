package otus.homework.customview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import otus.homework.customview.databinding.ActivityLinechartBinding

class LineChartActivity : AppCompatActivity()  {

    companion object {
        private const val CATEGORY_EXTRA = "CATEGORY"

        fun getStartIntent(context: Context, category: String): Intent {
            return Intent(context, LineChartActivity::class.java).apply {
                putExtra(CATEGORY_EXTRA, category)
            }
        }
    }

    lateinit var binding: ActivityLinechartBinding

    private var feedback = Feedback.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinechartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFeedBackButtonsListeners()

        val category = requireNotNull(intent.getStringExtra(CATEGORY_EXTRA))
        val expenses = decodeFromJsonFileResId<Expenses>(R.raw.payload)

        with(binding) {
            this.category.text = category
            lineChart.setExpenses(expenses, category)
        }
    }

    private fun setFeedBackButtonsListeners() = with(binding) {
        val likeClickAnimator = FeedbackClickAnimator(
            view = like,
            isFeedbackChanged = { !feedback.isLike },
            onClick = {
                feedback = Feedback.LIKE
                like.setImageResource(R.drawable.icon_like_fill)
                dislike.setImageResource(R.drawable.icon_dislike)
            }
        )
        like.setOnTouchListener { _, event ->
            likeClickAnimator.animateOnTouch(event)
            true
        }

        val dislikeAnimator = FeedbackClickAnimator(
            view = dislike,
            isFeedbackChanged = { !feedback.isDislike },
            onClick = {
                feedback = Feedback.DISLIKE
                dislike.setImageResource(R.drawable.icon_dislike_fill)
                like.setImageResource(R.drawable.icon_like)
            }
        )
        dislike.setOnTouchListener { _, event ->
            dislikeAnimator.animateOnTouch(event)
            true
        }
    }
}