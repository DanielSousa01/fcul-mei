package com.example.personalbusinesscard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.personalbusinesscard.ui.theme.PersonalBusinessCardTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun BackGround(modifier: Modifier) {
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
    PersonalBusinessCardTheme {
        BackGround(Modifier)
    }
}