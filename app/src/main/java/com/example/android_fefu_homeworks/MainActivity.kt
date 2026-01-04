package com.example.android_fefu_homeworks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.ui.graphics.*
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalWindowInfo


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    QuizApp()
                }
            }
        }
    }
}

data class Question(
    val id: Int,
    val question: String,
    val answers: List<String>,
    val correctid: Int
)

val Questions = listOf(
    Question(
        id = 0,
        question = "Что выполняет программы?",
        answers = listOf(
            "Жёсткий диск",
            "Оперативная память",
            "Процессор",
            "Видеокарта"
        ),
        correctid = 2
    ),
    Question(
        id = 1,
        question = "Расшифровка SSD?",
        answers = listOf(
            "Super Speed",
            "Solid Storage",
            "Solid State Drive",
            "System Disk"
        ),
        correctid = 2
    ),
    Question(
        id = 2,
        question = "Какая память энергозависимая?",
        answers = listOf(
            "HDD",
            "SSD",
            "Оперативная память",
            "Постоянная память"
        ),
        correctid = 2
    ),
    Question(
        id = 3,
        question = "Для чего нужен BIOS/UEFI?",
        answers = listOf(
            "Запуск игр",
            "Хранение файлов",
            "Загрузка и настройка ПК",
            "Охлаждение системы"
        ),
        correctid = 2
    ),
    Question(
        id = 4,
        question = "Что выводит изображение на экран?",
        answers = listOf(
            "Процессор",
            "Материнская плата",
            "Видеокарта",
            "Блок питания"
        ),
        correctid = 2
    ),
    Question(
        id = 5,
        question = "Что такое бит?",
        answers = listOf(
            "Процессор",
            "Единица информации (0 или 1)",
            "Скорость сети",
            "Разрядность системы"
        ),
        correctid = 1
    ),
    Question(
        id = 6,
        question = "Какой интерфейс у NVMe SSD?",
        answers = listOf(
            "SATA",
            "USB",
            "PCI Express",
            "HDMI"
        ),
        correctid = 2
    ),
    Question(
        id = 7,
        question = "Для чего нужен кэш процессора?",
        answers = listOf(
            "Хранение фильмов",
            "Быстрый доступ к данным",
            "Охлаждение",
            "Хранение драйверов"
        ),
        correctid = 1
    ),
    Question(
        id = 8,
        question = "Что используется при нехватке оперативной памяти?",
        answers = listOf(
            "Снижение температуры",
            "Увеличение частоты",
            "Файл подкачки",
            "Ничего не происходит"
        ),
        correctid = 2
    ),
    Question(
        id = 9,
        question = "Какая ОС основана на ядре Linux?",
        answers = listOf(
            "Windows",
            "macOS",
            "Ubuntu",
            "iOS"
        ),
        correctid = 2
    ),
    Question(
        id = 10,
        question = "Что выполняет арифметико-логическое устройство (ALU)?",
        answers = listOf(
            "Кэширование данных",
            "Управление памятью",
            "Логические и арифметические операции",
            "Загрузку BIOS"
        ),
        correctid = 2
    ),
    Question(
        id = 11,
        question = "Что такое троттлинг процессора?",
        answers = listOf(
            "Увеличение частоты",
            "Полное выключение",
            "Снижение частот из-за перегрева",
            "Повышение напряжения"
        ),
        correctid = 2
    ),
    Question(
        id = 12,
        question = "Какая шина имеет наибольшую пропускную способность?",
        answers = listOf(
            "PCI",
            "PCI Express",
            "USB 2.0",
            "SATA III"
        ),
        correctid = 1
    ),
    Question(
        id = 13,
        question = "Зачем нужна виртуальная память?",
        answers = listOf(
            "Ускорение видеокарты",
            "Расширение оперативной памяти",
            "Хранение драйверов",
            "Защита BIOS"
        ),
        correctid = 1
    ),
    Question(
        id = 14,
        question = "Что означает CL в характеристиках оперативной памяти?",
        answers = listOf(
            "Тактовая частота",
            "Контроллер памяти",
            "Задержка памяти (латентность)",
            "Количество каналов"
        ),
        correctid = 2
    ),
    Question(
        id = 15,
        question = "Какой кэш процессора самый быстрый?",
        answers = listOf(
            "L1",
            "L2",
            "L3",
            "Оперативная память"
        ),
        correctid = 0
    ),
    Question(
        id = 16,
        question = "Что НЕ входит в задачи операционной системы?",
        answers = listOf(
            "Управление памятью",
            "Управление процессами",
            "Рендеринг 3D-графики",
            "Управление файлами"
        ),
        correctid = 2
    ),
    Question(
        id = 17,
        question = "Где хранится прошивка UEFI?",
        answers = listOf(
            "Оперативная память",
            "Жёсткий диск",
            "Флеш-память",
            "Кэш процессора"
        ),
        correctid = 2
    ),
    Question(
        id = 18,
        question = "Что такое многопоточность?",
        answers = listOf(
            "Несколько видеокарт",
            "Параллельное выполнение потоков",
            "Несколько дисков",
            "Максимальная частота CPU"
        ),
        correctid = 1
    ),
    Question(
        id = 19,
        question = "Какой режим SATA обеспечивает максимальную скорость?",
        answers = listOf(
            "IDE",
            "Legacy",
            "AHCI",
            "Compatibility"
        ),
        correctid = 2
    )
)

