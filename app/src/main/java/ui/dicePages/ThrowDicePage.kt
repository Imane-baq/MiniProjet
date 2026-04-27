package ui.dicePages

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import domain.model.Dice
import kotlin.math.abs
import kotlin.random.Random

private val DarkWood = Color(0xFF2B1D14)
private val BoardBrown = Color(0xFF3B2416)
private val Parchment = Color(0xFFF4D27A)
private val LightParchment = Color(0xFFFFF1B8)
private val Gold = Color(0xFFE8B95E)
private val Red = Color(0xFF8B0000)

@Composable
fun ThrowDicePage(modifier: Modifier = Modifier) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val diceList = remember { mutableStateListOf<Dice>() }

    var selectedDice by remember { mutableStateOf<Dice?>(null) }
    var result by remember { mutableStateOf<String?>(null) }
    var rollAnimationTrigger by remember { mutableStateOf(0) }
    var message by remember {
        mutableStateOf("Choisis un dé, puis bouge le téléphone pour le lancer.")
    }

    LaunchedEffect(userId) {
        if (userId == null) return@LaunchedEffect

        db.collection("dices")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                diceList.clear()
                snapshot.documents.forEach { document ->
                    document.toObject(Dice::class.java)?.let { dice ->
                        diceList.add(dice)
                    }
                }
            }
            .addOnFailureListener {
                message = "Erreur lors du chargement des dés."
            }
    }

    GyroscopeDiceLauncher(
        isEnabled = selectedDice != null,
        onRollDetected = {
            val dice = selectedDice ?: return@GyroscopeDiceLauncher
            result = rollWeightedDice(dice)
            rollAnimationTrigger++
            message = "Dé lancé avec le gyroscope."
        },
        onSensorUnavailable = {
            message = "Gyroscope non disponible sur cet appareil."
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkWood)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Header(message = message)

        DiceStage(
            selectedDice = selectedDice,
            result = result,
            animationTrigger = rollAnimationTrigger
        )

        if (diceList.isEmpty()) {
            EmptyDiceState()
        } else {
            DicePicker(
                diceList = diceList,
                selectedDice = selectedDice,
                onSelect = { dice ->
                    selectedDice = dice
                    result = null
                    message = "Bouge le téléphone pour lancer ${dice.diceName}."
                }
            )
        }
    }
}

@Composable
private fun Header(message: String) {
    Card(
        shape = CutCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BoardBrown),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Lancer un dé",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Parchment
            )

            Text(
                text = message,
                color = LightParchment
            )
        }
    }
}

@Composable
private fun DiceStage(
    selectedDice: Dice?,
    result: String?,
    animationTrigger: Int
) {
    Card(
        shape = CutCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Parchment),
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (selectedDice == null) {
                Text(
                    text = "Aucun dé sélectionné",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif,
                    color = BoardBrown
                )

                Text(
                    text = "Choisis un dé dans la liste en bas.",
                    color = Color(0xFF5C3B24)
                )
            } else {
                Text(
                    text = selectedDice.diceName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = BoardBrown
                )

                Text(
                    text = "${selectedDice.diceFaces.size} faces",
                    color = Color(0xFF5C3B24)
                )

                AnimatedDice(
                    result = result,
                    animationTrigger = animationTrigger
                )

                Text(
                    text = "Résultat : ${result ?: "-"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Red
                )
            }
        }
    }
}

@Composable
private fun AnimatedDice(
    result: String?,
    animationTrigger: Int
) {
    val rotationX by animateFloatAsState(
        targetValue = animationTrigger * 720f,
        animationSpec = tween(durationMillis = 900),
        label = "rotationX"
    )

    val rotationY by animateFloatAsState(
        targetValue = animationTrigger * 540f,
        animationSpec = tween(durationMillis = 900),
        label = "rotationY"
    )

    val rotationZ by animateFloatAsState(
        targetValue = animationTrigger * 1080f,
        animationSpec = tween(durationMillis = 900),
        label = "rotationZ"
    )

    val jump by animateFloatAsState(
        targetValue = if (animationTrigger % 2 == 0) 0f else -70f,
        animationSpec = tween(durationMillis = 450),
        label = "jump"
    )

    Box(
        modifier = Modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .graphicsLayer {
                    this.rotationX = rotationX
                    this.rotationY = rotationY
                    this.rotationZ = rotationZ
                    translationY = jump
                    cameraDistance = 14f * density
                    shadowElevation = 26f
                    shape = CutCornerShape(24.dp)
                    clip = false
                }
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFF7CF),
                            Color(0xFFFFD978),
                            Gold
                        )
                    ),
                    shape = CutCornerShape(24.dp)
                )
                .border(
                    width = 5.dp,
                    color = BoardBrown,
                    shape = CutCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = result ?: "🎲",
                fontSize = 52.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Red
            )
        }
    }
}

@Composable
private fun DicePicker(
    diceList: List<Dice>,
    selectedDice: Dice?,
    onSelect: (Dice) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Tes dés",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Parchment
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(diceList) { dice ->
                DiceChoiceCard(
                    dice = dice,
                    isSelected = selectedDice?.diceName == dice.diceName,
                    onSelect = { onSelect(dice) }
                )
            }
        }
    }
}

@Composable
private fun DiceChoiceCard(
    dice: Dice,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        shape = CutCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Gold else Parchment
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 10.dp else 4.dp),
        modifier = Modifier.size(width = 190.dp, height = 130.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = dice.diceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = BoardBrown
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${dice.diceFaces.size} faces",
                    color = Color(0xFF5C3B24)
                )
            }

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Red else BoardBrown
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSelected) "Sélectionné" else "Choisir")
            }
        }
    }
}

@Composable
private fun EmptyDiceState() {
    Card(
        shape = CutCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Parchment),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Aucun dé disponible",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = BoardBrown
            )

            Text(
                text = "Crée un dé avant de pouvoir le lancer.",
                color = Color(0xFF5C3B24)
            )
        }
    }
}

@Composable
private fun GyroscopeDiceLauncher(
    isEnabled: Boolean,
    onRollDetected: () -> Unit,
    onSensorUnavailable: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(isEnabled) {
        if (!isEnabled) {
            return@DisposableEffect onDispose {}
        }

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        if (gyroscope == null) {
            onSensorUnavailable()
            return@DisposableEffect onDispose {}
        }

        var lastRollTime = 0L
        val cooldownMillis = 1200L
        val rotationThreshold = 6.5f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rotationSpeed =
                    abs(event.values[0]) + abs(event.values[1]) + abs(event.values[2])

                val now = System.currentTimeMillis()

                if (rotationSpeed > rotationThreshold && now - lastRollTime > cooldownMillis) {
                    lastRollTime = now
                    onRollDetected()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            gyroscope,
            SensorManager.SENSOR_DELAY_GAME
        )

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
}

private fun rollWeightedDice(dice: Dice): String {
    val faces = dice.diceFaces

    if (faces.isEmpty()) return "-"

    val totalWeight = faces.sumOf { face ->
        face.faceWeight.coerceAtLeast(0)
    }

    if (totalWeight <= 0) {
        return faces.random().faceValue
    }

    var randomValue = Random.nextInt(totalWeight)

    for (face in faces) {
        randomValue -= face.faceWeight.coerceAtLeast(0)

        if (randomValue < 0) {
            return face.faceValue
        }
    }

    return faces.last().faceValue
}