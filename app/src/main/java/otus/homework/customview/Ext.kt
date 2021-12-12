package otus.homework.customview

fun List<PaymentPieChart>.sumAmount(): Int {
    var acc = 0
        this.forEach {
            acc += it.amountSum
            }
    return acc
}


