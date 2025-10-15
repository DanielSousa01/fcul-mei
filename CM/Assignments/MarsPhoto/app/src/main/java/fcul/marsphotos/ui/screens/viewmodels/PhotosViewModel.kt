package fcul.marsphotos.ui.screens.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fcul.marsphotos.network.FirebaseService
import fcul.marsphotos.network.MarsApi
import fcul.marsphotos.network.MarsPhoto
import fcul.marsphotos.network.PhotoPair
import fcul.marsphotos.network.RandomApi
import fcul.marsphotos.network.RandomPhoto
import kotlinx.coroutines.launch
import java.io.IOException

class PhotosViewModel : ViewModel() {
    var photosUiState: PhotosUiState by mutableStateOf(PhotosUiState.Loading)
        private set

    private var rollsCounter: Int = 0

    private var showSaveDialog = false

    private lateinit var marsPhotos: List<MarsPhoto>
    private lateinit var marsPhoto: String

    private lateinit var randomPhotos: List<RandomPhoto>
    private lateinit var randomPhoto: String

    private val firebaseService = FirebaseService()

    init {
        getPhotos()
    }

    private fun getPhotos() {
        viewModelScope.launch {
            rollsCounter = firebaseService.getRolls()
            marsPhotos = MarsApi.retrofitService.getMarsPhotos()
            randomPhotos = RandomApi.retrofitService.getRandomPhotos()

            randomPhoto = randomPhotos.random().imgSrc
            marsPhoto = marsPhotos.random().imgSrc

            updateUiState()
        }
    }

    private fun updateUiState() {
        try {
            photosUiState =
                if (this::marsPhotos.isInitialized && this::randomPhotos.isInitialized) {
                    PhotosUiState.Success(
                        totalRolls = rollsCounter,
                        marsPhotos = "Success: ${marsPhotos.size} Mars photos retrieved",
                        marsPhotoUri = marsPhoto,
                        randomPhotos = "Success: ${randomPhotos.size} Random photos retrieved",
                        randomPhotoUri = randomPhoto,
                        showSaveDialog = showSaveDialog,
                        dismissDialog = { dismissDialog() },
                        savePhotos = { savePhotos() },
                        LoadPhotos = { loadLastSavedPhotos() },
                        toggleBlur = { toggleBlur() },
                        toggleGrayScale = { toggleGrayScale() },
                        randomize = { randomizePhotos() },
                    )
                } else {
                    PhotosUiState.Loading
                }
        } catch (e: IOException) {
            Log.e("PhotosViewModel", "Failure: ${e.message}")
            photosUiState = PhotosUiState.Error
        }

    }

    private fun incrementRolls() {
        viewModelScope.launch {
            try {
                firebaseService.incrementRolls()
            } catch (e: IOException) {
                Log.e("PhotosViewModel", "Failure: ${e.message}")
            }
        }
        updateUiState()
    }

    private fun toggleBlur() {
        Log.d("PhotosViewModel", "Toggling blur for $randomPhoto")
        val uri = randomPhoto.toUri()
        val uriBuilder = uri.buildUpon()
        randomPhoto = if (uri.queryParameterNames.contains("blur")) {
            uriBuilder.clearQuery()
            uriBuilder.build().toString()
        } else {
            uriBuilder.appendQueryParameter("blur", "10")
            uriBuilder.build().toString()
        }
        updateUiState()
    }

    private fun toggleGrayScale() {
        Log.d("PhotosViewModel", "Toggling grayscale for $randomPhoto")
        val uri = randomPhoto.toUri()
        val uriBuilder = uri.buildUpon()
        randomPhoto = if (uri.queryParameterNames.contains("grayscale")) {
            uriBuilder.clearQuery()
            uriBuilder.build().toString()
        } else {
            uriBuilder.appendQueryParameter("grayscale", null)
            uriBuilder.build().toString()
        }
        updateUiState()
    }

    private fun randomizePhotos() {
        incrementRolls()
        randomPhoto = randomPhotos.random().imgSrc
        marsPhoto = marsPhotos.random().imgSrc
        ++rollsCounter
        updateUiState()
    }

    private fun savePhotos() {
        viewModelScope.launch {
            try {
                firebaseService.savePhotos(
                    PhotoPair(
                        marsPhoto,
                        randomPhoto,
                    ),
                )
                showSaveDialog = true
                updateUiState()
            } catch (e: IOException) {
                Log.e("PhotosViewModel", "Failure: ${e.message}")
            }
        }
    }

    private fun loadLastSavedPhotos() {
        viewModelScope.launch {
            try {
                val lastPhotoPair = firebaseService.getLastPhotoPair()
                if (lastPhotoPair != null) {
                    randomPhoto = lastPhotoPair.randomPhotoUri
                    marsPhoto = lastPhotoPair.marsPhotoUri
                }
                updateUiState()
            } catch (e: IOException) {
                Log.e("PhotosViewModel", "Failure: ${e.message}")
            }
        }
    }

    private fun dismissDialog() {
        showSaveDialog = false
        updateUiState()
    }
}
