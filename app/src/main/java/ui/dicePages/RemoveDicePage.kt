package ui.dicePages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import domain.model.Dice

data class DiceDocument(
    val documentId: String,
    val dice: Dice
)

@Composable
fun RemoveDicePage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    val diceDocuments = remember { mutableStateListOf<DiceDocument>() }

    fun loadDice() {
        if (currentUserId == null) {
            Toast.makeText(context, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("dices")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                diceDocuments.clear()

                snapshot.documents.forEach { document ->
                    val dice = document.toObject(Dice::class.java)

                    if (dice != null) {
                        diceDocuments.add(
                            DiceDocument(
                                documentId = document.id,
                                dice = dice
                            )
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Erreur chargement : ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun deleteDice(diceDocument: DiceDocument) {
        if (currentUserId == null) {
            Toast.makeText(context, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show()
            return
        }

        if (diceDocument.dice.userId != currentUserId) {
            Toast.makeText(context, "Ce dé ne t'appartient pas.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("dices")
            .document(diceDocument.documentId)
            .delete()
            .addOnSuccessListener {
                diceDocuments.remove(diceDocument)

                Toast.makeText(
                    context,
                    "Dé supprimé de la base de données.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Erreur suppression : ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    LaunchedEffect(currentUserId) {
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
            text = "Enlever un dé",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Serif,
            color = Color(0xFFF4D27A)
        )

        if (diceDocuments.isEmpty()) {
            EmptyDiceMessage()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = diceDocuments,
                    key = { it.documentId }
                ) { diceDocument ->
                    RemoveDiceCard(
                        diceDocument = diceDocument,
                        onRemove = {
                            deleteDice(diceDocument)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDiceMessage() {
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
                text = "Aucun dé à enlever",
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
private fun RemoveDiceCard(
    diceDocument: DiceDocument,
    onRemove: () -> Unit
) {
    val dice = diceDocument.dice

    Card(
        shape = CutCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4D27A)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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

            Button(
                onClick = onRemove,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B0000)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Supprimer ce dé")
            }
        }
    }
}