package otus.homework.customview

enum class Feedback {
    LIKE,
    DISLIKE,
    NONE;

    val isLike: Boolean
        get() = this == LIKE

    val isDislike: Boolean
        get() = this == DISLIKE
}