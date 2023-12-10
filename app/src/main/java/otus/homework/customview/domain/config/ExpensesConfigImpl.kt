package otus.homework.customview.domain.config

/**
 * Реализация конфигурационных данных по расходам
 */
class ExpensesConfigImpl : ExpensesConfig {

    override var providerType = DEFAULT_PROVIDER

    companion object {
        val DEFAULT_PROVIDER = ExpensesProviderType.FILE
    }
}