package com.example.personalbusinesscard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
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
            FculIcon(modifier, context)
            Profile(modifier)
            ContactInfo(modifier, context)
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
