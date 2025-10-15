@file:OptIn(ExperimentalMaterial3Api::class)

package fcul.marsphotos.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import fcul.marsphotos.R
import fcul.marsphotos.ui.screens.HomeScreen
import fcul.marsphotos.ui.screens.viewmodels.PhotosViewModel

@Composable
fun PhotosApp() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val photosViewModel: PhotosViewModel = viewModel()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { PhotosTopAppBar(scrollBehavior = scrollBehavior) },
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            HomeScreen(
                photosUiState = photosViewModel.photosUiState,
                contentPadding = it,
            )
        }
    }
}

@Composable
fun PhotosTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        modifier = modifier,
    )
}
