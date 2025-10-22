package fcul.marsphotos.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fcul.marsphotos.ui.screens.CameraScreen
import fcul.marsphotos.ui.screens.HomeScreen
import fcul.marsphotos.ui.screens.Screens
import fcul.marsphotos.ui.screens.viewmodels.PhotosViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    photosViewModel: PhotosViewModel,
    contentPadding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = Screens.HomeScreen.route
    ) {
        composable(Screens.HomeScreen.route) {
            HomeScreen(
                navController = navController,
                photosUiState = photosViewModel.photosUiState,
                contentPadding = contentPadding
            )
        }

        composable(Screens.CameraScreen.route) {
            CameraScreen(
                navController = navController,
                uploadPhoto = { photoUri, onSuccess, onFailure ->
                    photosViewModel.uploadPhoto(photoUri, onSuccess, onFailure)
                },
            )

        }
    }
}