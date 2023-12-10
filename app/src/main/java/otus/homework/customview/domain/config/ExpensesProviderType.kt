package otus.homework.customview.domain.config

/**
 * Способ получения данных по расходам
 */
enum class ExpensesProviderType {

    /** Получение данных из файла */
    FILE,

    /** Получение сгенерированных данных */
    RANDOM
}