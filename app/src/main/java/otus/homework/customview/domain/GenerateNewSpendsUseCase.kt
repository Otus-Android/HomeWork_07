package otus.homework.customview.domain

class GenerateNewSpendsUseCase(
    private val repository: Repository
) {
    fun generateNewSpends() = repository.generateNewSpends()
}