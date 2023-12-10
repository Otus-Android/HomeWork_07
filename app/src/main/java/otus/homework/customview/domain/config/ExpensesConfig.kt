package otus.homework.customview.domain.config

/**
 * Конфигурационные данные по расходам
 */
interface ExpensesConfig {

    /** Способ получения данных по расходам */
    var providerType: ExpensesProviderType
}