package fcul.marsphotos.ui.screens

sealed class Screens(val route: String) {
    object HomeScreen : Screens("home_screen")
    object CameraScreen : Screens("camera_screen")
}