enum class Screen {
    Start,
    Question,
    Result
}

data class QuizUiState(
    val currentQuestionIndex: Int = 0,
    val selectedAnswerIndex: Int? = null,
    val correctAnswersCount: Int = 0,
    val screen: Screen = Screen.Start
)

class QuizStateHolder : ViewModel() {
    private var questions: List<Question> = Questions

    var uiState by mutableStateOf(QuizUiState())

    private fun shuffleQuestionAnswers(question: Question): Question {
        val shuffledAnswers = question.answers.shuffled()
        val newCorrectId = shuffledAnswers.indexOf(question.answers[question.correctid])
        return question.copy(answers = shuffledAnswers, correctid = newCorrectId)
    }

    fun startQuiz() {
        questions = Questions.shuffled().map { shuffleQuestionAnswers(it) }
        uiState = QuizUiState(screen = Screen.Question)
    }

    fun selectAnswer(index: Int) {
        uiState = uiState.copy(selectedAnswerIndex = index)
    }

    fun nextQuestion() {
        val current = questions[uiState.currentQuestionIndex]
        val isCorrect = uiState.selectedAnswerIndex == current.correctid
        val nextIndex = uiState.currentQuestionIndex + 1

        uiState = if (nextIndex < questions.size) {
            uiState.copy(
                currentQuestionIndex = nextIndex,
                selectedAnswerIndex = null,
                correctAnswersCount = uiState.correctAnswersCount + if (isCorrect) 1 else 0
            )
        } else {
            uiState.copy(
                correctAnswersCount = uiState.correctAnswersCount + if (isCorrect) 1 else 0,
                screen = Screen.Result
            )
        }
    }

    fun restartQuiz() {
        questions = Questions.shuffled().map { shuffleQuestionAnswers(it) }
        uiState = QuizUiState(
            screen = Screen.Question,
            currentQuestionIndex = 0,
            selectedAnswerIndex = null,
            correctAnswersCount = 0
        )
    }

    fun currentQuestion(): Question? = questions.getOrNull(uiState.currentQuestionIndex)
}


@Composable
fun QuizApp() {
    val holder: QuizStateHolder = viewModel()
    val state = holder.uiState


    when (state.screen) {
        Screen.Start -> StartScreen(onStart = { holder.startQuiz() })
        Screen.Question -> {
            val question = holder.currentQuestion()
            if (question != null) {
                QuestionScreen(
                    question = question,
                    uiState = state,
                    onAnswerSelected = { holder.selectAnswer(it) },
                    onNext = { holder.nextQuestion() }
                )
            }
        }
        Screen.Result -> ResultScreen(
            uiState = state,
            total = Questions.size,
            onRestart = { holder.restartQuiz() }
        )
    }
}

