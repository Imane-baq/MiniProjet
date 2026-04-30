package ui.dicePages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import domain.model.Dice
import domain.model.DiceFace

data class EditableDiceDocument(
    val documentId: String,
    val dice: Dice
)

@Composable
fun CustomizeDicePage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    val diceDocuments = remember { mutableStateListOf<EditableDiceDocument>() }

    var selectedDocumentId by remember { mutableStateOf<String?>(null) }
    var diceName by remember { mutableStateOf("") }
    var numberOfFaces by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    val editableFaces = remember { mutableStateListOf<DiceFace>() }

    fun loadDice() {
        if (userId == null) return

        db.collection("dices")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                diceDocuments.clear()

                snapshot.documents.forEach { document ->
                    val dice = document.toObject(Dice::class.java)

                    if (dice != null) {
                        diceDocuments.add(
                            EditableDiceDocument(
                                documentId = document.id,
                                dice = dice
                            )
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                message = "Erreur chargement : ${exception.message}"
            }
    }

    fun selectDice(diceDocument: EditableDiceDocument) {
        selectedDocumentId = diceDocument.documentId
        diceName = diceDocument.dice.diceName
        numberOfFaces = diceDocument.dice.diceFaces.size.toString()

        editableFaces.clear()
        editableFaces.addAll(diceDocument.dice.diceFaces)

        message = null
    }

    fun updateNumberOfFaces(newValue: String) {
        if (!newValue.all { it.isDigit() }) return

        numberOfFaces = newValue

        val newFaceCount = newValue.toIntOrNull() ?: return
        if (newFaceCount !in 4..20) return

        val currentFaceCount = editableFaces.size

        if (newFaceCount > currentFaceCount) {
            for (index in currentFaceCount until newFaceCount) {
                editableFaces.add(
                    DiceFace(
                        faceValue = (index + 1).toString(),
                        faceWeight = 1
                    )
                )
            }
        }

        if (newFaceCount < currentFaceCount) {
            while (editableFaces.size > newFaceCount) {
                editableFaces.removeAt(editableFaces.lastIndex)
            }
        }
    }

    fun saveDice() {
        val documentId = selectedDocumentId

        if (userId == null) {
            message = "Utilisateur non connecté."
            return
        }

        if (documentId == null) {
            message = "Aucun dé sélectionné."
            return
        }

        if (diceName.isBlank()) {
            message = "Le nom du dé est obligatoire."
            return
        }

        val faceCount = numberOfFaces.toIntOrNull()
        if (faceCount == null || faceCount !in 4..20) {
            message = "Le nombre de faces doit être entre 4 et 20."
            return
        }

        if (editableFaces.size != faceCount) {
            message = "Erreur : le nombre de faces ne correspond pas."
            return
        }

        // Validation : Valeurs de faces uniques
        val faceValues = editableFaces.map { it.faceValue.trim() }
        if (faceValues.any { it.isEmpty() }) {
            message = "Toutes les faces doivent avoir une valeur."
            return
        }
        if (faceValues.size != faceValues.distinct().size) {
            message = "Chaque face doit avoir une valeur unique."
            return
        }

        val updatedDice = Dice(
            userId = userId,
            diceName = diceName.trim(),
            diceWeight = editableFaces.size,
            diceFaces = editableFaces.toList()
        )

        db.collection("dices")
            .document(documentId)
            .set(updatedDice)
            .addOnSuccessListener {
                Toast.makeText(context, "Dé modifié avec succès.", Toast.LENGTH_SHORT).show()

                // RESET pour revenir à la liste
                selectedDocumentId = null
                diceName = ""
                numberOfFaces = ""
                editableFaces.clear()

                // recharge la liste
                loadDice()

                message = null
            }
            .addOnFailureListener { exception ->
                message = "Erreur sauvegarde : ${exception.message}"
            }
    }

    LaunchedEffect(userId) {
        loadDice()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF2B1D14))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Modifier un dé",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Serif,
            color = Color(0xFFF4D27A)
        )

        if (diceDocuments.isEmpty()) {
            EmptyCustomizeMessage()
        } else if (selectedDocumentId == null) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(diceDocuments) { diceDocument ->
                    DiceToEditCard(
                        diceDocument = diceDocument,
                        isSelected = selectedDocumentId == diceDocument.documentId,
                        onSelect = {
                            selectDice(diceDocument)
                        }
                    )
                }
            }
        }

        if (selectedDocumentId != null) {
            EditDiceForm(
                diceName = diceName,
                numberOfFaces = numberOfFaces,
                onDiceNameChange = { diceName = it; message = null },
                onNumberOfFacesChange = { updateNumberOfFaces(it); message = null },
                faces = editableFaces,
                onFaceValueChange = { index, value ->
                    editableFaces[index] = editableFaces[index].copy(faceValue = value)
                    message = null
                },
                onFaceWeightChange = { index, weight ->
                    editableFaces[index] = editableFaces[index].copy(faceWeight = weight)
                    message = null 
                },
                errorMessage = message,
                onSave = { saveDice() }
            )
        }
        
        // Message général (ex: erreur de chargement) si pas dans le formulaire
        if (selectedDocumentId == null && message != null) {
            Text(text = message!!, color = Color(0xFFF4D27A))
        }
    }
}

