package fcul.dicepersonalbusinesscard.screens.businesscard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fcul.dicepersonalbusinesscard.R
import fcul.dicepersonalbusinesscard.ui.theme.DicePersonalBusinessCardTheme
import fcul.dicepersonalbusinesscard.utils.BackGround
import fcul.dicepersonalbusinesscard.utils.TransparentButton

@Composable
fun BusinessCardScreen(
    navController: NavController
) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp),
        ) {
            FculIcon(context = context)
            Profile()
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                ContactButtons(context = context)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TransparentButton(
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .weight(1f),
                    ) {
                        Text(text = stringResource(R.string.back))
                    }

                }

            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun BusinessCardPreview() {
    DicePersonalBusinessCardTheme {
        BusinessCardScreen(navController = NavController(LocalContext.current))
    }
}
