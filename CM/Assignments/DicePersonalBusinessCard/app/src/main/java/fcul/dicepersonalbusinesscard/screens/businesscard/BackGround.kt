package fcul.dicepersonalbusinesscard.screens.businesscard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import fcul.dicepersonalbusinesscard.R
import fcul.dicepersonalbusinesscard.ui.theme.DicePersonalBusinessCardTheme

@Composable
fun BackGround(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.background),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
    )
}

@Preview(showBackground = true)
@Composable
fun BackGroundPreview() {
    DicePersonalBusinessCardTheme {
        BackGround()
    }
}