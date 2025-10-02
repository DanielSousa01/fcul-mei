package fcul.dicepersonalbusinesscard.screens.dice.result

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
fun DiceResultScreen(
    navController: NavController,
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    resultShow: Int = 1
) {
    var result by remember { mutableIntStateOf(resultShow) }
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
                navController.popBackStack()
            },
        ) {
            Text(text = stringResource(R.string.back), fontSize = 24.sp)
        }

    }
}

@Preview(showBackground = true)
@Composable
fun DiceResultScreenPreview() {
    DicePersonalBusinessCardTheme {
        DiceResultScreen(navController = NavController(LocalContext.current))
    }
}