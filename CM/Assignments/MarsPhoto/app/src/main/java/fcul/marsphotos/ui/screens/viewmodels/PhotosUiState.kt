package fcul.marsphotos.ui.screens.viewmodels

sealed interface PhotosUiState {
    data class Success(
        val totalRolls: Int,
        val marsPhotos: String,
        val marsPhotoUri: String,
        val randomPhotos: String,
        val randomPhotoUri: String,
        val photoUri: String?,
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
