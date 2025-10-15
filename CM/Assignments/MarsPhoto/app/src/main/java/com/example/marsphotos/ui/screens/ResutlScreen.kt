package com.example.marsphotos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marsphotos.R

@Composable
fun ResultScreen(
    totalRolls: Int,
    marsPhotos: String,
    marsPhotoUri: String,
    randomPhotos: String,
    randomPhotoUri: String,
    showSaveDialog: Boolean = false,
    dismissDialog: () -> Unit,
    savePhotos: () -> Unit,
    loadPhotos: () -> Unit,
    toggleBlur: () -> Unit,
    toggleGrayScale: () -> Unit,
    randomize: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = dismissDialog,
            title = { Text(text = stringResource(R.string.success)) },
            text = { Text(text = stringResource(R.string.success_save_text)) },
            confirmButton = {
                Button(onClick = dismissDialog) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val imagesModifier =
            Modifier
                .fillMaxWidth()
                .weight(1f)

        ImagePlaceholder(
            randomPhotos,
            randomPhotoUri,
            imagesModifier,
        )
        ImagePlaceholder(
            marsPhotos,
            marsPhotoUri,
            imagesModifier,
        )
        Text(text = stringResource(R.string.rolls, totalRolls))
        OptionMenu(
            {
                savePhotos()
            },
            loadPhotos,
            randomize,
            toggleBlur,
            toggleGrayScale
        )
    }
}



@Composable
fun ImagePlaceholder(
    photos: String,
    photoUri: String,
    modifier: Modifier = Modifier,
) {

    Text(text = photos)
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(photoUri)
            .crossfade(true)
            .build(),
        contentDescription = stringResource(R.string.photos_api),
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@Composable
fun OptionMenu(
    savePhotos: () -> Unit,
    loadPhotos: () -> Unit,
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = savePhotos) {
            Text(text = stringResource(R.string.save))
        }

        Button(onClick = loadPhotos) {
            Text(text = stringResource(R.string.load))
        }
    }

}

