package ui.dicePages

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import domain.model.Dice
import domain.model.DiceFace

@Composable
fun AddDicePage(modifier: Modifier = Modifier) {

    var diceName by remember { mutableStateOf("") }
    var numberOfSides by remember { mutableStateOf("") }
    val db = Firebase.firestore
    val user = FirebaseAuth.getInstance().currentUser
    val uId = user?.uid
    val context = LocalContext.current

    Column(
        modifier = Modifier
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
                        if(diceName.isNotEmpty() && uId != null && numberOfSides.isNotEmpty() && numberOfSides.toInt() >=4 && numberOfSides.toInt() <= 20) {
                            val mutDiceFaces = mutableListOf<DiceFace>()
                            for(i in 1..numberOfSides.toInt()) {
                                val face = DiceFace(i.toString(), 1) //Default face value is the number of the face, weight is 1
                                mutDiceFaces.add(face)
                            }
                            val diceFaces = mutDiceFaces.toList()
                            val name = diceName
                            val dice = Dice(uId, name, numberOfSides.toInt(), diceFaces )
                            db.collection("dices").add(dice)
                                .addOnSuccessListener { documentReference ->
                                    Log.d("Firestore", "Dice added with ID: ${documentReference.id}")
                                    Toast.makeText(context, "Dice Added with ID: ${documentReference.id}", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Error adding Dice", e)
                                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }

                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B3F00)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ajouter le dé")
                }
            }
        }
    }
}