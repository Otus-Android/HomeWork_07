package otus.homework.customview

sealed class Either<out F, out S> {

    data class Failure<out F, out S>(val error: F) : Either<F, S>()

    data class Success<out F, out S>(val result: S) : Either<F, S>()

}

fun <E> E.failure() = Either.Failure<E, Nothing>(this)

fun <T> T.success() = Either.Success<Nothing, T>(this)