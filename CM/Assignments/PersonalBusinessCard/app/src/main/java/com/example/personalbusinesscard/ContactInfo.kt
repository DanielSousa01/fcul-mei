package com.example.personalbusinesscard

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.personalbusinesscard.ui.theme.PersonalBusinessCardTheme

@Composable
fun ContactInfo(modifier: Modifier) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.padding(16.dp),
    ) {
        val buttons = listOf<ContactButton>(
            ContactButton(
                onClick = {
                    val intent = Intent(
                    Intent.ACTION_DIAL,
                    "tel:+351962005244".toUri()
                )
                    context.startActivity(intent)
                },
                icon = ContactIcon.VectorIcon(Icons.Default.Phone),
                description = "Phone"
            ),
            ContactButton(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://www.instagram.com/daniel.sousa245?igsh=NTI5dnc3cXB4dTgw".toUri()
                    )
                    intent.setPackage("com.instagram.android")
                    context.startActivity(intent)
                },
                icon =  ContactIcon.PainterIcon(painterResource(R.drawable.instagram)),
                description = "Instagram"
            ),
            ContactButton(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_SENDTO,
                        "mailto:fc66128@alunos.fc.ul.pt".toUri()
                    )
                    context.startActivity(intent)
                },
                icon = ContactIcon.VectorIcon(Icons.Default.Email),
                description = "Email"
            ),
            ContactButton(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://www.linkedin.com/in/daniel-sousa-36b890328?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app".toUri()
                    )
                    intent.setPackage("com.linkedin.android")
                    context.startActivity(intent)
                },
                icon = ContactIcon.PainterIcon(painterResource(R.drawable.linkedin)),
                description = "LinkedIn"
            ),
            ContactButton(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://github.com/DanielSousa01".toUri()
                    )
                    intent.setPackage("com.github.android")
                    context.startActivity(intent)
                },
                icon = ContactIcon.PainterIcon(painterResource(R.drawable.github)),
                description = "GitHub"
            ),

        )
        ContactButtons(contactButtons = buttons, modifier = modifier)
    }
}

@Composable
fun ContactButtons(contactButtons: List<ContactButton>, modifier: Modifier = Modifier) {
    for (buttonIdx in 0 until contactButtons.size step 2) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            val firstButton = contactButtons[buttonIdx]
            val secondButton = contactButtons.getOrNull(buttonIdx + 1)

            repeat(if (secondButton != null) 2 else 1) { idx ->
                val button = if (idx == 0) {
                    firstButton
                }
                else {
                    requireNotNull(secondButton) {"Second button should not be null here"}
                    secondButton
                }

                Button(
                    onClick = button.onClick,
                    modifier = modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.25f),
                        contentColor = Color.White
                    )
                ) {
                    if (button.icon is ContactIcon.VectorIcon) {
                        Icon(button.icon.imageVector,null)
                    } else {
                        require(button.icon is ContactIcon.PainterIcon)
                        {"Icon should be a PainterIcon here"}
                        Icon(button.icon.painter,null
                        )
                    }
                    Spacer(modifier.width(8.dp))
                    Text(button.description, style = MaterialTheme.typography.bodyLarge)
                }
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