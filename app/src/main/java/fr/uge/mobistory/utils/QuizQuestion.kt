package fr.uge.mobistory.utils

import fr.uge.mobistory.database.entity.Event
import java.time.LocalDate

/**
 * Represents a question.
 * To get the correct answer, just use event.beginDate.
 *
 * @param event Event.
 * @param choices List of String, 4 dates.
 */
data class QuizQuestion(val event: Event, val choices: MutableList<LocalDate>) {
}