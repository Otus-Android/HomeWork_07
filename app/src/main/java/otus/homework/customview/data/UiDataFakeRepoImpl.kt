package otus.homework.customview.data

import otus.homework.customview.presentation.PieChartUiInfo
import javax.inject.Inject

class UiDataFakeRepoImpl @Inject constructor(
    private val jsonParser: UiDataJsonParser,
    private val mapper: UiDataMapper
) : UiDataRepo {
    companion object {
        private const val FILE_NAME = "payload.json"
    }

    override fun getUiData(): PieChartUiInfo = mapper.map(
        data = jsonParser.parseFromAssets(
            FILE_NAME
        )
    )
}