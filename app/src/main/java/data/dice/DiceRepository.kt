package data.dice

import domain.model.Dice
import domain.model.DiceFace
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class DiceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _dices = MutableStateFlow<List<Dice>>(emptyList())
    val dices: StateFlow<List<Dice>> = _dices

    suspend fun loadUserDices() {
        val userId = auth.currentUser?.uid ?: return
        try {
            val snapshot = db.collection("dices")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val diceList = snapshot.toObjects(Dice::class.java)
            _dices.value = diceList
        } catch (e: Exception) {
            // Gérer l'erreur
        }
    }

    suspend fun updateDice(dice: Dice, diceId: String) {
        try {
            db.collection("dices").document(diceId).set(dice).await()
            loadUserDices()
        } catch (e: Exception) {
            // Gérer l'erreur
        }
    }
}
