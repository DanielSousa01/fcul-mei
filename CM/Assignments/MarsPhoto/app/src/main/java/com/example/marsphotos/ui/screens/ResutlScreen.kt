package com.example.marsphotos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marsphotos.R
import com.example.marsphotos.network.PhotoData

@Composable
fun ResultScreen(
    marsPhotos: String,
    marsPhoto: PhotoData,
    randomPhotos: String,
    randomPhoto: PhotoData,
    randomize: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var blur by remember { mutableStateOf(false) }
        var grayScale by remember { mutableStateOf(false) }

        val imagesModifier =
            Modifier
                .fillMaxWidth()
                .weight(1f)

        ImagePlaceholder(
            randomPhotos,
            randomPhoto,
            imagesModifier,
            blur,
            grayScale
        )
        ImagePlaceholder(
            marsPhotos,
            marsPhoto,
            imagesModifier,
        )
        OptionMenu(
            randomize,
            { blur = !blur },
            { grayScale = !grayScale }
        )
    }
}

@Composable
fun ImagePlaceholder(
    photos: String,
    photo: PhotoData,
    modifier: Modifier = Modifier,
    blurImage: Boolean = false,
    grayScaleImage: Boolean = false
) {
    val uriBuilder = photo.imgSrc.toUri().buildUpon()
    if (blurImage) uriBuilder.appendQueryParameter("blur", "10")
    if (grayScaleImage) uriBuilder.appendQueryParameter("grayscale", null)
    val finalUri = uriBuilder.build()

    Text(text = photos)
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(finalUri)
            .crossfade(true)
            .build(),
        contentDescription = stringResource(R.string.photos_api),
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@Composable
fun OptionMenu(
    randomize: () -> Unit,
    blur: () -> Unit,
    grayScale: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = randomize) {
            Text(text = stringResource(R.string.roll))
        }
        Button(onClick = blur) {
            Text(text = stringResource(R.string.blur))
        }
        Button(onClick = grayScale) {
            Text(text = stringResource(R.string.gray_scale))
        }
    }
}