@Composable
private fun EmptyCustomizeMessage() {
    Card(
        shape = CutCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4D27A)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Aucun dé à modifier",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF3B2416)
            )

            Text(
                text = "Crée un dé avant d’utiliser cette option.",
                color = Color(0xFF5C3B24)
            )
        }
    }
}

@Composable
private fun DiceToEditCard(
    diceDocument: EditableDiceDocument,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val dice = diceDocument.dice

    Card(
        shape = CutCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE8B95E) else Color(0xFFF4D27A)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = dice.diceName,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF3B2416)
                )

                Text(
                    text = "Nombre de faces : ${dice.diceFaces.size}",
                    color = Color(0xFF5C3B24)
                )
            }

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(0xFF8B0000) else Color(0xFF7B3F00)
                )
            ) {
                Text(if (isSelected) "Sélectionné" else "Modifier")
            }
        }
    }
}

@Composable
private fun EditDiceForm(
    diceName: String,
    numberOfFaces: String,
    onDiceNameChange: (String) -> Unit,
    onNumberOfFacesChange: (String) -> Unit,
    faces: List<DiceFace>,
    onFaceValueChange: (Int, String) -> Unit,
    onFaceWeightChange: (Int, Int) -> Unit,
    errorMessage: String?,
    onSave: () -> Unit
) {
    Card(
        shape = CutCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4D27A)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Configuration du dé",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF3B2416)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFB00020), // Rouge vif pour les erreurs
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = diceName,
                onValueChange = onDiceNameChange,
                label = { Text("Nom du dé") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = numberOfFaces,
                onValueChange = onNumberOfFacesChange,
                label = { Text("Nombre de faces (4-20)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(faces.size) { index ->
                    FaceEditCard(
                        index = index,
                        face = faces[index],
                        onFaceValueChange = { value ->
                            onFaceValueChange(index, value)
                        },
                        onFaceWeightChange = { weight ->
                            onFaceWeightChange(index, weight)
                        }
                    )
                }
            }

            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B3F00)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sauvegarder les modifications")
            }
        }
    }
}

@Composable
private fun FaceEditCard(
    index: Int,
    face: DiceFace,
    onFaceValueChange: (String) -> Unit,
    onFaceWeightChange: (Int) -> Unit
) {
    var weightText by remember(face.faceWeight) { 
        mutableStateOf(if (face.faceWeight == 0) "" else face.faceWeight.toString()) 
    }

    Card(
        shape = CutCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8B95E)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = face.faceValue,
                onValueChange = onFaceValueChange,
                label = { Text("Valeur") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = weightText,
                onValueChange = { newValue ->
                    if (newValue.isEmpty()) {
                        weightText = ""
                        onFaceWeightChange(0)
                    } else if (newValue.all { it.isDigit() }) {
                        weightText = newValue
                        onFaceWeightChange(newValue.toIntOrNull() ?: 1)
                    }
                },
                label = { Text("Poids") },
                modifier = Modifier.width(100.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}
