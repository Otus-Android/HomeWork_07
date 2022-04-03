package otus.homework.customview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CategorySpendingBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.category_spending_bottomsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireArguments().getParcelable<CategoryOverallSpending>(CATEGORY_ARG)?.let {
            view.findViewById<TextView>(R.id.category_title).text = it.category.title
            view.findViewById<TextView>(R.id.spending_text).text =
                resources.getString(R.string.spending_text, it.amount)
            view.findViewById<ImageView>(R.id.category_icon).apply {
                setImageResource(it.category.iconRes)
                imageTintList = resources.getColorStateList(it.category.colorRes, context.theme)
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.ThemeOverlay_BottomSheetDialog
    }

    companion object {
        const val TAG = "CategorySpendingBottomSheet"
        private const val CATEGORY_ARG = "CATEGORY_ARG"

        fun newInstance(category: CategoryOverallSpending): CategorySpendingBottomSheet {
            val args = Bundle()
            args.putParcelable(CATEGORY_ARG, category)
            val fragment = CategorySpendingBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }
}