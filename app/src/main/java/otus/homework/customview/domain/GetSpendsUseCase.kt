package otus.homework.customview.domain

class GetSpendsUseCase(
    private val repository: Repository
) {
    fun getSpends() = repository.getSpendsUseCase()
}