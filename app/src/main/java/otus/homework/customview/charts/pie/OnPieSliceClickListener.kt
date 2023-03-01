package otus.homework.customview.charts.pie

import otus.homework.customview.charts.PayloadEntity

interface OnPieSliceClickListener {

    fun onClick(entry: PayloadEntity)

}