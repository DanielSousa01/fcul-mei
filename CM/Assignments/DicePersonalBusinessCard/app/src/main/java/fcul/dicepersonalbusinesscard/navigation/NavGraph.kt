package fcul.dicepersonalbusinesscard.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fcul.dicepersonalbusinesscard.screens.Screens
import fcul.dicepersonalbusinesscard.screens.businesscard.BusinessCardScreen
import fcul.dicepersonalbusinesscard.screens.dice.result.DiceResultScreen
import fcul.dicepersonalbusinesscard.screens.dice.resultincrement.DiceResultIncrementScreen
import fcul.dicepersonalbusinesscard.screens.dice.resultmenu.DiceResultMenuScreen
import fcul.dicepersonalbusinesscard.screens.dice.resultprev.DiceResultPrevScreen
import fcul.dicepersonalbusinesscard.screens.dice.resulttextfield.DiceResultTextFieldScreen
import fcul.dicepersonalbusinesscard.screens.dice.roller.DiceRollerScreen


@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screens.Roller.route
    ) {
        composable(Screens.Roller.route) {
            DiceRollerScreen(navController = navController)
        }
        composable(Screens.BusinessCard.route) {
            BusinessCardScreen(navController = navController)
        }
        composable(Screens.DiceResult.route) { navBackStack->
            val resultShow: Int = navBackStack.arguments?.getString("result")?.toIntOrNull()?:1
            DiceResultScreen(navController = navController, resultShow = resultShow)
        }
        composable(Screens.DiceResultIncrement.route) { navBackStack->
            val resultShow: Int = navBackStack.arguments?.getString("result")?.toIntOrNull()?:1
            DiceResultIncrementScreen(navController = navController, resultShow = resultShow)
        }
        composable(Screens.DiceResultMenu.route) {navBackStack->
            val resultShow: Int = navBackStack.arguments?.getString("result")?.toIntOrNull()?:1
            val previousShow: Int = navBackStack.arguments?.getString("previous")?.toIntOrNull()?:1
            DiceResultMenuScreen(
                navController = navController,
                resultShow = resultShow,
                previousShow = previousShow
            )
        }
        composable(Screens.DiceResultTextField.route) { navBackStack->
            val resultShow: Int = navBackStack.arguments?.getString("result")?.toIntOrNull()?:1
            DiceResultTextFieldScreen(navController = navController, resultShow = resultShow)
        }
        composable(Screens.DiceResultPrev.route) { navBackStack->
            val resultShow: Int = navBackStack.arguments?.getString("result")?.toIntOrNull()?:1
            val previousShow: Int = navBackStack.arguments?.getString("previous")?.toIntOrNull()?:1
            DiceResultPrevScreen(
                navController = navController,
                resultShow = resultShow,
                previousShow = previousShow
            )
        }

    }
}