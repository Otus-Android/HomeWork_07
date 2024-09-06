package otus.homework.customview.presentation.model

enum class PieChartColor(val intColor: Int) {
    BLUE(0xFF0000FF.toInt()),
    YELLOW(0xFFFFFF00.toInt()),
    GREEN(0xFF00FF00.toInt()),
    ORANGE(0xFFFFA500.toInt()),
    RED(0xFFFF0000.toInt()),
    PURPLE(0xFF800080.toInt()),
    OLIVE(0xFF808000.toInt()),
    DARK_GREEN(0xFF006400.toInt()),
    MAROON(0xFF800000.toInt()),
    AQUA(0xFF00FFFF.toInt()),
    TEAL(0xFF008080.toInt()),
    GRAY(0xFF808080.toInt()),
    NAVY(0xFF000080.toInt()),
    FUCHSIA(0xFFFF00FF.toInt());

    fun nextColor(): PieChartColor {
        val values = values()
        val nextOrdinal = (this.ordinal + 1) % values.size
        return values[nextOrdinal]
    }
}