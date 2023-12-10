package otus.homework.customview.domain.config

class ExpensesConfigImpl : ExpensesConfig {

    override var provider = DEFAULT_PROVIDER

    companion object {
        val DEFAULT_PROVIDER = ExpensesProvider.ORIGIN
    }
}