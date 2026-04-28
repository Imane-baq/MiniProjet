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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Header(message = message)

        DiceStage(
            selectedDice = selectedDice,
            result = result,
            animationTrigger = rollAnimationTrigger,
            modifier = Modifier.weight(1f)
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
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Lancer un dé",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Parchment
            )

            Text(
                text = message,
                color = LightParchment,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DiceStage(
    selectedDice: Dice?,
    result: String?,
    animationTrigger: Int,
    modifier: Modifier = Modifier
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp

    val diceSize = when {
        screenHeight < 700 -> 155.dp
        screenHeight < 800 -> 175.dp
        else -> 190.dp
    }

    val fontSize = when {
        screenHeight < 700 -> 38.sp
        screenHeight < 800 -> 44.sp
        else -> 48.sp
    }

    Card(
        shape = CutCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Parchment),
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .heightIn(min = 230.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = BoardBrown,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${selectedDice.diceFaces.size} faces",
                    color = Color(0xFF5C3B24)
                )

                AnimatedDice(
                    result = result,
                    animationTrigger = animationTrigger,
                    diceSize = diceSize,
                    fontSize = fontSize
                )

                ResultCard(result = result)
            }
        }
    }
}

@Composable
private fun ResultCard(result: String?) {
    Card(
        shape = CutCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightParchment),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = "Résultat : ${result ?: "-"}",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Red,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun AnimatedDice(
    result: String?,
    animationTrigger: Int,
    diceSize: androidx.compose.ui.unit.Dp,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    val tilt by animateFloatAsState(
        targetValue = if (animationTrigger % 2 == 0) -8f else 8f,
        animationSpec = tween(durationMillis = 350),
        label = "tilt"
    )

    val jump by animateFloatAsState(
        targetValue = if (animationTrigger % 2 == 0) 0f else -35f,
        animationSpec = tween(durationMillis = 350),
        label = "jump"
    )

    val scale by animateFloatAsState(
        targetValue = if (animationTrigger % 2 == 0) 1f else 1.06f,
        animationSpec = tween(durationMillis = 350),
        label = "scale"
    )

    Box(
        modifier = Modifier.size(diceSize + 35.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(diceSize)
                .graphicsLayer {
                    rotationZ = tilt
                    translationY = jump
                    scaleX = scale
                    scaleY = scale
                    shadowElevation = 24f
                    shape = CutCornerShape(24.dp)
                    clip = false
                }
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFAE0),
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
                fontSize = fontSize,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Red,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
    val screenWidth = LocalConfiguration.current.screenWidthDp

    val cardWidth = when {
        screenWidth < 360 -> 185.dp
        screenWidth < 420 -> 205.dp
        else -> 225.dp
    }

    val cardHeight = when {
        screenWidth < 360 -> 175.dp
        else -> 165.dp
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
            contentPadding = PaddingValues(bottom = 0.dp)
        ) {
            items(diceList) { dice ->
                DiceChoiceCard(
                    dice = dice,
                    isSelected = selectedDice?.diceName == dice.diceName,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
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
    cardWidth: androidx.compose.ui.unit.Dp,
    cardHeight: androidx.compose.ui.unit.Dp,
    onSelect: () -> Unit
) {
    Card(
        shape = CutCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Gold else Parchment
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 10.dp else 4.dp),
        modifier = Modifier.size(width = cardWidth, height = cardHeight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = dice.diceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = BoardBrown,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${dice.diceFaces.size} faces",
                    color = Color(0xFF5C3B24),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Red else BoardBrown
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isSelected) "Sélectionné" else "Choisir",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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