package com.example.personalbusinesscard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personalbusinesscard.ui.theme.PersonalBusinessCardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalBusinessCardTheme {
                MainContent(Modifier)
            }
        }
    }
}

@Composable
fun MainContent(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        BackGround(modifier)
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(top = 32.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(50.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.fcul),
                contentDescription = null,
                modifier = modifier
                    .width(8000.dp)
                    .systemBarsPadding(),
                alignment = Alignment.TopStart,
            )

            Profile(modifier)
            ContactInfo(modifier)
        }
    }
}

@Preview
@Composable
fun MainContentPreview() {
    PersonalBusinessCardTheme {
        MainContent(Modifier)
    }
}
