package com.example.miniprojet.data.auth

import com.example.miniprojet.domain.model.AppUser
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<AppUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Utilisateur introuvable."))

            val user = AppUser(
                uid = firebaseUser.uid,
                name = name,
                email = email
            )

            saveUserProfile(user)

            Result.success(user)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<AppUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Utilisateur introuvable."))

            getUserProfile(firebaseUser.uid)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun loginWithGoogle(account: GoogleSignInAccount): Result<AppUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()

            val firebaseUser = result.user
                ?: return Result.failure(Exception("Utilisateur Google introuvable."))

            val user = AppUser(
                uid = firebaseUser.uid,
                name = firebaseUser.displayName ?: "Joueur",
                email = firebaseUser.email ?: ""
            )

            saveUserProfile(user)

            Result.success(user)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getCurrentUserProfile(): Result<AppUser> {
        val userId = getCurrentUserId()
            ?: return Result.failure(Exception("Aucun utilisateur connecté."))

        return getUserProfile(userId)
    }

    fun logout() {
        auth.signOut()
    }

    private suspend fun saveUserProfile(user: AppUser) {
        db.collection("users")
            .document(user.uid)
            .set(user)
            .await()
    }

    private suspend fun getUserProfile(userId: String): Result<AppUser> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .get()
                .await()

            val user = snapshot.toObject(AppUser::class.java)
                ?: return Result.failure(Exception("Profil utilisateur introuvable."))

            Result.success(user)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}