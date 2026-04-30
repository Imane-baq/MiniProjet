package domain.model

import com.google.firebase.firestore.PropertyName

data class DiceFace(
    @get:PropertyName("faceValue") @set:PropertyName("faceValue") var faceValue: String = "1",
    @get:PropertyName("faceWeight") @set:PropertyName("faceWeight") var faceWeight: Int = 1
)
