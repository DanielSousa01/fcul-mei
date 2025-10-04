package com.example.marsphotos.ui.screens.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marsphotos.network.MarsApi
import com.example.marsphotos.network.MarsPhoto
import com.example.marsphotos.network.RandomApi
import com.example.marsphotos.network.RandomPhoto
import kotlinx.coroutines.launch
import java.io.IOException

class PhotosViewModel : ViewModel() {
    var photosUiState: PhotosUiState by mutableStateOf(PhotosUiState.Loading)
        private set

    private lateinit var marsPhotos: List<MarsPhoto>
    private lateinit var randomPhotos: List<RandomPhoto>

    init {
        getPhotos()
    }

    private fun getPhotos() {
        viewModelScope.launch {
            try {
                marsPhotos = MarsApi.retrofitService.getMarsPhotos()
                randomPhotos = RandomApi.retrofitService.getRandomPhotos()

                photosUiState =
                    PhotosUiState.Success(
                        marsPhotos = "Success: ${marsPhotos.size} Mars photos retrieved",
                        marsPhoto = marsPhotos.random(),
                        randomPhotos = "Success: ${randomPhotos.size} Random photos retrieved",
                        randomPhoto = randomPhotos.random(),
                        randomize = { randomizePhotos() },
                    )
            } catch (e: IOException) {
                Log.e("PhotosViewModel", "Failure: ${e.message}")
                photosUiState = PhotosUiState.Error
            }
        }
    }

    private fun randomizePhotos() {
        photosUiState = if (this::marsPhotos.isInitialized && this::randomPhotos.isInitialized) {
            PhotosUiState.Success(
                marsPhotos = "Success: ${marsPhotos.size} Mars photos retrieved",
                marsPhoto = marsPhotos.random(),
                randomPhotos = "Success: ${randomPhotos.size} Random photos retrieved",
                randomPhoto = randomPhotos.random(),
                randomize = { randomizePhotos() },
            )
        } else {
            PhotosUiState.Loading
        }

    }
}
