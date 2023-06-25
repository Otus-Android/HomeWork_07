package otus.homework.customview

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

fun Long.fromEpochSecondToLocalDate(): LocalDate = LocalDateTime
    .ofInstant(Instant.ofEpochSecond(this), ZoneId.systemDefault())
    .toLocalDate()