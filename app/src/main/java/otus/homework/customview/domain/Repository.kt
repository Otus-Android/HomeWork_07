package otus.homework.customview.domain

import kotlinx.coroutines.flow.StateFlow

interface Repository {
    fun getSpendsUseCase(): StateFlow<List<Spend>>
    fun generateNewSpends()
}