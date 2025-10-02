package fcul.dicepersonalbusinesscard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import fcul.dicepersonalbusinesscard.navigation.NavGraph
import fcul.dicepersonalbusinesscard.ui.theme.DicePersonalBusinessCardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DicePersonalBusinessCardTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}