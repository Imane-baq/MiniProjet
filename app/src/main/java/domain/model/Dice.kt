package domain.model

data class Dice(
    val userId: String = "",
    var diceName: String = "",
    var diceWeight: Int = 0,
    var diceFaces: List<DiceFace> = emptyList()
)