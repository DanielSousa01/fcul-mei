package fcul.dicepersonalbusinesscard.screens.dice.roller

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun DiceRollerScreen(
    navController: NavController,
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)
) {
    var result by remember { mutableIntStateOf(1) }
    var previousResult by remember { mutableIntStateOf(1) }

    val newResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<Int>("newResult")

    val newPrevious = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<Int>("newPrevious")

    LaunchedEffect(newResult) {
        newResult?.let {
            result = it
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<Int>("newResult")
        }
    }

    LaunchedEffect(newPrevious) {
        newPrevious?.let {
            previousResult = it
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<Int>("newPrevious")
        }
    }

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
                previousResult = result
                result = (1..6).random()
            },
        ) {
            Text(text = stringResource(R.string.roll), fontSize = 24.sp)
        }
        TransparentButton(
            onClick = { navController.navigate(diceFace.screen(result, previousResult)) }
        ) {
            Text(text = stringResource(R.string.go_to_result), fontSize = 24.sp)
        }

    }
}

@Preview(showBackground = true)
@Composable
fun DiceRollerScreenPreview() {
    DicePersonalBusinessCardTheme {
        DiceRollerScreen(navController = NavController(LocalContext.current))
    }
}
