package com.example.miniprojet.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.miniprojet.ui.auth.AuthUiState

@Composable
fun AccountScreen(
    uiState: AuthUiState,
    onLogout: () -> Unit
) {
    val user = uiState.user

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
                    text = "MON COMPTE",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF3B2416)
                )

                Text("Nom : ${user?.name ?: "Chargement..."}")
                Text("Email : ${user?.email ?: "Chargement..."}")

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B3F00)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Quitter la table")
                }
            }
        }
    }
}