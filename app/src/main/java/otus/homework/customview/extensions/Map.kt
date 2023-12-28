package otus.homework.customview.extensions

import otus.homework.customview.pojo.Sector

fun Map<String, Sector>.getNameByAngle(angle: Float): String {
    return filterValues { angle in it.startAngle .. it.endAngle }.firstNotNullOf { it.key }
}