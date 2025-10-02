package fcul.dicepersonalbusinesscard.screens

import fcul.dicepersonalbusinesscard.R

sealed class Screens(val route: String) {
    companion object {
        data class Faces(val image: Int, val screen: (result: Int, previous: Int) -> String)

        private val facesList = listOf(
            Faces(R.drawable.dice_1, { result, previous -> BusinessCard.route }),
            Faces(
                R.drawable.dice_2,
                { result, previous ->
                    DiceResult.route
                        .replace(oldValue = "{result}", newValue = result.toString())
                }
            ),
            Faces(
                R.drawable.dice_3,
                { result, previous ->
                    DiceResultIncrement.route
                        .replace(oldValue = "{result}", newValue = result.toString())
                }
            ),
            Faces(
                R.drawable.dice_4,
                { result, previous ->
                    DiceResultMenu.route
                        .replace(oldValue = "{result}", newValue = result.toString())
                        .replace(oldValue = "{previous}", newValue = previous.toString())
                }
            ),
            Faces(
                R.drawable.dice_5,
                { result, previous ->
                    DiceResultTextField.route
                        .replace(oldValue = "{result}", newValue = result.toString())
                }
            ),
            Faces(
                R.drawable.dice_6,
                { result, previous ->
                    DiceResultPrev.route
                        .replace(oldValue = "{result}", newValue = result.toString())
                        .replace(oldValue = "{previous}", newValue = previous.toString())
                }
            )
        )

        fun getDiceFace(result: Int) = facesList[result - 1]
    }

    object Roller : Screens("roll_screen")
    object BusinessCard : Screens("business_card_screen")
    object DiceResult : Screens("dice_result_screen/{result}")
    object DiceResultIncrement : Screens("dice_result_increment_screen/{result}")
    object DiceResultMenu : Screens("dice_result_menu_screen/{result}/{previous}")
    object DiceResultTextField : Screens("dice_result_textfield_screen/{result}")
    object DiceResultPrev : Screens("dice_result_prev_screen/{result}/{previous}")


}