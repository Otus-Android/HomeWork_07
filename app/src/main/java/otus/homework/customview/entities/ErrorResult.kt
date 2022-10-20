package otus.homework.customview.entities

sealed class ErrorResult {

    object UnknownArc : ErrorResult()

    object UnknownCategory : ErrorResult()

}
