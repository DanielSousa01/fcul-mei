package fcul.diceroller.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fcul.diceroller.DiceResultScreen
import fcul.diceroller.DiceRollerScreen
import fcul.diceroller.Screens

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screens.Roller.route
    ) {
        composable(Screens.Roller.route) {
            DiceRollerScreen(navController = navController)
        }
        composable(Screens.DiceResult.route) {
            DiceResultScreen(navController = navController)
        }

        composable(route = Screens.DiceResult.route+ "?result={result}"){ navBackStack->
            var resultShow: Int = navBackStack.arguments?.getString("result")?.toIntOrNull()?:1

            DiceResultScreen(navController = navController, resultShow = resultShow) }
    }
}




