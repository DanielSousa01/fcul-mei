package com.example.personalbusinesscard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personalbusinesscard.ui.theme.PersonalBusinessCardTheme

@Composable
fun Profile(modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .size(350.dp, 300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.profile),
                contentDescription = null,
                modifier = modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .graphicsLayer(
                        scaleX = 1.5f,
                        scaleY = 1.5f
                    )
            )

            Spacer(modifier = modifier.padding(10.dp))

            Text(
                text = "Daniel Martins Cabrita de Sousa",
                modifier = modifier
                    .padding(bottom = 8.dp),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Text(
                text = "MSc Student in Software Engineering",
                modifier = modifier
                    .padding(bottom = 24.dp),
                color = Color.White
            )
        }

    }
}

@Preview
@Composable
fun ProfilePreview() {
    PersonalBusinessCardTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Profile(Modifier)
        }
    }
}