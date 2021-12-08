package otus.homework.customview.state

import otus.homework.customview.model.Sector
import java.io.Serializable

class PieChartState : Serializable {
    val sectors: MutableList<Sector> = mutableListOf()

    fun addSector(angle: Float, id: String, name: String, category: String, color: Int) {
        sectors.add(Sector(angle, id, name, category, color))
    }
}