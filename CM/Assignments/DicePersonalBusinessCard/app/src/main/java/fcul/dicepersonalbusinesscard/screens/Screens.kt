package fcul.dicepersonalbusinesscard.screens

sealed class Screens(val route: String) {
    object Roller : Screens("roll_screen")
    object BusinessCard : Screens("business_card_screen")
    object DiceResult : Screens("dice_result_screen/{result}")
    object DiceResultIncrement : Screens("dice_result_increment_screen/{result}")
    object DiceResultMenu : Screens("dice_result_menu_screen")
    object DiceResultTextField : Screens("dice_result_textfield_screen/{result}")
    object DiceResultDouble : Screens("dice_result_double_screen/{current}/{previous}")
}