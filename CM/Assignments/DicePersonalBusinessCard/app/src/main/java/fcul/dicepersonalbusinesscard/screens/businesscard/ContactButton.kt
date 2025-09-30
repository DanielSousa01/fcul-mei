package fcul.dicepersonalbusinesscard.screens.businesscard

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ContactIcon {
    data class PainterIcon(val painter: Painter) : ContactIcon()
    data class VectorIcon(val imageVector: ImageVector) : ContactIcon()
}

data class ContactButton(
    val onClick: () -> Unit,
    val icon: ContactIcon,
    val description: String
)
