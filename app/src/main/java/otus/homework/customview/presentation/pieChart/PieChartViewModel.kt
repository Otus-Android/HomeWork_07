package otus.homework.customview.presentation.pieChart

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import otus.homework.customview.CustomViewApp
import otus.homework.customview.domain.GenerateNewSpendsUseCase
import otus.homework.customview.domain.GetSpendsUseCase
import otus.homework.customview.domain.Spend
import otus.homework.customview.presentation.pieChart.model.PieData
import otus.homework.customview.presentation.pieChart.model.PieSlice
import kotlin.random.Random

class PieChartViewModel(
    getSpendsUseCase: GetSpendsUseCase,
    private val generateNewSpendsUseCase: GenerateNewSpendsUseCase,
): ViewModel() {

    private var pieSlices = mutableMapOf<String, PieSlice>()
    private var totalValue = 0

    private val _spends = getSpendsUseCase.getSpends()
    val spends: StateFlow<PieData> = _spends.map { addSpends(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = PieData(
                totalValue = totalValue,
                pieSlices = pieSlices,
            )
        )

    fun generateNewSpends() = generateNewSpendsUseCase.generateNewSpends()

    private fun addSpends(spends: List<Spend>): PieData {
        resetData()

        for (spend in spends) {
            totalValue += spend.amount
            val category = spend.category
            val pieSlice = pieSlices[category.title]
            if (pieSlice != null) {
                pieSlices[category.title] = pieSlice.copy(amount = pieSlice.amount + spend.amount)
            } else {
                pieSlices[category.title] = PieSlice(
                    categoryTitle = category.title,
                    amount = spend.amount,
                    startAngle = 0f,
                    sweepAngle = 0f,
                    thickness = 0f,
                    indicatorCircleLocation = PointF(),
                    paint = generatePaint()
                )
            }
        }

        setPieSliceDimensions()

        return PieData(totalValue = totalValue, pieSlices = pieSlices)
    }

    private fun generatePaint(): Paint = Paint().apply {
        color = Color.argb(
            /* alpha = */ 255,
            /* red = */ Random.nextInt(255),
            /* green = */ Random.nextInt(255),
            /* blue = */ Random.nextInt(255)
        )
        isAntiAlias = true
    }

    private fun resetData() {
        pieSlices.clear()
        totalValue = 0
    }

    private fun setPieSliceDimensions() {
        var lastAngle = 0f
        pieSlices.map { pieSliceEntry ->
            with(pieSliceEntry.value) {
                startAngle = lastAngle
                val part = amount / totalValue.toFloat()
                sweepAngle = (part * 360f)
                thickness = part
                lastAngle += sweepAngle
            }
        }
    }

    companion object {

        val Factory = object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY]) as CustomViewApp
                return PieChartViewModel(
                    getSpendsUseCase = GetSpendsUseCase(application.repository),
                    generateNewSpendsUseCase = GenerateNewSpendsUseCase(application.repository)
                ) as T
            }
        }
    }
}