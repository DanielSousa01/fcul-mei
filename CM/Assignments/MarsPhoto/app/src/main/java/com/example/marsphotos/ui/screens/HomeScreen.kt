/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.marsphotos.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.marsphotos.ui.screens.viewmodels.PhotosUiState

@Composable
fun HomeScreen(
    photosUiState: PhotosUiState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    when (photosUiState) {
        is PhotosUiState.Loading -> LoadingScreen(modifier = modifier.fillMaxWidth())
        is PhotosUiState.Success ->
            ResultScreen(
                totalRolls = photosUiState.totalRolls,
                marsPhotos = photosUiState.marsPhotos,
                marsPhotoUri = photosUiState.marsPhotoUri,
                randomPhotos = photosUiState.randomPhotos,
                randomPhotoUri = photosUiState.randomPhotoUri,
                showSaveDialog = photosUiState.showSaveDialog,
                dismissDialog = photosUiState.dismissDialog,
                savePhotos = photosUiState.savePhotos,
                loadPhotos = photosUiState.LoadPhotos,
                toggleBlur = photosUiState.toggleBlur,
                toggleGrayScale = photosUiState.toggleGrayScale,
                randomize = photosUiState.randomize,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
            )

        is PhotosUiState.Error -> ErrorScreen(modifier = modifier.fillMaxSize())
    }
}
