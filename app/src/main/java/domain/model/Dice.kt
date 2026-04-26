package domain.model

import com.google.firebase.firestore.PropertyName

data class Dice(
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("diceName") @set:PropertyName("diceName") var diceName: String = "",
    @get:PropertyName("diceWeight") @set:PropertyName("diceWeight") var diceWeight: Int = 0,
    @get:PropertyName("diceFaces") @set:PropertyName("diceFaces") var diceFaces: List<DiceFace> = emptyList()
)
