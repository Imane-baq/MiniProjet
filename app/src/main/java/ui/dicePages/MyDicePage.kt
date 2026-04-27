package ui.dicePages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import domain.model.Dice

@Composable
fun MyDicePage(
    modifier: Modifier = Modifier,
    onGoToAccount: () -> Unit
) {
    val diceList = remember { mutableStateListOf<Dice>() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(userId) {
        if (userId == null) return@LaunchedEffect

        db.collection("dices")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                diceList.clear()

                val dices = snapshot.documents.mapNotNull { document ->
                    document.toObject(Dice::class.java)
                }

                diceList.addAll(dices)
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF2B1D14))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mes dés",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Serif,
                color = Color(0xFFF4D27A)
            )

            Button(
                onClick = onGoToAccount,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B3F00)
                )
            ) {
                Text("Mon compte")
            }
        }

        if (diceList.isEmpty()) {
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
                        text = "Aucun dé créé",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF3B2416)
                    )

                    Text(
                        text = "Crée un dé avec l’option Ajouter dés dans la barre du bas.",
                        color = Color(0xFF5C3B24)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(diceList) { dice ->
                    DiceCard(dice = dice)
                }
            }
        }
    }
}

@Composable
private fun DiceCard(dice: Dice) {
    Card(
        shape = CutCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4D27A)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
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
    }
}