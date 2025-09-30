package fcul.dicepersonalbusinesscard.screens.dice.resultmenu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
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
fun DiceResultMenuScreen(
    navController: NavController,
    modifier: Modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
    resultShow: Int = 1,
    previousShow: Int = 1
) {
    var result by remember { mutableIntStateOf(resultShow) }
    var previousResult by remember { mutableIntStateOf(previousShow) }

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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(25.dp)

    ) {
        TransparentButton(
            onClick = {
                navController.popBackStack()
            },
        ) {
            Text(text = stringResource(R.string.back), fontSize = 24.sp)
        }

        TransparentButton(
            onClick = {
                navController.navigate(Screens.BusinessCard.route)
            },
        ) {
            Text(text = stringResource(R.string.screen1), fontSize = 24.sp)
        }

        TransparentButton(
            onClick = {
                navController.navigate(Screens.DiceResult.route
                    .replace("{result}", result.toString()))
            },
        ) {
            Text(text = stringResource(R.string.screen2), fontSize = 24.sp)
        }

        TransparentButton(
            onClick = {
                navController.navigate(Screens.DiceResultIncrement.route
                    .replace("{result}", result.toString()))
            },
        ) {
            Text(text = stringResource(R.string.screen3), fontSize = 24.sp)
        }

        TransparentButton(
            onClick = {
                navController.navigate(Screens.DiceResultTextField.route
                    .replace("{result}", result.toString()))
            },
        ) {
            Text(text = stringResource(R.string.screen5), fontSize = 24.sp)
        }

        TransparentButton(
            onClick = {
                navController.navigate(Screens.DiceResultPrev.route
                    .replace("{result}", result.toString())
                    .replace("{previous}", previousResult.toString())
                )
            },
        ) {
            Text(text = stringResource(R.string.screen6), fontSize = 24.sp)
        }

    }
}

@Preview(showBackground = true)
@Composable
fun DiceResultMenuScreenPreview() {
    DicePersonalBusinessCardTheme {
        DiceResultMenuScreen(navController = NavController(LocalContext.current))
    }
}