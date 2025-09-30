package fcul.diceroller

sealed class Screens(val route: String) {
    object Roller : Screens("roll_screen")
    object DiceResult : Screens("result_screen/{result}")
}