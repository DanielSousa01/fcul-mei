package com.example.personalbusinesscard

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.personalbusinesscard.ui.theme.PersonalBusinessCardTheme

@Composable
fun FculIcon(modifier: Modifier, context: Context = LocalContext.current) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        Button(
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://ciencias.ulisboa.pt/".toUri()
                )
                context.startActivity(intent)
            },
            modifier = modifier.height(150.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black.copy(alpha = 0f),
                contentColor = Color.White
            )
        ) {
            Image(
                painter = painterResource(R.drawable.fcul),
                contentDescription = null,
                modifier = modifier.size(200.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FculIconPreview() {
    PersonalBusinessCardTheme {
        FculIcon(Modifier)
    }
}