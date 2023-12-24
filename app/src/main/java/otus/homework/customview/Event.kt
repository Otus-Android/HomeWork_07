package otus.homework.customview

/**
 * События на экране отображения продуктов
 *
 * @author Евтушенко Максим 26.11.2023
 */
sealed class Event {

    /**
     * Событие клика на продукт на круговой диаграмме
     */
    data class ProductClick(val category: String) : Event()
}
