package fcul.dicepersonalbusinesscard.screens.dice.resulttextfield

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fcul.dicepersonalbusinesscard.R
import fcul.dicepersonalbusinesscard.screens.Screens
import fcul.dicepersonalbusinesscard.ui.theme.DicePersonalBusinessCardTheme
import fcul.dicepersonalbusinesscard.utils.TransparentButton

@Composable
fun DiceResultTextFieldScreen(
    navController: NavController,
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    resultShow: Int = 1
) {
    var resultText by remember { mutableStateOf(resultShow.toString()) }
    val result = resultText.toIntOrNull()?.coerceIn(1, 6) ?: 1
    val diceFace = Screens.getDiceFace(result)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(25.dp)

    ) {
        Image(
            painter = painterResource(diceFace.image),
            contentDescription = "Dice showing $result"
        )
        TransparentButton(
            onClick = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("newResult", result)
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("newPrevious", resultShow)

                navController.popBackStack()
            },
        ) {
            Text(text = stringResource(R.string.back), fontSize = 24.sp)
        }
        TextField(
            value = resultText,
            onValueChange = { newValue ->
                val intValue = newValue.toIntOrNull()
                if (intValue == null || (intValue in 1..6) || newValue.isEmpty()) {
                    resultText = newValue
                }
            },
            placeholder = { Text(text = stringResource(R.string.enter_number)) },
            singleLine = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DiceResultTextFieldPreview() {
    DicePersonalBusinessCardTheme {
        DiceResultTextFieldScreen(navController = NavController(LocalContext.current))
    }
}