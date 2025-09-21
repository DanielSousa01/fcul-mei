package com.example.personalbusinesscard

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.personalbusinesscard.ui.theme.PersonalBusinessCardTheme
import androidx.core.net.toUri

@Composable
fun ContactInfo(modifier: Modifier) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.padding(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                val intent = Intent(
                    Intent.ACTION_DIAL,
                    "tel:+351962005244".toUri()
                )
                context.startActivity(intent)
            },
                modifier = modifier.weight(1f).height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.25f))
                ) {
                Icon(Icons.Default.Phone, contentDescription = null)
                Spacer(modifier.width(8.dp))
                Text("Phone", style = MaterialTheme.typography.bodyLarge)
            }
            Button(onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://www.instagram.com/daniel.sousa245?igsh=NTI5dnc3cXB4dTgw".toUri()
                )
                intent.setPackage("com.instagram.android")
                context.startActivity(intent)
            },
                modifier = modifier.weight(1f).height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.25f))
                ) {
                Icon(painterResource(R.drawable.instagram), contentDescription = null)
                Spacer(modifier.width(8.dp))
                Text("Instagram", style = MaterialTheme.typography.bodyLarge)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                val intent = Intent(
                    Intent.ACTION_SENDTO,
                    "mailto:fc66128@alunos.fc.ul.pt".toUri()
                )
                context.startActivity(intent)
            },
                modifier = modifier.weight(1f).height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.25f))
                ) {
                Icon(Icons.Default.Email, contentDescription = null)
                Spacer(modifier.width(8.dp))
                Text("Email")
            }
            Button(onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://www.linkedin.com/in/daniel-sousa-36b890328?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app".toUri()
                )
                intent.setPackage("com.linkedin.android")
                context.startActivity(intent)
            },
                modifier = modifier.weight(1f).height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.25f))
                ) {
                Icon(painterResource(R.drawable.linkedin), contentDescription = null)
                Spacer(modifier.width(8.dp))
                Text("LinkedIn")
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://github.com/DanielSousa01".toUri()
                )
                intent.setPackage("com.github.android")
                context.startActivity(intent)
            },
                modifier = modifier.weight(1f).height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.25f))
                ) {
                Icon(painterResource(R.drawable.github), contentDescription = null)
                Spacer(modifier.width(8.dp))
                Text("GitHub")
            }
        }
    }
}

@Preview
@Composable
fun ContactInfoPreview() {
    PersonalBusinessCardTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ContactInfo(Modifier)
        }
    }
}