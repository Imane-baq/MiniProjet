package ui.dicePages

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import domain.model.Dice
import domain.model.DiceFace
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun AddDicePage(modifier: Modifier = Modifier) {
    var diceName by remember { mutableStateOf("") }
    var numberOfSides by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val diceFaces = remember { mutableStateListOf<DiceFace>() }

    val db = Firebase.firestore
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF2B1D14))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            shape = CutCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF4D27A)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ajout d'un dé",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF3B2416)
                )

                OutlinedTextField(
                    value = diceName,
                    onValueChange = { diceName = it },
                    label = { Text("Nom du nouveau dé") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = numberOfSides,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            numberOfSides = newValue
                        }
                    },
                    label = { Text("Nombre de faces (4-20)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val faceCount = numberOfSides.toIntOrNull()

                        if (faceCount == null || faceCount !in 4..20) {
                            errorMessage = "Le nombre de faces doit être entre 4 et 20."
                            return@Button
                        }

                        diceFaces.clear()

                        repeat(faceCount) { index ->
                            diceFaces.add(
                                DiceFace(
                                    faceValue = (index + 1).toString(),
                                    faceWeight = 1
                                )
                            )
                        }

                        errorMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B2416)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configurer les faces")
                }

                if (diceFaces.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(diceFaces) { index, face ->
                            FaceEditor(
                                index = index,
                                face = face,
                                onValueChange = { newValue ->
                                    diceFaces[index] = face.copy(faceValue = newValue)
                                },
                                onWeightChange = { newWeight ->
                                    diceFaces[index] = face.copy(faceWeight = newWeight)
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (diceName.isBlank()) {
                            errorMessage = "Le nom du dé est obligatoire."
                            return@Button
                        }

                        if (userId == null) {
                            errorMessage = "Utilisateur non connecté."
                            return@Button
                        }

                        if (diceFaces.isEmpty()) {
                            val faceCount = numberOfSides.toIntOrNull()

                            if (faceCount == null || faceCount !in 4..20) {
                                errorMessage = "Le nombre de faces doit être entre 4 et 20."
                                return@Button
                            }

                            repeat(faceCount) { index ->
                                diceFaces.add(
                                    DiceFace(
                                        faceValue = (index + 1).toString(),
                                        faceWeight = 1
                                    )
                                )
                            }
                        }

                        val dice = Dice(
                            userId = userId,
                            diceName = diceName.trim(),
                            diceWeight = diceFaces.size,
                            diceFaces = diceFaces.toList()
                        )

                        db.collection("dices")
                            .add(dice)
                            .addOnSuccessListener { documentReference ->
                                Log.d("Firestore", "Dice added with ID: ${documentReference.id}")
                                Toast.makeText(
                                    context,
                                    "Dé ajouté avec succès",
                                    Toast.LENGTH_SHORT
                                ).show()

                                diceName = ""
                                numberOfSides = ""
                                diceFaces.clear()
                                errorMessage = null
                            }
                            .addOnFailureListener { exception ->
                                Log.w("Firestore", "Error adding Dice", exception)
                                errorMessage = exception.message
                            }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B3F00)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ajouter le dé")
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFF8B0000)
                    )
                }
            }
        }
    }
}

@Composable
private fun FaceEditor(
    index: Int,
    face: DiceFace,
    onValueChange: (String) -> Unit,
    onWeightChange: (Int) -> Unit
) {
    Card(
        shape = CutCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8B95E)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Face ${index + 1}",
                color = Color(0xFF3B2416),
                fontFamily = FontFamily.Serif
            )

            OutlinedTextField(
                value = face.faceValue,
                onValueChange = onValueChange,
                label = { Text("Valeur de la face") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = face.faceWeight.toString(),
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        onWeightChange(newValue.toIntOrNull() ?: 1)
                    }
                },
                label = { Text("Poids / chance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
