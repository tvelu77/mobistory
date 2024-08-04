package fr.uge.mobistory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.mobistory.R
import fr.uge.mobistory.database.entity.Event
import fr.uge.mobistory.localstorage.AppData
import fr.uge.mobistory.utils.QuizQuestion
import fr.uge.mobistory.utils.toCorrectFormat
import java.time.LocalDate
import java.util.Random

/**
 * Takes a random question and shows 4 choices.
 * The player plays until he has a wrong answer.
 *
 * @param appData AppData.
 */
@Composable
fun Quiz(appData: AppData) {
    val context = LocalContext.current
    val events = appData.dbManager.getDb().eventDao().getAll()
    val score = remember { mutableIntStateOf(0) }
    val isGameOver = remember { mutableStateOf(false) }
    val questions = remember { mutableStateOf(generateQuestions(events)) }
    val currentQuestionIndex = remember { mutableIntStateOf(0) }
    val currentQuestion = questions.value[currentQuestionIndex.intValue]
    if (isGameOver.value) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = context.getString(R.string.game_over),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = context.getString(R.string.final_score, score.intValue),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = context.getString(R.string.previous_question, currentQuestion.event.title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = context.getString(R.string.previous_question_answer,
                    toCorrectFormat(currentQuestion.event.beginDate.toString())),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            )
            Button(
                onClick = {
                    questions.value = generateQuestions(events)
                    score.intValue = 0
                    isGameOver.value = false
                    currentQuestionIndex.intValue = 0
                          },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = context.getString(R.string.restart))
            }
        }
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = currentQuestion.event.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            currentQuestion.choices.forEach { choice ->
                Button(
                    onClick = {
                        if (choice == currentQuestion.event.beginDate) {
                            score.intValue += 10
                            currentQuestionIndex.intValue = (currentQuestionIndex.intValue + 1) % questions.value.size
                        } else {
                            isGameOver.value = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = toCorrectFormat(choice.toString()))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Generates questions according to the list of events.
 * To choose the wrong answer, we just take 3 others dates of other events.
 *
 * @param events List of Event.
 * @return List of QuizQuestion, contains "List of Events"-size of question.
 */
private fun generateQuestions(events: List<Event>): List<QuizQuestion> {
    val random = Random(System.currentTimeMillis())
    return events.map { event ->
        var choices = mutableListOf<LocalDate>()
        val incorrectEvents = events.filterNot { it == event }.shuffled(random).take(3)
        val datesChoice = incorrectEvents.map {
            it.beginDate
        }
        choices.addAll(datesChoice)
        choices.add(event.beginDate)
        choices = choices.shuffled(random) as MutableList<LocalDate>
        QuizQuestion(event, choices)
    }.shuffled(random)
}