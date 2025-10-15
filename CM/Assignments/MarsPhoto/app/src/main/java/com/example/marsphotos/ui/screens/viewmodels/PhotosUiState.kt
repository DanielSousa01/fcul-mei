package com.example.marsphotos.ui.screens.viewmodels

import com.example.marsphotos.network.MarsPhoto
import com.example.marsphotos.network.PhotoPair
import com.example.marsphotos.network.RandomPhoto

sealed interface PhotosUiState {
    data class Success(
        val totalRolls: Int,
        val marsPhotos: String,
        val marsPhotoUri: String,
        val randomPhotos: String,
        val randomPhotoUri: String,
        val showSaveDialog: Boolean,
        val dismissDialog: () -> Unit,
        val savePhotos: () -> Unit,
        val LoadPhotos: () -> Unit,
        val toggleBlur: () -> Unit,
        val toggleGrayScale: () -> Unit,
        val randomize: () -> Unit,
    ) : PhotosUiState

    object Error : PhotosUiState

    object Loading : PhotosUiState
}
