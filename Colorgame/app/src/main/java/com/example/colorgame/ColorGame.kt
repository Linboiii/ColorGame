package com.example.colorgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class StroopGameState { INITIAL, COUNTDOWN, RUNNING, GAME_OVER }
enum class StroopMode { COLOR, TEXT }
enum class StroopColor(val label: String, val color: Color) {
    RED("RED", Color.Red),
    BLUE("BLUE", Color.Blue),
    GREEN("GREEN", Color(0xFF2E7D32)),
    ORANGE("ORANGE", Color(0xFFFF9800))
}

data class QuestionData(
    val id: Long,
    val mode: StroopMode,
    val textWord: StroopColor,
    val inkColor: StroopColor,
    val leftOption: StroopColor,
    val rightOption: StroopColor
)

class ColorWordMatchingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ColorWordMatchingScreen()
            }
        }
    }
}

@Composable
fun ColorWordMatchingScreen() {
    var gameState by remember { mutableStateOf(StroopGameState.INITIAL) }
    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }
    var strikes by remember { mutableIntStateOf(0) }

    var currentQuestion by remember { mutableStateOf<QuestionData?>(null) }
    var countdownValue by remember { mutableStateOf("3") }
    var timeRemainingSeconds by remember { mutableIntStateOf(5) }

    fun generateNewQuestion() {
        val mode = StroopMode.entries.random()
        val textWord = StroopColor.entries.random()
        var tempInk = StroopColor.entries.random()
        while (tempInk == textWord) {
            tempInk = StroopColor.entries.random()
        }
        val inkColor = tempInk

        val targetColor = if (mode == StroopMode.COLOR) inkColor else textWord
        val wrongOptions = StroopColor.entries.filter { it != targetColor }
        val wrongChoice = wrongOptions.random()

        val isLeftTarget = listOf(true, false).random()
        val leftOption = if (isLeftTarget) targetColor else wrongChoice
        val rightOption = if (isLeftTarget) wrongChoice else targetColor

        currentQuestion = QuestionData(
            id = System.currentTimeMillis() + (0..1000).random(),
            mode = mode,
            textWord = textWord,
            inkColor = inkColor,
            leftOption = leftOption,
            rightOption = rightOption
        )
        timeRemainingSeconds = 5
    }

    LaunchedEffect(gameState) {
        if (gameState == StroopGameState.COUNTDOWN) {
            countdownValue = "3"
            delay(1000)
            countdownValue = "2"
            delay(1000)
            countdownValue = "1"
            delay(1000)
            countdownValue = "Start!"
            delay(700)
            score = 0
            strikes = 0
            generateNewQuestion()
            gameState = StroopGameState.RUNNING
        }
    }

    val questionId = currentQuestion?.id
    LaunchedEffect(gameState, questionId) {
        if (gameState == StroopGameState.RUNNING && questionId != null) {
            while (timeRemainingSeconds > 0) {
                delay(1000)
                timeRemainingSeconds--
            }
            strikes++
            if (strikes >= 3) {
                if (score > bestScore) bestScore = score
                gameState = StroopGameState.GAME_OVER
            } else {
                generateNewQuestion()
            }
        }
    }

    fun handleAnswer(selectedColor: StroopColor) {
        val q = currentQuestion ?: return
        val targetColor = if (q.mode == StroopMode.COLOR) q.inkColor else q.textWord
        if (selectedColor == targetColor) {
            score++
            generateNewQuestion()
        } else {
            strikes++
            if (strikes >= 3) {
                if (score > bestScore) bestScore = score
                gameState = StroopGameState.GAME_OVER
            } else {
                generateNewQuestion()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9)),
        contentAlignment = Alignment.Center
    ) {
        when (gameState) {
            StroopGameState.INITIAL -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Welcome\nto\nColor Word Matching", fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = { gameState = StroopGameState.COUNTDOWN }) {
                        Text("Start Game")
                    }
                }
            }

            StroopGameState.COUNTDOWN -> {
                Text(countdownValue, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            }

            StroopGameState.RUNNING -> {
                val q = currentQuestion
                if (q != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mode: ${q.mode.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("✅ $score  ❌ $strikes/3", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${timeRemainingSeconds} s", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(24.dp))
                            Text(
                                q.textWord.label,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = q.inkColor.color
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { handleAnswer(q.leftOption) },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.width(130.dp)
                            ) {
                                Text(q.leftOption.label)
                            }
                            Button(
                                onClick = { handleAnswer(q.rightOption) },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.width(130.dp)
                            ) {
                                Text(q.rightOption.label)
                            }
                        }
                    }
                }
            }

            StroopGameState.GAME_OVER -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Game Over!", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Text("You're Score", fontSize = 16.sp, color = Color.Gray)
                    Text("$score", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Best Score", fontSize = 14.sp, color = Color.Gray)
                    Text("$bestScore", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = { gameState = StroopGameState.COUNTDOWN }) {
                        Text("Restart Game")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { gameState = StroopGameState.INITIAL },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("Exit", color = Color.Black)
                    }
                }
            }
        }
    }
}