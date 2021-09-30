package otus.homework.customview.graphicview

import otus.homework.customview.Spending
import java.util.Calendar

/**
 *
 *
 * @author Юрий Польщиков on 29.09.2021
 */
class GraphicBoundsModel(
    val minTime: Calendar,
    val maxTime: Calendar,
    val minAmount: Double,
    val maxAmount: Double,
    val spendings: List<Spending>
)