@Composable
fun StartScreen(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Computer Quiz",
                fontSize = 36.sp,
                color = Color(0xFFdddddd),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Проверь знания о компьютерах и технологиях",
                fontSize = 20.sp,
                color = Color(0xFFdddddd),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onStart,
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF222222),
                    contentColor = Color(0xffdddddd)
                ),
                modifier = Modifier
                    .height(56.dp)
                    .width(200.dp)
            ) {
                Text("Начать", fontSize = 22.sp)
            }
        }
    }
}

@Composable
fun QuestionScreen(
    question: Question,
    uiState: QuizUiState,
    onAnswerSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    val totalQuestions = Questions.size
    val progress = (uiState.currentQuestionIndex + 1).toFloat() / totalQuestions
    val windowInfo = LocalWindowInfo.current
    val size = windowInfo.containerSize

    val isPortrait = size.height > size.width

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Вопрос ${uiState.currentQuestionIndex + 1} из ${Questions.size}",
                    fontSize = 27.sp,
                    color = Color(0xFFdddddd)
                )
                Text(
                    text = "Верных ${uiState.correctAnswersCount}",
                    fontSize = 27.sp,
                    color = Color(0xFFdddddd),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = {progress},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                color = Color(0xFFDDDDDD),
                trackColor = Color(0xFF444444)
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF222222), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = question.question,
                    fontSize = 28.sp,
                    color = Color(0xFFdddddd),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 30.sp
                )
            }
        }

        LazyVerticalGrid(
            columns = if (isPortrait) GridCells.Fixed(2) else GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(0.21f)
                .padding(horizontal = 16.dp)
        ) {
            items(question.answers.size) { index ->
                val isSelected = uiState.selectedAnswerIndex == index
                val backgroundColor = when {
                    isSelected -> Color(0xFFdddddd)
                    else -> Color(0xFF222222)
                }
                val contentColor = if (isSelected) Color(0xFF222222) else Color(0xFFdddddd)

                Button(
                    onClick = { onAnswerSelected(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (isPortrait) 1.33f else 1.8f),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = backgroundColor,
                        contentColor = contentColor
                    )
                ) {
                    Text(
                        text = question.answers[index],
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                .clickable(
                    enabled = uiState.selectedAnswerIndex != null,
                    onClick = onNext
                )
                .background(
                    color = if (uiState.selectedAnswerIndex != null) Color(0xFFdddddd) else Color(0x55222222),
                    shape = RoundedCornerShape(15.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Дальше",
                fontSize = 22.sp,
                color = if (uiState.selectedAnswerIndex != null) Color(0xFF222222) else Color(0x55dddddd)
            )
        }
    }
}

@Composable
fun ResultScreen(uiState: QuizUiState, total: Int, onRestart: () -> Unit) {
    val percent = (uiState.correctAnswersCount * 100) / total
    val comment = when {
        percent < 50 -> "Начальный уровень. Есть над чем поработать!"
        percent < 80 -> "Хороший результат! Ты уверенный пользователь."
        else -> "Отлично! Настоящий компьютерный эксперт."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Ваш результат: ${uiState.correctAnswersCount} из $total",
                fontSize = 28.sp,
                color = Color(0xFFdddddd),
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "($percent%)",
                fontSize = 34.sp,
                color = Color(0xFFdddddd),
                textAlign = TextAlign.Center,
                lineHeight = 35.sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                comment,
                fontSize = 25.sp,
                color = Color(0xFFdddddd),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onRestart,
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF222222),
                    contentColor = Color(0xFFdddddd)
                ),
                modifier = Modifier
                    .height(56.dp)
                    .width(220.dp)
            ) {
                Text("Пройти ещё раз", fontSize = 22.sp)
            }
        }
    }
}