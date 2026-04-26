package ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.miniprojet.ui.auth.AuthUiState
import ui.dicePages.AddDicePage
import ui.dicePages.CustomizeDicePage
import ui.dicePages.RemoveDicePage
import ui.dicePages.ThrowDicePage

@Composable
fun DiceOptionsScreen(
    uiState: AuthUiState,
    navController: NavController,
    modifier: Modifier = Modifier,
    )
{

    val navItemList = listOf(
        NavItem("Ajouter dés", Icons.Default.Add),
        NavItem("Enlever dés", Icons.Default.Clear),
        NavItem("Modifier dés", Icons.Default.Create),
        NavItem("Lancer dés", Icons.Default.PlayArrow)
    )

    var selectedIndex by remember {
        mutableStateOf(0)
    }
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF4D27A)
            ) {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = index==selectedIndex,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = {
                            Icon(imageVector = navItem.icon, contentDescription = navItem.label)
                        },
                        label = {
                            Text(text = navItem.label)
                        }
                    )
                }
            }
        }
    ) {
        ContentScreen(modifier = modifier.padding(it), selectedIndex)
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex : Int) {
    when (selectedIndex) {
        0 -> AddDicePage(modifier)
        1 -> RemoveDicePage(modifier)
        2 -> CustomizeDicePage(modifier)
        3 -> ThrowDicePage(modifier)
    }
}

data class NavItem (
    val label : String,
    val icon : ImageVector
)

// Icons :
// Icons.Default.Add   Icons.Default.Clear   Icons.Default.Create   Icons.Default.PlayArrow