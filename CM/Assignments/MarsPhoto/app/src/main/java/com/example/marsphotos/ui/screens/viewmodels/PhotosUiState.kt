package com.example.marsphotos.ui.screens.viewmodels

import com.example.marsphotos.network.MarsPhoto
import com.example.marsphotos.network.RandomPhoto

sealed interface PhotosUiState {
    data class Success(
        val marsPhotos: String,
        val marsPhoto: MarsPhoto,
        val randomPhotos: String,
        val randomPhoto: RandomPhoto,
        val randomize: () -> Unit,
    ) : PhotosUiState

    object Error : PhotosUiState

    object Loading : PhotosUiState
}
